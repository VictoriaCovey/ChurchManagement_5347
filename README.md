==============================================================
VICTORIA COVEY - Church Management System Project Run Instructions
==============================================================

PREREQUISITES
----------------------------------------------------------------
- Java 17 or higher installed
- Docker Desktop - Installed and running
- Postman (for testing endpoints)


HOW TO RUN
----------------------------------------------------------------
1. Clone the repository from:
   https://github.com/VictoriaCovey/ChurchManagement_5347

2. Open a terminal and navigate to the ChurchManagement_5347 folder

3. Start the application in the dev profile using the command:
   docker compose up --build

4. Watch all containers load (5 including config-service) in on docker desktop and wait for the terminal to finish populating and setting up the application run
   
5. There should be no data currently stored in any of the service's databases
   
6. To test, open the Postman collection associated with this submission. The following services can now be tested using the inputted API endpoints and raw JSON bodies:
      A. Member service
      B. Prayer Service
      C. Event Service
      D. Notification Service

7. Close down the run by sending the command:
   docker compose down 

8. Now model the prod profile by running the command: 
    docker compose --env-file .env.prod --profile prod up --build

9. Repeat step 6 until satisfied with system's operation. 

10. Close down the instance by sending the command: 
   docker compose --env-file .env.prod --profile prod down

11. Wait until all containers are closed down, then start back up in prod profile again with the command: 
   docker compose --profile prod up                           

12. Open Postman GET endpoints of your choosing from step 9 again, and verify that the data that was created during testing of Postman in prod profile is still visible and was not removed. 

JMS + DISTRIBUTED TRACING
----------------------------------------------------------------
- prayer-service (on prayer approval/rejection) and events-service (on event registration) both
  publish notifications as JMS messages to an ActiveMQ Artemis queue ("notifications.queue")
  instead of calling notification-service directly over REST; notification-service consumes the
  queue and creates the Notification record.
- Artemis web console: http://localhost:8161 (login artemis/artemis) - watch message counts on
  notifications.queue.
- Zipkin UI (distributed traces, including the JMS hop): http://localhost:9411/zipkin
- If ports 8080/8081/8082 are already in use on your machine by something else, override the
  host-side ports before starting: `GATEWAY_PORT=18080 PRAYER_PORT=18081 USER_PORT=18082 docker
  compose up --build` (container-internal ports and all service-to-service traffic are
  unaffected either way).

STRESS TEST
----------------------------------------------------------------
- With the stack running (dev profile), from gatling-stress-test/: `mvn gatling:test`
- Simulates member browsing/event registration and admin prayer-request moderation through the
  gateway; the registration and approval requests are what generate JMS + trace volume - see
  VIDEO_DEMO_SCRIPT.md.
- Override the target: `mvn gatling:test -Dgateway.baseUrl=https://localhost:8080` (or your
  GATEWAY_PORT override).

CLOUD DEPLOYMENT (RENDER)
----------------------------------------------------------------
- render.yaml at the repo root is a Render Blueprint deploying gateway-service, config-service,
  keycloak, and the 4 business services (not eureka-service/artemis/zipkin - see comments in
  render.yaml for why).
- In the Render dashboard: New -> Blueprint -> point at this repo -> Render reads render.yaml and
  provisions all 7 services.
- Service URLs in render.yaml assume the churchmgmt-* names are available; if any is taken,
  update both the `name:` field and every env var referencing that URL.
- Each service runs on Spring profile `render`, which swaps Eureka-based service discovery for
  static Render URLs (see gateway-service/application-render.yml) and disables the gateway's
  internal SSL (Render terminates TLS at its edge).
