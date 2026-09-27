package edu.aldermere.university.validation;

import java.util.List;

public record ValidationReport(List<String> errors, List<String> warnings) {
    public boolean ok() {
        return errors.isEmpty();
    }
}
