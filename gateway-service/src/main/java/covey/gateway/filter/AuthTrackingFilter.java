package covey.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.UUID;

/**
 * Stamps every request with a correlation id and, when a bearer token is present, logs the
 * token subject so its journey through the gateway and downstream services is visible in logs.
 * Does NOT strip or replace the Authorization header — Spring Cloud Gateway forwards it to the
 * routed service unchanged, which is how the bearer token propagates across services.
 */
@Component
public class AuthTrackingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthTrackingFilter.class);
    private static final String CORRELATION_ID_HEADER = "tmx-correlation-id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest.Builder mutatedRequest = exchange.getRequest().mutate();
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        mutatedRequest.header(CORRELATION_ID_HEADER, correlationId);

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("[{}] {} {} bearerToken={}",
                correlationId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(),
                describe(authHeader));

        return chain.filter(exchange.mutate().request(mutatedRequest.build()).build());
    }

    private String describe(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "none";
        }
        try {
            String[] parts = authHeader.substring(7).split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            return payload.contains("preferred_username")
                    ? payload.replaceAll(".*\"preferred_username\":\"([^\"]+)\".*", "subject=$1")
                    : "present";
        } catch (Exception e) {
            return "present";
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
