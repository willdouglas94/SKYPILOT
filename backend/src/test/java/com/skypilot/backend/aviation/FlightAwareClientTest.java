package com.skypilot.backend.aviation;

import com.skypilot.backend.aviation.client.FlightAwareClient;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlightAwareClientTest {

    @Test
    void shouldParseFlightAwareRoutesPayload() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/routes", exchange -> {
            byte[] body = (
                    "{\"routes\":[{" +
                    "\"id\":\"FA-1\"," +
                    "\"originCode\":\"GRU\"," +
                    "\"destinationCode\":\"REC\"," +
                    "\"distanceKm\":2800," +
                    "\"durationMinutes\":180" +
                    "}]}"
            ).getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            String baseUrl = "http://localhost:" + server.getAddress().getPort();
            FlightAwareClient client = new FlightAwareClient(baseUrl, "demo-key");

            List<FlightAwareClient.FlightAwareRoute> routes = client.fetchRoutes();

            assertThat(routes).hasSize(1);
            assertThat(routes.get(0).originCode()).isEqualTo("GRU");
            assertThat(routes.get(0).destinationCode()).isEqualTo("REC");
            assertThat(routes.get(0).distanceKm()).isEqualTo(2800);
        } finally {
            server.stop(0);
        }
    }
}
