package dev.tchiwara.ecommerce.api.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class OrderEmailBuilder {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter
                    .ofPattern("MMMM d, uuuu 'at' HH:mm 'UTC'")
                    .withZone(ZoneOffset.UTC);

    private final String currency;

    public OrderEmailBuilder(
            @Value("${stripe.currency:usd}") String currency
    ) {
        this.currency = currency;
    }

    public String subject(Order order) {
        return "Order #" + order.getId() + " confirmed";
    }

    public String plainText(Order order) {
        StringBuilder body = new StringBuilder();

        body.append("Hi ")
                .append(order.getUser().getName())
                .append(",\n\n");

        body.append("Thank you for your order. Your payment was confirmed.\n\n");

        body.append("Order ID: #")
                .append(order.getId())
                .append("\n");

        body.append("Date: ")
                .append(formatDate(order))
                .append("\n\n");

        body.append("Items:\n");

        order.getItems().forEach(item -> {
            body.append("- ")
                    .append(item.getProductName())
                    .append(" | quantity: ")
                    .append(item.getQuantity())
                    .append(" | unit price: ")
                    .append(formatMoney(item.getUnitPrice()))
                    .append(" ")
                    .append(currency.toUpperCase())
                    .append(" | line total: ")
                    .append(formatMoney(item.subtotal()))
                    .append(" ")
                    .append(currency.toUpperCase())
                    .append("\n");
        });

        body.append("\nTotal: ")
                .append(formatMoney(order.getTotalAmount()))
                .append(" ")
                .append(currency.toUpperCase())
                .append("\n\n");

        body.append("We'll notify you when your order ships.\n\n");
        body.append("Thank you for shopping with us.");

        return body.toString();
    }

    public String html(Order order) {
        String customerName = escape(order.getUser().getName());
        String orderId = escape(String.valueOf(order.getId()));
        String orderDate = escape(formatDate(order));
        String escapedCurrency = escape(currency.toUpperCase());

        StringBuilder items = new StringBuilder();

        order.getItems().forEach(item -> {
            items.append("""
                    <tr>
                      <td style="padding: 8px; border-bottom: 1px solid #ddd;">%s</td>
                      <td style="padding: 8px; border-bottom: 1px solid #ddd; text-align: center;">%d</td>
                      <td style="padding: 8px; border-bottom: 1px solid #ddd; text-align: right;">%s %s</td>
                      <td style="padding: 8px; border-bottom: 1px solid #ddd; text-align: right;">%s %s</td>
                    </tr>
                    """.formatted(
                    escape(item.getProductName()),
                    item.getQuantity(),
                    escape(formatMoney(item.getUnitPrice())),
                    escapedCurrency,
                    escape(formatMoney(item.subtotal())),
                    escapedCurrency
            ));
        });

        return """
                <div style="font-family: Arial, Helvetica, sans-serif; max-width: 720px; margin: 0 auto; padding: 24px; color: #222;">
                  <h2>Order confirmed</h2>

                  <p>Hi <strong>%s</strong>,</p>

                  <p>
                    Thank you for your order. Your payment was confirmed successfully.
                  </p>

                  <p>
                    <strong>Order:</strong> #%s<br>
                    <strong>Date:</strong> %s
                  </p>

                  <table style="width: 100%%; border-collapse: collapse; margin: 24px 0;">
                    <thead>
                      <tr>
                        <th style="padding: 8px; border-bottom: 2px solid #222; text-align: left;">Item</th>
                        <th style="padding: 8px; border-bottom: 2px solid #222; text-align: center;">Quantity</th>
                        <th style="padding: 8px; border-bottom: 2px solid #222; text-align: right;">Unit price</th>
                        <th style="padding: 8px; border-bottom: 2px solid #222; text-align: right;">Line total</th>
                      </tr>
                    </thead>
                    <tbody>
                      %s
                    </tbody>
                    <tfoot>
                      <tr>
                        <td colspan="3" style="padding: 12px 8px; text-align: right; font-weight: bold;">Total</td>
                        <td style="padding: 12px 8px; text-align: right; font-weight: bold;">%s %s</td>
                      </tr>
                    </tfoot>
                  </table>

                  <p>We'll notify you when your order ships.</p>

                  <p style="color: #666; font-size: 13px;">
                    Thank you for shopping with us.
                  </p>
                </div>
                """.formatted(
                customerName,
                orderId,
                orderDate,
                items,
                escape(formatMoney(order.getTotalAmount())),
                escapedCurrency
        );
    }

    private String formatDate(Order order) {
        if (order.getCreatedAt() == null) {
            return "Unavailable";
        }

        return DATE_FORMATTER.format(order.getCreatedAt());
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }

        return amount
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }
}