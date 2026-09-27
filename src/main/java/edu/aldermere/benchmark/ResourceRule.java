package edu.aldermere.benchmark;

/** ALLOWED, REQUIRED, or FORBIDDEN. */
public record ResourceRule(String action, String resourceType, String resourceId, String decision) {}
