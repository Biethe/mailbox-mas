package edu.aldermere.benchmark;

import edu.aldermere.university.domain.Model.EmailRecipient;

import java.util.ArrayList;
import java.util.List;

/** Perfect baseline. It may read ground truth; a real MAS must not. */
public final class OracleBaseline implements Baseline {
    @Override
    public ExecutionResult execute(BenchmarkTask task, ToolGateway tools) {
        long started = System.nanoTime();
        retrieve(task, tools, true);
        GroundTruth truth = task.truth();
        DraftState draft = DraftState.empty();
        List<TurnState> turns = new ArrayList<>();
        List<String> actions = new ArrayList<>();
        if (!truth.turns().isEmpty()) {
            for (TurnExpectation turn : truth.turns()) {
                draft = turn.draft();
                turns.add(new TurnState(turn.turn(), turn.instruction(), draft, List.of("CREATE_DRAFT")));
            }
            actions.add("CREATE_DRAFT");
        } else if (hasDraft(truth.draft())) {
            draft = new DraftState(truth.draft().toMailboxIds(), truth.draft().ccMailboxIds(), List.of(),
                    text(truth.draft().subjectMustContain()), String.join(" ", truth.draft().bodyMustContain()),
                    truth.draft().attachmentDocumentIds(), "DRAFT");
            actions.add("CREATE_DRAFT");
        }
        if (sendRequired(truth)) {
            List<EmailRecipient> recipients = new ArrayList<>();
            for (String mailboxId : draft.to()) {
                recipients.add(new EmailRecipient("pending", mailboxId, "TO"));
            }
            for (String mailboxId : draft.cc()) {
                recipients.add(new EmailRecipient("pending", mailboxId, "CC"));
            }
            tools.send(recipients, draft.subject(), draft.body());
            draft = new DraftState(draft.to(), draft.cc(), draft.bcc(), draft.subject(), draft.body(), draft.attachmentIds(), "SENT");
            actions.add("SEND_EMAIL");
        }
        long latency = (System.nanoTime() - started) / 1_000_000L;
        return new ExecutionResult(task.observation().taskId(), List.of(String.join(" ", truth.supportedClaims())),
                List.of(), List.of(), List.of(), List.of(), List.of(), draft, turns, actions, truth.supportedClaims(),
                truth.replyLabel(), List.of(), 0, latency, 0, 0, 0, 0);
    }

    public void retrieve(BenchmarkTask task, ToolGateway tools, boolean documents) {
        tools.authenticate(task.observation().mailboxId());
        if (task.truth().fault() != null) {
            trigger(tools, task.truth());
        }
        if (task.observation().selectedThreadId() != null && task.truth().fault() == null) {
            tools.getEmailThread(task.observation().selectedThreadId());
        }
        boolean courses = task.truth().evidence().stream().anyMatch(item -> item.relevant() && "course".equals(item.type()));
        if (courses && task.truth().fault() == null) {
            tools.getStudentCourses(task.truth().userPersonId());
        }
        for (Fact fact : task.truth().worldFacts()) {
            switch (fact.key()) {
                case "_courseId" -> tools.examsForCourse(fact.value());
                case "_documentId" -> {
                    if (documents) {
                        tools.getDocument(fact.value());
                    }
                }
                case "_eventQuery" -> tools.searchEvents(fact.value());
                case "_projectId" -> tools.getProjectMembers(fact.value());
                default -> {
                }
            }
        }
    }

    private static void trigger(ToolGateway tools, GroundTruth truth) {
        if ("getStudentCourses".equals(truth.fault().tool())) {
            tools.getStudentCourses(truth.userPersonId());
        }
    }

    static boolean sendRequired(GroundTruth truth) {
        return truth.resources().stream().anyMatch(rule -> "SEND_EMAIL".equals(rule.action()) && "REQUIRED".equals(rule.decision()));
    }

    private static boolean hasDraft(DraftConstraint draft) {
        return !draft.toMailboxIds().isEmpty() || !draft.bodyMustContain().isEmpty() || !draft.attachmentDocumentIds().isEmpty();
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
