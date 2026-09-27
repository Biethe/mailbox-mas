package edu.aldermere.benchmark;

/**
 * Adapter for a real system under test. Implementations must not be given {@link GroundTruth}.
 */
public interface MasUnderTest {
    ExecutionResult execute(TaskObservation observation, ToolGateway tools);
}
