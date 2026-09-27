package edu.aldermere.benchmark;

import java.util.List;

/** Expected draft after a user turn. {@code preserve} names fields that must stay unchanged from the previous turn. */
public record TurnExpectation(int turn, String instruction, DraftState draft, List<String> preserve) {
    public TurnExpectation {
        preserve = List.copyOf(preserve);
    }
}
