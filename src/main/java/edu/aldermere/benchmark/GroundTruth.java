package edu.aldermere.benchmark;

import java.util.ArrayList;
import java.util.List;

/** Layered ground truth. Never passed to {@link MasUnderTest}. */
public record GroundTruth(
        String category,
        String difficulty,
        String userPersonId,
        List<Fact> worldFacts,
        List<EvidenceItem> evidence,
        List<ResourceRule> resources,
        List<String> supportedClaims,
        List<String> forbiddenClaims,
        String replyLabel,
        DraftConstraint draft,
        List<TurnExpectation> turns,
        FaultSpec fault,
        boolean requiresDocuments,
        boolean requiresTools,
        Integer expectedDelegations
) {
    public GroundTruth {
        worldFacts = List.copyOf(worldFacts);
        evidence = List.copyOf(evidence);
        resources = List.copyOf(resources);
        supportedClaims = List.copyOf(supportedClaims);
        forbiddenClaims = List.copyOf(forbiddenClaims);
        turns = List.copyOf(turns);
    }

    public List<String> relevantIds(String type) {
        List<String> ids = new ArrayList<>();
        for (EvidenceItem item : evidence) {
            if (item.relevant() && type.equals(item.type())) {
                ids.add(item.id());
            }
        }
        return ids;
    }

    public GroundTruth withResources(List<ResourceRule> rules) {
        return new GroundTruth(category, difficulty, userPersonId, worldFacts, evidence, rules, supportedClaims,
                forbiddenClaims, replyLabel, draft, turns, fault, requiresDocuments, requiresTools, expectedDelegations);
    }
}
