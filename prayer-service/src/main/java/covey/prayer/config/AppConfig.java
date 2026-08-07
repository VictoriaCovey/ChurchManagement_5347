package covey.prayer.config;

import covey.prayer.security.BearerTokenPropagationInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    /**
     * @LoadBalanced makes this RestTemplate resolve plain service-id URLs (e.g.
     * http://user-service/...) against the Eureka registry instead of requiring a
     * hardcoded host:port, and every call carries the caller's bearer token forward via
     * BearerTokenPropagationInterceptor. Used everywhere except the "render" profile, which
     * has no Eureka registry to resolve against (see application-render.properties) and instead
     * points user.service.url directly at that service's real URL.
     */
    @Bean
    @Profile("!render")
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate() {
        return restTemplateWithBearerToken();
    }

    @Bean
    @Profile("render")
    public RestTemplate plainRestTemplate() {
        return restTemplateWithBearerToken();
    }

    private RestTemplate restTemplateWithBearerToken() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BearerTokenPropagationInterceptor());
        return restTemplate;
    }
}
