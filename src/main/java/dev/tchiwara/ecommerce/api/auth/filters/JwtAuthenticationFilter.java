package dev.tchiwara.ecommerce.api.auth.filters;

import dev.tchiwara.ecommerce.api.auth.JwtService;
import jakarta.servlet.FilterChain;import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;  // early exit pattern or guard clause
        }

        var token=authHeader.replace("Bearer ", "");

        if(!jwtService.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        //at this point the token is correct hence we need to tell spring that the user is authenticated
        //UsernamePasswordAuthenticationToken used for both authenticated and anonymous users via its constructors
        var authentication=new UsernamePasswordAuthenticationToken(
            jwtService.getEmailFromToken(token),
                null,
                null
        );

        // Attach request metadata to the authentication; not the user's identity or permissions.
        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        // Tell Spring Security that this request has been authenticated and associate the authenticated user
        // with the current security context, so downstream code can access the user's identity and authorities.
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);

    }
}
