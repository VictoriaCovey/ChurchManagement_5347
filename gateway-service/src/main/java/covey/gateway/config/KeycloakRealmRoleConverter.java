package covey.gateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Keycloak puts realm roles under the {@code realm_access.roles} claim rather than the
 * default OAuth2 {@code scope} claim, so Spring Security's built-in scope-to-authority
 * mapping doesn't see them. This converter reads that claim directly.
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    @SuppressWarnings("unchecked")
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        Collection<GrantedAuthority> authorities;
        if (realmAccess == null || realmAccess.get("roles") == null) {
            authorities = List.of();
        } else {
            List<String> roles = (List<String>) realmAccess.get("roles");
            authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
        }
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }
}
