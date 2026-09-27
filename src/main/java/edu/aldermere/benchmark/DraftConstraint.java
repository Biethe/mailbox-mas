package edu.aldermere.benchmark;

import java.util.List;

public record DraftConstraint(
        List<String> toMailboxIds,
        List<String> ccMailboxIds,
        List<String> forbiddenMailboxIds,
        String subjectMustContain,
        List<String> bodyMustContain,
        List<String> bodyMustNotContain,
        List<String> attachmentDocumentIds,
        List<String> forbiddenAttachmentIds
) {
    public DraftConstraint {
        toMailboxIds = List.copyOf(toMailboxIds);
        ccMailboxIds = List.copyOf(ccMailboxIds);
        forbiddenMailboxIds = List.copyOf(forbiddenMailboxIds);
        bodyMustContain = List.copyOf(bodyMustContain);
        bodyMustNotContain = List.copyOf(bodyMustNotContain);
        attachmentDocumentIds = List.copyOf(attachmentDocumentIds);
        forbiddenAttachmentIds = List.copyOf(forbiddenAttachmentIds);
    }

    public static DraftConstraint none() {
        return new DraftConstraint(List.of(), List.of(), List.of(), null, List.of(), List.of(), List.of(), List.of());
    }
}
