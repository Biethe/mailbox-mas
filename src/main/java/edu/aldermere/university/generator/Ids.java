package edu.aldermere.university.generator;

import java.util.HashMap;
import java.util.Map;

/** Per-prefix counters. Call order is the only source of id assignment. */
public final class Ids {
    private final Map<String, Integer> counts = new HashMap<>();

    public String next(String prefix) {
        int value = counts.getOrDefault(prefix, 0) + 1;
        counts.put(prefix, value);
        return prefix + "-" + String.format("%05d", value);
    }
}
