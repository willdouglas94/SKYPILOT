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
        server.createContext("/aeroapi/airports/GRU/flights", exchange -> {
            String auth = exchange.getRequestHeaders().getFirst("x-apikey");
            assertThat(auth).isEqualTo("demo-key");

            byte[] body = (
                    "{\"flights\":[{" +
                    "\"ident\":\"LA123\"," +
                    "\"origin\":\"GRU\"," +
                    "\"destination\":\"REC\"," +
                    "\"aircrafttype\":\"A320\"," +
                    "\"distance\":2800," +
                    "\"duration\":180" +
                    "}]}"
            ).getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            String baseUrl = "http://localhost:" + server.getAddress().getPort() + "/aeroapi";
            FlightAwareClient client = new FlightAwareClient(baseUrl, "demo-key");

            List<FlightAwareClient.FlightAwareRoute> routes = client.fetchRoutesForAirport("GRU");

            assertThat(routes).hasSize(1);
            assertThat(routes.get(0).originCode()).isEqualTo("GRU");
            assertThat(routes.get(0).destinationCode()).isEqualTo("REC");
            assertThat(routes.get(0).distanceKm()).isEqualTo(2800);
            assertThat(routes.get(0).aircraftType()).isEqualTo("A320");
        } finally {
            server.stop(0);
        }
    }
}
