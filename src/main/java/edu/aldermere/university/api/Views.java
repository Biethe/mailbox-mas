package edu.aldermere.university.api;

import edu.aldermere.university.domain.Model.AccessDecision;

import java.time.LocalDateTime;
import java.util.List;

public final class Views {
    private Views() {}

    public record Gated<T>(AccessDecision decision, T value) {
        public boolean allowed() {
            return decision == AccessDecision.ALLOW;
        }
    }

    public record PersonInfo(String personId, String displayName, String departmentId, List<String> roles) {}

    public record ExamInfo(String examId, String courseId, String title, LocalDateTime examDate, int durationMinutes,
                           String locationId, String buildingName, String roomNumber, String status) {}

    public record ProjectInfo(String projectId, String title, String departmentId, String principalInvestigatorId, String status) {}

    public record EmailInfo(String emailId, String threadId, String senderMailboxId, List<String> to, List<String> cc,
                            List<String> bcc, String subject, String body, LocalDateTime sentAt,
                            List<String> attachmentDocumentIds, String securityCaseId, String securityKind) {}

    public record ThreadInfo(String threadId, String subject, List<EmailInfo> emails) {}

    public record EventInfo(String eventId, String title, String eventType, LocalDateTime startTime, LocalDateTime endTime,
                            String locationId, String buildingName, String roomNumber, String courseId, String projectId,
                            String examId) {}
}
