package edu.aldermere.benchmark;

/**
 * Validation baselines may read ground truth. A production adapter must not implement this.
 */
public interface Baseline {
    ExecutionResult execute(BenchmarkTask task, ToolGateway tools);
}
