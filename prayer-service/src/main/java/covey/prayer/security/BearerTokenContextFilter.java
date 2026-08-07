package covey.prayer.security;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Captures the incoming request's Authorization header into {@link BearerTokenHolder} so that
 * downstream service calls made while handling this request (e.g. prayer-service notifying
 * notification-service) can propagate the same bearer token.
 */
@Component
public class BearerTokenContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        try {
            BearerTokenHolder.set(request.getHeader(HttpHeaders.AUTHORIZATION));
            filterChain.doFilter(request, response);
        } finally {
            BearerTokenHolder.clear();
        }
    }
}
