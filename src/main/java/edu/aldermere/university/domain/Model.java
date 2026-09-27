package edu.aldermere.university.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Domain records and enums. Nested types are imported with {@code import ...domain.Model.*}. */
public final class Model {
    private Model() {}

    public enum Role { STUDENT, PROFESSOR, RESEARCHER, ADMINISTRATOR }
    public enum DegreeLevel { BACHELOR, MASTER, PHD }
    public enum AccountStatus { ACTIVE, SUSPENDED, CLOSED }
    public enum OwnerType { PERSON, UNIVERSITY_SERVICE }
    public enum MailboxStatus { ACTIVE, UNAVAILABLE }
    public enum RecipientType { TO, CC, BCC }
    public enum EnrollmentStatus { ACTIVE, COMPLETED, DROPPED, FAILED }
    public enum RoomType { CLASSROOM, OFFICE, LABORATORY, MEETING_ROOM, LECTURE_HALL }
    public enum DocumentType {
        COURSE_MATERIAL, COURSE_GUIDELINE, EXAM_DOCUMENT, RESEARCH_PAPER, RESEARCH_REPORT,
        PROJECT_DOCUMENT, UNIVERSITY_POLICY, ADMINISTRATIVE_DOCUMENT, MEETING_NOTES, MEMO, STUDENT_DOCUMENT
    }
    public enum Visibility {
        PUBLIC, UNIVERSITY, DEPARTMENT, COURSE, PROJECT, ROLE_RESTRICTED, PRIVATE, CONFIDENTIAL
    }
    public enum Sensitivity { LOW, MODERATE, HIGH }
    public enum EventType { LECTURE, EXAM, MEETING, CONFERENCE, DEFENSE, DEADLINE, ADMINISTRATIVE_EVENT }
    public enum Action { READ, CREATE, UPDATE, DELETE, SHARE }
    public enum ResourceType {
        PERSONAL_DATA, COURSE, COURSE_MATERIAL, ENROLLMENT, GRADE, EXAM,
        RESEARCH_PROJECT, PUBLICATION, DOCUMENT, EMAIL, EVENT, ADMINISTRATIVE_RECORD
    }
    public enum SubjectType { ROLE, PERSON, ADMIN_UNIT }
    public enum Scope { OWN, ENROLLED, TEACHING, PROJECT_MEMBER, DEPARTMENT, UNIT, PUBLIC }
    public enum Effect { ALLOW, DENY }
    public enum SecurityKind {
        DIRECT_INJECTION, INDIRECT_INJECTION, FAKE_AUTHORIZATION,
        MALICIOUS_TOOL, SYSTEM_IMPERSONATION, EXFILTRATION
    }
    public enum Carrier { EMAIL, DOCUMENT }
    public enum PersonStatus { ACTIVE, INACTIVE }
    public enum Priority { LOW, NORMAL, HIGH }
    public enum InstructorRole { PRIMARY, ASSISTANT }
    public enum ProjectRole { PI, RESEARCHER, ASSISTANT, STUDENT }
    public enum ParticipantRole { ORGANIZER, SPEAKER, ATTENDEE, CANDIDATE }
    public enum AccessDecision { ALLOW, ACCESS_DENIED }
    public enum ScenarioType {
        COURSE_LIST, EXAM_LOOKUP, PROJECT_MEMBERS, EMAIL_DEADLINE, GUIDELINE_RAG,
        ACCESS_DENIED, INDIRECT_INJECTION, MULTI_SOURCE, MAILBOX_IDENTITY
    }

    public record University(String id, String name, String shortName, String description,
                             String location, String website, String emailDomain) {}

    public record WorldMeta(long seed, String generatorVersion, String schemaVersion, String datasetProfile,
                            String academicYear, String currentSemester, String emailDomain, String masEmail,
                            String configuration) {}

    public record Faculty(String id, String universityId, String name, String description) {}

    public record Department(String id, String facultyId, String name, String description, String codePrefix) {}

    public record AcademicProgram(String id, String departmentId, String name, String degreeLevel, String description) {}

    public record AdministrativeUnit(String id, String universityId, String name, String description) {}

    public record Building(String id, String universityId, String name, String address) {}

    public record Room(String id, String buildingId, String roomNumber, String roomType, int capacity) {}

    public record Person(String id, String firstName, String lastName, String displayName, LocalDate dateOfBirth,
                         String departmentId, String phone, String officeId, String status) {}

    public record PersonRole(String personId, String role) {}

    public record AdministratorAssignment(String personId, String unitId) {}

    public record ServicePrincipal(String id, String name, String description) {}

    public record Mailbox(String id, String ownerType, String ownerId, String emailAddress, String provider, String status) {}

    public record UniversityAccount(String id, String personId, String servicePrincipalId, String username,
                                    String universityEmail, String status, LocalDateTime createdAt, String mailboxId) {}

    public record Course(String id, String courseCode, String name, String description, String departmentId,
                         int credits, String semester, String academicYear) {}

    public record CourseInstructor(String courseId, String personId, String role) {}

    public record Enrollment(String id, String studentId, String courseId, String academicYear, String semester,
                             String status, LocalDate enrollmentDate, String finalGrade) {}

    public record Assignment(String id, String courseId, String title, String description, LocalDateTime deadline,
                             String documentId) {}

    public record Exam(String id, String courseId, String title, String description, LocalDateTime examDate,
                       int durationMinutes, String locationId, String status, String documentId) {}

    public record ResearchProject(String id, String title, String description, String departmentId,
                                  String principalInvestigatorId, LocalDate startDate, LocalDate endDate, String status) {}

    public record ResearchProjectMember(String projectId, String personId, String role) {}

    public record ResearchPublication(String id, String projectId, String title, String abstractText, int year,
                                      String venue, String doi) {}

    public record SecurityCase(String id, String kind, String carrier, String carrierId, String expectedHandling, String note) {}

    public record Document(String id, String title, String documentType, String ownerId, LocalDateTime createdAt,
                           LocalDateTime updatedAt, String content, String mimeType, String sensitivity, String visibility,
                           String courseId, String projectId, String departmentId, String adminUnitId,
                           String securityCaseId, String securityKind) {}

    public record EmailThread(String id, String subject) {}

    public record Email(String id, String threadId, String senderMailboxId, String subject, String body,
                        LocalDateTime sentAt, String priority, boolean messageRead, String securityCaseId, String securityKind) {}

    public record EmailRecipient(String emailId, String recipientMailboxId, String recipientType) {}

    public record EmailAttachment(String id, String emailId, String documentId, String filename, String mimeType) {}

    public record Event(String id, String title, String description, String eventType, LocalDateTime startTime,
                        LocalDateTime endTime, String locationId, String organizerId, String courseId, String projectId,
                        String examId) {}

    public record EventParticipant(String eventId, String personId, String role) {}

    public record PermissionPolicy(String id, String subjectType, String subjectId, String action, String resourceType,
                                   String scope, String effect) {}

    public record CanonicalScenario(String id, String scenarioType, String userPersonId, String question,
                                    String expectedEntities, String expectedFacts, String requiredPermissions,
                                    String securityCaseId) {}

    public record ActorContext(String accountId, String mailboxId, String personId, String departmentId,
                               List<String> roles, List<String> adminUnitIds) {}

    public record ResourceFacts(String ownerId, String departmentId, String courseId, String projectId,
                                String adminUnitId, String visibility, List<String> participantPersonIds,
                                List<String> participantMailboxIds) {}
}
