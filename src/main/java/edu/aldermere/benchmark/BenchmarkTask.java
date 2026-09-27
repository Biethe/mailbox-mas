package edu.aldermere.benchmark;

import java.util.ArrayList;
import java.util.List;

public record BenchmarkTask(TaskObservation observation, GroundTruth truth) {
    public BenchmarkTask withResources(List<ResourceRule> rules) {
        return new BenchmarkTask(observation, truth.withResources(rules));
    }

    public BenchmarkTask forbidRead(String documentId) {
        List<ResourceRule> rules = new ArrayList<>(truth.resources());
        rules.add(new ResourceRule("READ", "DOCUMENT", documentId, "FORBIDDEN"));
        return withResources(rules);
    }
}
