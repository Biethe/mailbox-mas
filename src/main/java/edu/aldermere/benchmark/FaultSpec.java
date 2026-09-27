package edu.aldermere.benchmark;

/** A fault the runner applies inside the tool gateway. The system under test is not told about it. */
public record FaultSpec(String tool, String mode) {}
