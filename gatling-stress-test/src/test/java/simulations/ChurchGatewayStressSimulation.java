package simulations;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class ChurchGatewayStressSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("gateway.baseUrl", "https://localhost:8080");
    private static final String TOKEN_PATH = "/auth/realms/church-realm/protocol/openid-connect/token";
    private static final int SEEDED_PRAYERS = 20;
    private static final int SEEDED_EVENTS = 5;

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json");

    private ChainBuilder fetchToken(String username, String password, String sessionKey) {
        return exec(http("Get token for " + username)
                .post(TOKEN_PATH)
                .formParam("grant_type", "password")
                .formParam("client_id", "church-gateway")
                .formParam("username", username)
                .formParam("password", password)
                .check(jsonPath("$.access_token").saveAs(sessionKey)));
    }

    private static Iterator<Map<String, Object>> cyclicIdFeeder(int count) {
        return Stream.iterate(1, i -> i % count + 1)
                .map(i -> Map.<String, Object>of("id", i))
                .iterator();
    }

    private final ScenarioBuilder seedScenario = scenario("Seed data")
            .exec(fetchToken("admin", "admin123", "adminToken"))
            .exec(http("Create member user (idempotent)")
                    .post("/api/users")
                    .header("Authorization", "Bearer #{adminToken}")
                    .body(StringBody("{\"username\":\"member\",\"role\":\"MEMBER\"}")).asJson()
                    .check(status().in(201, 400, 409, 500)))
            .exec(fetchToken("member", "member123", "memberToken"))
            .repeat(SEEDED_PRAYERS).on(
                    exec(http("Seed prayer request")
                            .post("/api/public/prayers")
                            .header("Authorization", "Bearer #{memberToken}")
                            .body(StringBody("{\"requesterName\":\"member\",\"content\":\"Please pray for our congregation.\"}")).asJson())
            )
            .repeat(SEEDED_EVENTS).on(
                    exec(http("Seed event")
                            .post("/api/events")
                            .header("Authorization", "Bearer #{adminToken}")
                            .body(StringBody("{\"title\":\"Stress Test Event\",\"eventDate\":\"2027-01-01T10:00:00\",\"location\":\"Main Hall\",\"capacity\":500}")).asJson())
            );

    private final ScenarioBuilder memberScenario = scenario("Member browsing")
            .exec(fetchToken("member", "member123", "memberToken"))
            .exec(http("List public prayers")
                    .get("/api/public/prayers")
                    .header("Authorization", "Bearer #{memberToken}"))
            .pause(Duration.ofMillis(200))
            .exec(http("List upcoming events")
                    .get("/api/events")
                    .header("Authorization", "Bearer #{memberToken}"))
            .pause(Duration.ofMillis(200))
            .exec(session -> session.set("registrant", "member-" + UUID.randomUUID()))
            .feed(cyclicIdFeeder(SEEDED_EVENTS))
            .exec(http("Register for event (publishes JMS notification)")
                    .post("/api/events/#{id}/register")
                    .header("Authorization", "Bearer #{memberToken}")
                    .body(StringBody("{\"memberName\":\"#{registrant}\"}")).asJson());

    private final ScenarioBuilder adminScenario = scenario("Admin moderation")
            .exec(fetchToken("admin", "admin123", "adminToken"))
            .exec(http("List pending prayers")
                    .get("/api/admin/prayers/pending")
                    .header("Authorization", "Bearer #{adminToken}"))
            .pause(Duration.ofMillis(200))
            .feed(cyclicIdFeeder(SEEDED_PRAYERS))
            .exec(http("Approve prayer request (publishes JMS notification)")
                    .post("/api/admin/prayers/#{id}/approve")
                    .header("Authorization", "Bearer #{adminToken}"));

    {
        setUp(
                seedScenario.injectOpen(atOnceUsers(1))
                        .andThen(
                                memberScenario.injectOpen(rampUsers(40).during(Duration.ofSeconds(30))),
                                adminScenario.injectOpen(rampUsers(10).during(Duration.ofSeconds(30)))
                        )
        )
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().max().lt(5000),
                        global().successfulRequests().percent().gt(95.0)
                );
    }
}
