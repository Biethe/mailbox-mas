package edu.aldermere.benchmark.review;

import edu.aldermere.benchmark.BenchmarkTask;
import edu.aldermere.benchmark.DraftConstraint;
import edu.aldermere.benchmark.DraftState;
import edu.aldermere.benchmark.EvidenceItem;
import edu.aldermere.benchmark.Fact;
import edu.aldermere.benchmark.TurnExpectation;
import edu.aldermere.university.domain.Model.Assignment;
import edu.aldermere.university.domain.Model.Building;
import edu.aldermere.university.domain.Model.Course;
import edu.aldermere.university.domain.Model.Document;
import edu.aldermere.university.domain.Model.Email;
import edu.aldermere.university.domain.Model.EmailAttachment;
import edu.aldermere.university.domain.Model.EmailRecipient;
import edu.aldermere.university.domain.Model.Event;
import edu.aldermere.university.domain.Model.Exam;
import edu.aldermere.university.domain.Model.Mailbox;
import edu.aldermere.university.domain.Model.Person;
import edu.aldermere.university.domain.Model.Room;
import edu.aldermere.university.repository.WorldRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReviewCatalog {
    private final WorldRepository repo;
    private final ReviewStore store;
    private final List<BenchmarkTask> sample;
    private final Map<String, Integer> stratumSize;
    private final Map<String, BenchmarkTask> byId = new LinkedHashMap<>();

    public ReviewCatalog(WorldRepository repo, ReviewStore store, List<BenchmarkTask> sample,
                         Map<String, Integer> stratumSize) {
        this.repo = repo;
        this.store = store;
        this.sample = sample;
        this.stratumSize = stratumSize;
        for (BenchmarkTask task : sample) {
            byId.put(ReviewIds.fileId(ReviewIds.stableKey(task)), task);
        }
    }

    public List<QueueRow> queue() throws IOException {
        List<QueueRow> rows = new ArrayList<>();
        for (BenchmarkTask task : sample) {
            String key = ReviewIds.stableKey(task);
            String fileId = ReviewIds.fileId(key);
            boolean reviewed = store.load(fileId).isPresent();
            rows.add(new QueueRow(fileId, task.truth().category(), task.truth().difficulty(),
                    task.observation().operation(), task.observation().instructions().get(0), reviewed,
                    stratumSize.getOrDefault(task.truth().category(), 0)));
        }
        return rows;
    }

    public ReviewItem item(String fileId) throws IOException {
        BenchmarkTask task = byId.get(fileId);
        if (task == null) {
            return null;
        }
        String key = ReviewIds.stableKey(task);
        Person user = repo.findPerson(task.truth().userPersonId()).orElse(null);
        Mailbox mailbox = repo.findMailbox(task.observation().mailboxId()).orElse(null);
        DraftState autoDraft = autoDraft(task);
        return new ReviewItem(
                fileId,
                key,
                task.truth().category(),
                task.truth().difficulty(),
                task.observation().operation(),
                task.observation().instructions(),
                user == null ? task.truth().userPersonId() : user.displayName(),
                mailbox == null ? "" : mailbox.emailAddress(),
                String.join(", ", repo.rolesOf(task.truth().userPersonId())),
                facts(task),
                claims(task),
                task.truth().replyLabel(),
                sendRule(task),
                task.truth().forbiddenClaims(),
                resources(task),
                autoDraft,
                task.truth().turns(),
                mailTask(task.truth().category()),
                store.load(fileId).orElse(null)
        );
    }

    private List<FactLine> facts(BenchmarkTask task) {
        List<FactLine> lines = new ArrayList<>();
        for (Fact fact : task.truth().worldFacts()) {
            if (!fact.key().startsWith("_")) {
                lines.add(new FactLine(fact.key(), fact.value()));
            }
        }
        return lines;
    }

    private static List<String> claims(BenchmarkTask task) {
        return task.truth().supportedClaims();
    }

    private static String sendRule(BenchmarkTask task) {
        return task.truth().resources().stream()
                .filter(rule -> "SEND_EMAIL".equals(rule.action()))
                .map(rule -> rule.decision())
                .findFirst()
                .orElse("FORBIDDEN");
    }

    private static boolean mailTask(String category) {
        return "DRAFT".equals(category) || "COMPOSE".equals(category)
                || "SEND_SAFETY".equals(category) || "COMPLEX_WORKFLOW".equals(category);
    }

    private static DraftState autoDraft(BenchmarkTask task) {
        if (!task.truth().turns().isEmpty()) {
            TurnExpectation last = task.truth().turns().get(task.truth().turns().size() - 1);
            return last.draft();
        }
        DraftConstraint draft = task.truth().draft();
        if (draft.toMailboxIds().isEmpty() && draft.bodyMustContain().isEmpty() && draft.attachmentDocumentIds().isEmpty()) {
            return DraftState.empty();
        }
        return new DraftState(draft.toMailboxIds(), draft.ccMailboxIds(), List.of(),
                draft.subjectMustContain() == null ? "" : draft.subjectMustContain(),
                String.join("\n", draft.bodyMustContain()),
                draft.attachmentDocumentIds(),
                "DRAFT");
    }

    private List<SourceMaterial> resources(BenchmarkTask task) {
        List<SourceMaterial> materials = new ArrayList<>();
        Map<String, Boolean> seen = new LinkedHashMap<>();
        if (task.observation().selectedThreadId() != null) {
            for (Email email : repo.emailsInThread(task.observation().selectedThreadId())) {
                addEmail(materials, seen, email, "Selected thread");
            }
        } else if (task.observation().selectedEmailId() != null) {
            repo.findEmail(task.observation().selectedEmailId()).ifPresent(email -> addEmail(materials, seen, email, "Selected email"));
        }
        for (EvidenceItem item : task.truth().evidence()) {
            String label = item.relevant() ? "Relevant" : "Distractor";
            switch (item.type()) {
                case "email" -> repo.findEmail(item.id()).ifPresent(email -> addEmail(materials, seen, email, label));
                case "document" -> repo.findDocument(item.id()).ifPresent(document -> addDocument(materials, seen, document, label));
                case "course" -> repo.findCourse(item.id()).ifPresent(course -> addCourse(materials, seen, course, label));
                case "exam" -> repo.findExam(item.id()).ifPresent(exam -> addExam(materials, seen, exam, label));
                case "event" -> repo.findEvent(item.id()).ifPresent(event -> addEvent(materials, seen, event, label));
                case "room" -> repo.findRoom(item.id()).ifPresent(room -> addRoom(materials, seen, room, label));
                default -> {
                }
            }
        }
        for (String documentId : task.truth().draft().attachmentDocumentIds()) {
            repo.findDocument(documentId).ifPresent(document -> addDocument(materials, seen, document, "Draft attachment"));
        }
        expandRelated(materials, seen);
        return materials;
    }

    private void expandRelated(List<SourceMaterial> materials, Map<String, Boolean> seen) {
        List<String> courseIds = new ArrayList<>();
        List<String> emailIds = new ArrayList<>();
        for (SourceMaterial material : materials) {
            if ("Course".equals(material.type())) {
                courseIds.add(material.id());
            }
            if ("Email".equals(material.type())) {
                emailIds.add(material.id());
            }
        }
        for (String emailId : emailIds) {
            for (EmailAttachment attachment : repo.attachmentsOf(emailId)) {
                repo.findDocument(attachment.documentId()).ifPresent(document -> addDocument(materials, seen, document, "Email attachment"));
            }
        }
        for (String courseId : courseIds) {
            for (Document document : repo.documents()) {
                if (courseId.equals(document.courseId())) {
                    addDocument(materials, seen, document, "Course document");
                }
            }
            for (Exam exam : repo.examsForCourse(courseId)) {
                addExam(materials, seen, exam, "Course exam");
            }
            for (Assignment assignment : repo.assignmentsForCourse(courseId)) {
                addAssignment(materials, seen, assignment);
                if (assignment.documentId() != null) {
                    repo.findDocument(assignment.documentId()).ifPresent(document -> addDocument(materials, seen, document, "Assignment document"));
                }
            }
        }
    }

    private void addEmail(List<SourceMaterial> materials, Map<String, Boolean> seen, Email email, String role) {
        if (seen.putIfAbsent("email:" + email.id(), true) != null) {
            return;
        }
        StringBuilder meta = new StringBuilder();
        meta.append("From ").append(mailboxLabel(email.senderMailboxId()));
        meta.append("\nTo ").append(recipientLine(email.id(), "TO"));
        String cc = recipientLine(email.id(), "CC");
        if (!cc.isBlank()) {
            meta.append("\nCc ").append(cc);
        }
        meta.append("\n").append(email.sentAt());
        if (email.securityKind() != null) {
            meta.append("\nSecurity case: ").append(email.securityKind()).append(" (untrusted content)");
        }
        materials.add(new SourceMaterial(role, "Email", email.id(), email.subject(), meta.toString(), email.body()));
    }

    private void addDocument(List<SourceMaterial> materials, Map<String, Boolean> seen, Document document, String role) {
        if (seen.putIfAbsent("document:" + document.id(), true) != null) {
            return;
        }
        String owner = repo.findPerson(document.ownerId()).map(Person::displayName).orElse(document.ownerId());
        String meta = document.documentType() + " · " + document.visibility() + " · owner " + owner;
        materials.add(new SourceMaterial(role, "Document", document.id(), document.title(), meta, document.content()));
    }

    private void addCourse(List<SourceMaterial> materials, Map<String, Boolean> seen, Course course, String role) {
        if (seen.putIfAbsent("course:" + course.id(), true) != null) {
            return;
        }
        String meta = course.semester() + " " + course.academicYear() + " · " + course.credits() + " credits";
        materials.add(new SourceMaterial(role, "Course", course.id(), course.courseCode() + " " + course.name(), meta, course.description()));
    }

    private void addExam(List<SourceMaterial> materials, Map<String, Boolean> seen, Exam exam, String role) {
        if (seen.putIfAbsent("exam:" + exam.id(), true) != null) {
            return;
        }
        Room room = repo.findRoom(exam.locationId()).orElse(null);
        Building building = room == null ? null : repo.findBuilding(room.buildingId()).orElse(null);
        String where = room == null ? exam.locationId() : room.roomNumber() + (building == null ? "" : ", " + building.name());
        Course course = repo.findCourse(exam.courseId()).orElse(null);
        String title = (course == null ? exam.courseId() : course.courseCode()) + " exam";
        String body = "When: " + exam.examDate() + "\nDuration: " + exam.durationMinutes() + " minutes\nWhere: " + where
                + "\nStatus: " + exam.status();
        materials.add(new SourceMaterial(role, "Exam", exam.id(), title, exam.title(), body));
    }

    private void addEvent(List<SourceMaterial> materials, Map<String, Boolean> seen, Event event, String role) {
        if (seen.putIfAbsent("event:" + event.id(), true) != null) {
            return;
        }
        Room room = repo.findRoom(event.locationId()).orElse(null);
        String where = room == null ? event.locationId() : room.roomNumber();
        String body = event.eventType() + "\n" + event.startTime() + " to " + event.endTime() + "\nRoom " + where
                + "\n\n" + event.description();
        materials.add(new SourceMaterial(role, "Event", event.id(), event.title(), "", body));
    }

    private void addAssignment(List<SourceMaterial> materials, Map<String, Boolean> seen, Assignment assignment) {
        if (seen.putIfAbsent("assignment:" + assignment.id(), true) != null) {
            return;
        }
        String body = "Échéance : " + assignment.deadline() + "\n\n" + assignment.description();
        materials.add(new SourceMaterial("Course assignment", "Assignment", assignment.id(), assignment.title(), "", body));
    }

    private void addRoom(List<SourceMaterial> materials, Map<String, Boolean> seen, Room room, String role) {
        if (seen.putIfAbsent("room:" + room.id(), true) != null) {
            return;
        }
        Building building = repo.findBuilding(room.buildingId()).orElse(null);
        String title = (building == null ? "" : building.name() + " ") + room.roomNumber();
        materials.add(new SourceMaterial(role, "Room", room.id(), title, room.roomType() + ", capacity " + room.capacity(), ""));
    }

    private String mailboxLabel(String mailboxId) {
        Mailbox mailbox = repo.findMailbox(mailboxId).orElse(null);
        if (mailbox == null) {
            return mailboxId;
        }
        if (!"PERSON".equals(mailbox.ownerType())) {
            return "University assistant <" + mailbox.emailAddress() + ">";
        }
        String name = repo.findPerson(mailbox.ownerId()).map(Person::displayName).orElse(mailbox.ownerId());
        return name + " <" + mailbox.emailAddress() + ">";
    }

    private String recipientLine(String emailId, String type) {
        List<String> names = new ArrayList<>();
        for (EmailRecipient recipient : repo.recipientsOf(emailId)) {
            if (type.equals(recipient.recipientType())) {
                names.add(mailboxLabel(recipient.recipientMailboxId()));
            }
        }
        return String.join(", ", names);
    }

    public record QueueRow(String fileId, String category, String difficulty, String operation, String instruction,
                           boolean reviewed, int stratumSize) {}

    public record FactLine(String key, String value) {}

    public record SourceMaterial(String role, String type, String id, String title, String meta, String body) {}

    public record ReviewItem(
            String fileId,
            String stableKey,
            String category,
            String difficulty,
            String operation,
            List<String> instructions,
            String userName,
            String userEmail,
            String roles,
            List<FactLine> facts,
            List<String> autoClaims,
            String replyLabel,
            String sendRule,
            List<String> forbiddenClaims,
            List<SourceMaterial> materials,
            DraftState autoDraft,
            List<TurnExpectation> autoTurns,
            boolean mailTask,
            ReviewGold gold
    ) {}
}
