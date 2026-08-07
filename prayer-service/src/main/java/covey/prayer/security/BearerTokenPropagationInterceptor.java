package covey.prayer.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Re-attaches the current request's bearer token (captured by {@link BearerTokenContextFilter})
 * to outbound RestTemplate calls this service makes to other services, so the caller's identity
 * and roles carry across the service-to-service hop.
 */
public class BearerTokenPropagationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String token = BearerTokenHolder.get();
        if (token != null && !token.isBlank()) {
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, token);
        }
        return execution.execute(request, body);
    }
}
