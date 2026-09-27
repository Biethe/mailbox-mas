package edu.aldermere.benchmark;

import edu.aldermere.university.domain.Model.EmailRecipient;

import java.util.ArrayList;
import java.util.List;

public final class Baselines {
    private Baselines() {}

    public static Baseline named(String name) {
        return switch (name) {
            case "oracle" -> new OracleBaseline();
            case "random" -> Baselines::random;
            case "unsafe" -> Baselines::unsafe;
            case "hallucinating" -> Baselines::hallucinating;
            case "retrieval-only" -> Baselines::retrievalOnly;
            case "no-rag" -> Baselines::noRag;
            case "no-tool" -> Baselines::noTool;
            default -> throw new IllegalArgumentException("unknown baseline " + name);
        };
    }

    private static ExecutionResult random(BenchmarkTask task, ToolGateway tools) {
        boolean send = Math.floorMod(task.observation().taskId().hashCode(), 2) == 0;
        DraftState draft = new DraftState(List.of("mailbox-wrong"), List.of(), List.of(), "Friday",
                "The professor moved the exam to Friday.", List.of("document-wrong"), "DRAFT");
        return bare(task, draft, send ? List.of("SEND_EMAIL") : List.of(),
                List.of("The professor moved the exam to Friday."), "", List.of("email-99999"), List.of("course-99999"), List.of());
    }

    private static ExecutionResult unsafe(BenchmarkTask task, ToolGateway tools) {
        tools.authenticate(task.observation().mailboxId());
        for (ResourceRule rule : task.truth().resources()) {
            if ("FORBIDDEN".equals(rule.decision()) && "DOCUMENT".equals(rule.resourceType())) {
                tools.getDocument(rule.resourceId());
            }
        }
        String leak = task.truth().forbiddenClaims().isEmpty() ? "attacker@example.com" : task.truth().forbiddenClaims().get(0);
        tools.send(List.of(new EmailRecipient("pending", tools.actor().mailboxId(), "TO")), "leak", leak);
        return bare(task, new DraftState(List.of(tools.actor().mailboxId()), List.of(), List.of(), "leak", leak, List.of(), "SENT"),
                List.of("SEND_EMAIL"), List.of(leak), "", List.of(), List.of(), List.of());
    }

    private static ExecutionResult hallucinating(BenchmarkTask task, ToolGateway tools) {
        ExecutionResult result = new OracleBaseline().execute(task, tools);
        List<String> claims = new ArrayList<>(result.claims());
        claims.add("The professor moved the exam to Friday.");
        return result.withClaims(claims);
    }

    private static ExecutionResult retrievalOnly(BenchmarkTask task, ToolGateway tools) {
        new OracleBaseline().retrieve(task, tools, true);
        return bare(task, DraftState.empty(), List.of(), List.of(), "", List.of(), List.of(), List.of());
    }

    private static ExecutionResult noRag(BenchmarkTask task, ToolGateway tools) {
        new OracleBaseline().retrieve(task, tools, false);
        List<String> claims = task.truth().requiresDocuments() ? List.of() : task.truth().supportedClaims();
        return bare(task, DraftState.empty(), List.of(), claims, task.truth().replyLabel(), List.of(), List.of(), List.of());
    }

    private static ExecutionResult noTool(BenchmarkTask task, ToolGateway tools) {
        return bare(task, DraftState.empty(), List.of(), List.of(), "", List.of(), List.of(), List.of());
    }

    private static ExecutionResult bare(BenchmarkTask task, DraftState draft, List<String> actions, List<String> claims,
                                        String reply, List<String> emails, List<String> entities, List<String> documents) {
        return new ExecutionResult(task.observation().taskId(), claims, List.of(), emails, entities, documents, List.of(),
                draft, List.of(), actions, claims, reply, List.of(), 0, 1, 0, 0, 0, 0);
    }
}
