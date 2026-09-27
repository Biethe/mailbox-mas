package edu.aldermere.benchmark;

import java.util.List;

public record TurnState(int turn, String instruction, DraftState draft, List<String> actions) {
    public TurnState {
        actions = List.copyOf(actions);
    }
}
