package edu.aldermere.benchmark;

import java.util.List;

public record ExecutionResult(
        String taskId,
        List<String> agentMessages,
        List<ToolCall> toolCalls,
        List<String> retrievedEmailIds,
        List<String> retrievedEntityIds,
        List<String> retrievedDocumentIds,
        List<String> permissionChecks,
        DraftState draft,
        List<TurnState> turns,
        List<String> actions,
        List<String> claims,
        String replyLabel,
        List<String> errors,
        int sendCount,
        long latencyMillis,
        int llmCalls,
        int promptTokens,
        int completionTokens,
        int delegations
) {
    public ExecutionResult {
        agentMessages = List.copyOf(agentMessages);
        toolCalls = List.copyOf(toolCalls);
        retrievedEmailIds = List.copyOf(retrievedEmailIds);
        retrievedEntityIds = List.copyOf(retrievedEntityIds);
        retrievedDocumentIds = List.copyOf(retrievedDocumentIds);
        permissionChecks = List.copyOf(permissionChecks);
        turns = List.copyOf(turns);
        actions = List.copyOf(actions);
        claims = List.copyOf(claims);
        errors = List.copyOf(errors);
        draft = draft == null ? DraftState.empty() : draft;
        replyLabel = replyLabel == null ? "" : replyLabel;
    }

    public ExecutionResult overlayTools(List<ToolCall> calls, List<String> retrievedEmails, List<String> retrievedEntities,
                                        List<String> retrievedDocuments, List<String> checks, List<String> gatewayErrors,
                                        List<String> gatewayActions, int sends) {
        return new ExecutionResult(taskId, agentMessages, calls, retrievedEmails, retrievedEntities, retrievedDocuments,
                checks, draft, turns, gatewayActions.isEmpty() ? actions : gatewayActions, claims, replyLabel,
                join(errors, gatewayErrors), sends, latencyMillis, llmCalls, promptTokens, completionTokens, delegations);
    }

    public ExecutionResult withClaims(List<String> extra) {
        return new ExecutionResult(taskId, agentMessages, toolCalls, retrievedEmailIds, retrievedEntityIds,
                retrievedDocumentIds, permissionChecks, draft, turns, actions, extra, replyLabel, errors, sendCount,
                latencyMillis, llmCalls, promptTokens, completionTokens, delegations);
    }

    public ExecutionResult withDraft(DraftState replacement) {
        return new ExecutionResult(taskId, agentMessages, toolCalls, retrievedEmailIds, retrievedEntityIds,
                retrievedDocumentIds, permissionChecks, replacement, turns, actions, claims, replyLabel, errors, sendCount,
                latencyMillis, llmCalls, promptTokens, completionTokens, delegations);
    }

    public ExecutionResult withActions(List<String> replacement) {
        return new ExecutionResult(taskId, agentMessages, toolCalls, retrievedEmailIds, retrievedEntityIds,
                retrievedDocumentIds, permissionChecks, draft, turns, replacement, claims, replyLabel, errors, sendCount,
                latencyMillis, llmCalls, promptTokens, completionTokens, delegations);
    }

    public ExecutionResult withDocuments(List<String> replacement) {
        return new ExecutionResult(taskId, agentMessages, toolCalls, retrievedEmailIds, retrievedEntityIds,
                replacement, permissionChecks, draft, turns, actions, claims, replyLabel, errors, sendCount,
                latencyMillis, llmCalls, promptTokens, completionTokens, delegations);
    }

    private static List<String> join(List<String> left, List<String> right) {
        java.util.ArrayList<String> all = new java.util.ArrayList<>(left);
        all.addAll(right);
        return all;
    }
}
