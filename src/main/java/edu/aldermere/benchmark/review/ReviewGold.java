package edu.aldermere.benchmark.review;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.aldermere.benchmark.DraftState;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReviewGold(
        String fileId,
        String stableKey,
        String category,
        String response,
        String replyLabel,
        DraftState draft,
        List<GoldTurn> turns,
        String notes,
        String status,
        String updatedAt
) {
    public ReviewGold {
        turns = turns == null ? List.of() : List.copyOf(turns);
        response = response == null ? "" : response;
        replyLabel = replyLabel == null ? "" : replyLabel;
        notes = notes == null ? "" : notes;
        status = status == null ? "reviewed" : status;
        draft = draft == null ? DraftState.empty() : draft;
    }

    public record GoldTurn(int turn, String instruction, DraftState draft) {
        public GoldTurn {
            instruction = instruction == null ? "" : instruction;
            draft = draft == null ? DraftState.empty() : draft;
        }
    }
}
