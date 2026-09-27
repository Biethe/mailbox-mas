package edu.aldermere.benchmark;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public record BenchmarkProfile(String name, Map<String, Integer> limits) {
    public int limit(String category) {
        return limits.getOrDefault(category, 0);
    }

    public static BenchmarkProfile load(Path path) throws IOException {
        JsonNode root = new ObjectMapper().readTree(Files.readString(path));
        Map<String, Integer> limits = new LinkedHashMap<>();
        root.get("limits").fields().forEachRemaining(entry -> limits.put(entry.getKey(), entry.getValue().asInt()));
        return new BenchmarkProfile(root.get("name").asText(), Map.copyOf(limits));
    }
}
