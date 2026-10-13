package dev.tchiwara.ecommerce.api.global;

import dev.tchiwara.ecommerce.api.cart.ItemNotInCartException;
import dev.tchiwara.ecommerce.api.payment.StripePaymentException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException exception
    ) {
        var errors = new HashMap<String, String>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return ResponseEntity
                .badRequest()
                .body(errors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(
            ResourceNotFoundException exception
    ) {
        var error = new HashMap<String, String>();
        error.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        var error = new HashMap<String, String>();

        error.put(
                "message",
                "Invalid value '" + exception.getValue()
                        + "' for parameter: " + exception.getName()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        var error = new HashMap<String, String>();
        error.put("message", exception.getMessage());

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(ItemNotInCartException.class)
    public ResponseEntity<Map<String, String>> handleItemNotInCart(
            ItemNotInCartException exception
    ) {
        var error = new HashMap<String, String>();
        error.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException(
            BadCredentialsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<Map<String, String>>
    handleEmailAlreadyRegistered(
            EmailAlreadyRegisteredException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Email already registered"));
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<Map<String, String>> handleEmptyCart(
            EmptyCartException exception
    ) {
        var error = new HashMap<String, String>();
        error.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler({
            InsufficientStockException.class,
            PriceChangedException.class
    })
    public ResponseEntity<Map<String, String>> handleCheckoutConflict(
            RuntimeException exception
    ) {
        var error = new HashMap<String, String>();
        error.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(IllegalOrderStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalOrderState(
            IllegalOrderStateException exception
    ) {
        var error = new HashMap<String, String>();
        error.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(StripePaymentException.class)
    public ResponseEntity<Map<String, String>> handleStripePayment(
            StripePaymentException exception
    ) {
        var error = new HashMap<String, String>();
        error.put(
                "message",
                "The payment provider could not process the request."
        );

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(error);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>>
    handleConcurrentModification(
            ObjectOptimisticLockingFailureException exception
    ) {
        var error = new HashMap<String, String>();
        error.put(
                "message",
                "The resource was modified by another request. " +
                        "Please retry."
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }
}