package edu.aldermere.benchmark;

import java.util.ArrayList;
import java.util.List;

/**
 * What a system under test is allowed to see. Ground truth is not in this object.
 */
public record TaskObservation(
        String taskId,
        String mailboxId,
        String operation,
        List<String> instructions,
        String selectedEmailId,
        String selectedThreadId
) {
    public TaskObservation {
        instructions = List.copyOf(instructions);
    }
}
