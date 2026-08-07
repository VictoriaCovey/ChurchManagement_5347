FROM quay.io/keycloak/keycloak:25.0
COPY realm-export.json /opt/keycloak/data/import/realm-export.json
# --http-relative-path=/auth makes /auth Keycloak's own base path, so every link Keycloak
# generates (registration, forgot-password, form actions, static assets) already carries the
# same /auth prefix the gateway's route matches on - no path-rewrite filter needed, no dead
# links. Passed directly on start-dev (not a separate `kc.sh build` step) because start-dev
# re-runs its own auto-build from the live start command each boot and ignores an earlier
# persisted build-time config.
#
# --proxy-headers=xforwarded makes Keycloak trust the X-Forwarded-Host/-Proto/-Port headers
# the gateway sends (Spring Cloud Gateway adds these by default) when building browser-facing
# absolute URLs (form actions, redirects). Without this, Keycloak builds those URLs from the
# raw internal connection it sees from the gateway (http://keycloak:8080), which the browser
# can never reach - this is unrelated to --hostname, which we deliberately do NOT set here,
# since a fixed --hostname would also override the direct backend-to-backend issuer check
# Spring Security performs against http://keycloak:8080 (no forwarded headers on that path).
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm", "--http-relative-path=/auth", "--proxy-headers=xforwarded"]
