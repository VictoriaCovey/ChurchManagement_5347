package covey.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    // Keycloak issues tokens with iss=<external gateway address> (since --proxy-headers=
    // xforwarded makes it trust the gateway's forwarded headers), but public keys must still be
    // fetched from Keycloak's internal Docker-network address - "localhost" from inside this
    // container refers to this container, not Keycloak. A plain issuer-uri property conflates
    // "where to fetch keys from" with "what issuer to accept," which breaks here, so both are
    // configured explicitly and combined in a custom decoder instead.
    @Value("${keycloak.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${keycloak.issuer-uri}")
    private String issuerUri;

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        decoder.setJwtValidator(withIssuer);
        return decoder;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ReactiveJwtDecoder jwtDecoder) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // HSTS tells the browser to permanently refuse to let the user click through a
                // certificate warning for this origin. That's correct for a real deployment, but
                // fatal for a local self-signed dev certificate - Chrome silently starts
                // rejecting new connections once it caches this header, with no visible warning.
                .headers(headers -> headers.hsts(ServerHttpSecurity.HeaderSpec.HstsSpec::disable))
                .authorizeExchange(exchanges -> exchanges
                        .matchers(ServerWebExchangeMatchers.pathMatchers(
                                "/auth/**", "/api/public/**", "/actuator/health", "/ws/**"))
                        .permitAll()
                        // events-service's own SecurityConfig already makes browsing events
                        // public (GET /api/events/**) - the gateway was more restrictive than
                        // the service behind it, rejecting anonymous browsing before it ever
                        // reached events-service. Writes still fall through to the authenticated
                        // catch-all below, and events-service enforces the real ADMIN/PASTOR
                        // check on those independently either way.
                        .matchers(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/api/events/**"))
                        .permitAll()
                        .matchers(ServerWebExchangeMatchers.pathMatchers("/api/admin/**"))
                        .hasAnyRole("ADMIN", "PASTOR")
                        .anyExchange()
                        .authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(jwtDecoder).jwtAuthenticationConverter(new KeycloakRealmRoleConverter()))
                );
        return http.build();
    }
}
