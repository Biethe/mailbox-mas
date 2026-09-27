package edu.aldermere.benchmark;

public record ToolCall(String tool, String arguments, String result, boolean failed) {}
