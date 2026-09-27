package edu.aldermere.benchmark.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ReviewServer {
    private final ReviewCatalog catalog;
    private final ReviewStore store;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public ReviewServer(ReviewCatalog catalog, ReviewStore store) {
        this.catalog = catalog;
        this.store = store;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", this::handle);
        server.start();
    }

    private void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        try {
            if ("GET".equals(exchange.getRequestMethod()) && ("/".equals(path) || "/index.html".equals(path))) {
                byte[] html = readPage();
                write(exchange, 200, "text/html; charset=utf-8", html);
                return;
            }
            if ("GET".equals(exchange.getRequestMethod()) && "/api/queue".equals(path)) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("items", catalog.queue());
                write(exchange, 200, "application/json", mapper.writeValueAsBytes(body));
                return;
            }
            if ("GET".equals(exchange.getRequestMethod()) && path.startsWith("/api/item/")) {
                ReviewCatalog.ReviewItem item = catalog.item(path.substring("/api/item/".length()));
                if (item == null) {
                    write(exchange, 404, "application/json", "{\"error\":\"not found\"}".getBytes(StandardCharsets.UTF_8));
                    return;
                }
                write(exchange, 200, "application/json", mapper.writeValueAsBytes(item));
                return;
            }
            if ("PUT".equals(exchange.getRequestMethod()) && path.startsWith("/api/item/")) {
                String fileId = path.substring("/api/item/".length());
                if (catalog.item(fileId) == null) {
                    write(exchange, 404, "application/json", "{\"error\":\"not found\"}".getBytes(StandardCharsets.UTF_8));
                    return;
                }
                ReviewGold incoming = mapper.readValue(exchange.getRequestBody(), ReviewGold.class);
                ReviewGold saved = new ReviewGold(fileId, incoming.stableKey(), incoming.category(), incoming.response(),
                        incoming.replyLabel(), incoming.draft(), incoming.turns(), incoming.notes(), "reviewed",
                        Instant.now().toString());
                store.save(saved);
                write(exchange, 200, "application/json", mapper.writeValueAsBytes(saved));
                return;
            }
            write(exchange, 404, "text/plain", "not found".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            write(exchange, 500, "text/plain", e.getMessage().getBytes(StandardCharsets.UTF_8));
        }
    }

    private static byte[] readPage() throws IOException {
        try (InputStream in = ReviewServer.class.getResourceAsStream("/review/index.html")) {
            if (in == null) {
                throw new IOException("review page missing");
            }
            return in.readAllBytes();
        }
    }

    private static void write(HttpExchange exchange, int status, String type, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
