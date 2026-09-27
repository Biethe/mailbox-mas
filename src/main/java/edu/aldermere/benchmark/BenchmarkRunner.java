package edu.aldermere.benchmark;

import java.util.ArrayList;
import java.util.List;

public final class BenchmarkRunner {
    private final edu.aldermere.university.WorldSession session;

    public BenchmarkRunner(edu.aldermere.university.WorldSession session) {
        this.session = session;
    }

    public List<Scored> run(List<BenchmarkTask> tasks, Baseline baseline) {
        List<Scored> scored = new ArrayList<>();
        for (BenchmarkTask task : tasks) {
            ToolGateway tools = new ToolGateway(session.tools, session.mail, session.repo, task.truth().fault());
            ExecutionResult result = baseline.execute(task, tools).overlayTools(
                    tools.calls(), tools.emails(), tools.entities(), tools.documents(), tools.checks(),
                    tools.errors(), tools.actions(), tools.sends());
            scored.add(new Scored(task, result, Evaluator.evaluate(task, result)));
        }
        return scored;
    }

    public record Scored(BenchmarkTask task, ExecutionResult result, Evaluator.TaskScore score) {}
}
