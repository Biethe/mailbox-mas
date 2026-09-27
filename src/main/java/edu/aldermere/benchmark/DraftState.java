package edu.aldermere.benchmark;

import java.util.List;

public record DraftState(
        List<String> to,
        List<String> cc,
        List<String> bcc,
        String subject,
        String body,
        List<String> attachmentIds,
        String status
) {
    public DraftState {
        to = List.copyOf(to);
        cc = List.copyOf(cc);
        bcc = List.copyOf(bcc);
        attachmentIds = List.copyOf(attachmentIds);
        subject = subject == null ? "" : subject;
        body = body == null ? "" : body;
        status = status == null ? "DRAFT" : status;
    }

    public static DraftState empty() {
        return new DraftState(List.of(), List.of(), List.of(), "", "", List.of(), "EMPTY");
    }

    public String field(String name) {
        return switch (name) {
            case "TO" -> String.join(",", to);
            case "CC" -> String.join(",", cc);
            case "BCC" -> String.join(",", bcc);
            case "SUBJECT" -> subject;
            case "BODY" -> body;
            case "ATTACHMENTS" -> String.join(",", attachmentIds);
            default -> throw new IllegalArgumentException(name);
        };
    }
}
