package edu.aldermere.benchmark.review;

import edu.aldermere.benchmark.BenchmarkTask;
import edu.aldermere.benchmark.EvidenceItem;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

public final class ReviewIds {
    private ReviewIds() {}

    public static String stableKey(BenchmarkTask task) {
        List<String> evidence = new ArrayList<>();
        for (EvidenceItem item : task.truth().evidence()) {
            evidence.add(item.type() + ":" + item.id() + ":" + item.relevant());
        }
        evidence.sort(Comparator.naturalOrder());
        String fault = task.truth().fault() == null
                ? ""
                : task.truth().fault().tool() + ":" + task.truth().fault().mode();
        return task.truth().category()
                + "|" + task.truth().userPersonId()
                + "|" + task.observation().operation()
                + "|" + empty(task.observation().selectedEmailId())
                + "|" + empty(task.observation().selectedThreadId())
                + "|" + String.join(",", evidence)
                + "|" + fault
                + "|" + String.join("\n", task.observation().instructions());
    }

    public static String fileId(String stableKey) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(stableKey.getBytes(StandardCharsets.UTF_8));
            String category = stableKey.substring(0, stableKey.indexOf('|'));
            return category + "-" + HexFormat.of().formatHex(digest, 0, 6);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }
}
