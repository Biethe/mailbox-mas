package edu.aldermere.university.repository;

import java.util.List;

public record IntegrityFindings(List<String> errors, List<String> warnings) {
    public boolean ok() {
        return errors.isEmpty();
    }
}
