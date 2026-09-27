package edu.aldermere.university.service;

import edu.aldermere.university.api.Views.EmailInfo;
import edu.aldermere.university.api.Views.EventInfo;
import edu.aldermere.university.api.Views.ExamInfo;
import edu.aldermere.university.api.Views.Gated;
import edu.aldermere.university.api.Views.PersonInfo;
import edu.aldermere.university.api.Views.ProjectInfo;
import edu.aldermere.university.api.Views.ThreadInfo;
import edu.aldermere.university.domain.Model.AccessDecision;
import edu.aldermere.university.domain.Model.ActorContext;
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
import edu.aldermere.university.domain.Model.ResearchProject;
import edu.aldermere.university.domain.Model.ResearchProjectMember;
import edu.aldermere.university.domain.Model.Room;
import edu.aldermere.university.domain.Model.UniversityAccount;
import edu.aldermere.university.domain.Model.WorldMeta;
import edu.aldermere.university.repository.WorldRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class UniversityServices {
    private final WorldRepository repo;
    private final AccessControlService access;

    public UniversityServices(WorldRepository repo, AccessControlService access) {
        this.repo = repo;
        this.access = access;
    }

    public AccessDecision checkPermission(ActorContext actor, String action, String resourceType, String resourceId) {
        return access.check(actor, action, resourceType, resourceId);
    }

    /** Identity comes from the mailbox row, never from message text. */
    public ActorContext authenticate(String mailboxId) {
        Mailbox mailbox = repo.findMailbox(mailboxId).orElseThrow(() -> new IllegalArgumentException("unknown mailbox"));
        UniversityAccount account = repo.findAccountByMailbox(mailbox.id()).orElseThrow(() -> new IllegalStateException("mailbox has no account"));
        if ("UNIVERSITY_SERVICE".equals(mailbox.ownerType())) {
            return new ActorContext(account.id(), mailbox.id(), null, null, List.of(), List.of());
        }
        Person person = repo.findPerson(mailbox.ownerId()).orElseThrow(() -> new IllegalStateException("mailbox owner missing"));
        return new ActorContext(account.id(), mailbox.id(), person.id(), person.departmentId(),
                repo.rolesOf(person.id()), repo.adminUnitsOf(person.id()));
    }

    public Gated<PersonInfo> findPerson(ActorContext actor, String personId) {
        if (checkPermission(actor, "READ", "PERSONAL_DATA", personId) != AccessDecision.ALLOW) {
            return deny();
        }
        return repo.findPerson(personId).map(person -> allow(info(person))).orElseGet(() -> deny());
    }

    public Gated<List<PersonInfo>> searchPeople(ActorContext actor, String query) {
        if (actor.accountId() == null) {
            return deny();
        }
        List<PersonInfo> found = new ArrayList<>();
        for (Person person : repo.people()) {
            if (contains(person.displayName(), query)) {
                found.add(info(person));
            }
        }
        return allow(found);
    }

    public Gated<Course> findCourse(ActorContext actor, String courseId) {
        if (checkPermission(actor, "READ", "COURSE", courseId) != AccessDecision.ALLOW) {
            return deny();
        }
        return repo.findCourse(courseId).map(course -> allow(course)).orElseGet(() -> deny());
    }

    public Gated<List<Course>> searchCourses(ActorContext actor, String query) {
        List<Course> found = new ArrayList<>();
        for (Course course : repo.courses()) {
            if ((contains(course.courseCode(), query) || contains(course.name(), query))
                    && checkPermission(actor, "READ", "COURSE", course.id()) == AccessDecision.ALLOW) {
                found.add(course);
            }
        }
        return allow(found);
    }

    public Gated<List<Course>> findCoursesForStudent(ActorContext actor, String studentId) {
        WorldMeta meta = repo.meta();
        List<Course> courses = repo.coursesForStudent(studentId, meta.academicYear(), meta.currentSemester());
        if (studentId.equals(actor.personId())) {
            return allow(courses);
        }
        if (actor.roles().contains("PROFESSOR")) {
            List<Course> taught = new ArrayList<>();
            for (Course course : courses) {
                if (repo.isInstructor(actor.personId(), course.id())) {
                    taught.add(course);
                }
            }
            return taught.isEmpty() ? deny() : allow(taught);
        }
        return deny();
    }

    public Gated<List<PersonInfo>> findStudentsInCourse(ActorContext actor, String courseId) {
        if (checkPermission(actor, "READ", "COURSE", courseId) != AccessDecision.ALLOW) {
            return deny();
        }
        List<PersonInfo> students = new ArrayList<>();
        for (Person student : repo.studentsInCourse(courseId)) {
            students.add(info(student));
        }
        return allow(students);
    }

    public Gated<List<Course>> findProfessorCourses(ActorContext actor, String professorId) {
        if (!professorId.equals(actor.personId())) {
            return deny();
        }
        return allow(repo.coursesForProfessor(professorId));
    }

    public Gated<List<ExamInfo>> findExamsForCourse(ActorContext actor, String courseId) {
        List<ExamInfo> exams = new ArrayList<>();
        for (Exam exam : repo.examsForCourse(courseId)) {
            if (checkPermission(actor, "READ", "EXAM", exam.id()) == AccessDecision.ALLOW) {
                Room room = repo.findRoom(exam.locationId()).orElseThrow();
                Building building = repo.findBuilding(room.buildingId()).orElseThrow();
                exams.add(new ExamInfo(exam.id(), exam.courseId(), exam.title(), exam.examDate(), exam.durationMinutes(),
                        room.id(), building.name(), room.roomNumber(), exam.status()));
            }
        }
        return exams.isEmpty() ? deny() : allow(exams);
    }

    public Gated<List<ProjectInfo>> findResearchProjects(ActorContext actor, String query) {
        List<ProjectInfo> found = new ArrayList<>();
        for (ResearchProject project : repo.projects()) {
            if (contains(project.title(), query)
                    && checkPermission(actor, "READ", "RESEARCH_PROJECT", project.id()) == AccessDecision.ALLOW) {
                found.add(new ProjectInfo(project.id(), project.title(), project.departmentId(),
                        project.principalInvestigatorId(), project.status()));
            }
        }
        return allow(found);
    }

    public Gated<List<PersonInfo>> findProjectMembers(ActorContext actor, String projectId) {
        if (checkPermission(actor, "READ", "RESEARCH_PROJECT", projectId) != AccessDecision.ALLOW) {
            return deny();
        }
        List<PersonInfo> members = new ArrayList<>();
        for (ResearchProjectMember member : repo.membersOf(projectId)) {
            repo.findPerson(member.personId()).ifPresent(person -> members.add(info(person)));
        }
        return allow(members);
    }

    public Gated<List<Document>> findDocuments(ActorContext actor, String query) {
        List<Document> found = new ArrayList<>();
        for (Document document : repo.documents()) {
            if (contains(document.title(), query)
                    && checkPermission(actor, "READ", "DOCUMENT", document.id()) == AccessDecision.ALLOW) {
                found.add(document);
            }
        }
        return allow(found);
    }

    public Gated<Document> findDocument(ActorContext actor, String documentId) {
        if (checkPermission(actor, "READ", "DOCUMENT", documentId) != AccessDecision.ALLOW) {
            return deny();
        }
        return repo.findDocument(documentId).map(document -> allow(document)).orElseGet(() -> deny());
    }

    public Gated<List<EmailInfo>> findEmails(ActorContext actor, String query) {
        List<EmailInfo> found = new ArrayList<>();
        for (Email email : repo.emailsForMailbox(actor.mailboxId())) {
            if (contains(email.subject(), query) || contains(email.body(), query)) {
                found.add(toInfo(actor, email));
            }
        }
        return allow(found);
    }

    public Gated<ThreadInfo> getEmailThread(ActorContext actor, String threadId) {
        List<Email> emails = repo.emailsInThread(threadId);
        if (emails.isEmpty()) {
            return deny();
        }
        List<EmailInfo> visible = new ArrayList<>();
        for (Email email : emails) {
            if (participates(actor, email)) {
                visible.add(toInfo(actor, email));
            }
        }
        if (visible.isEmpty()) {
            return deny();
        }
        return allow(new ThreadInfo(threadId, emails.get(0).subject(), visible));
    }

    public Gated<List<EventInfo>> findEvents(ActorContext actor, String query) {
        List<EventInfo> found = new ArrayList<>();
        for (Event event : repo.events()) {
            if (contains(event.title(), query) && checkPermission(actor, "READ", "EVENT", event.id()) == AccessDecision.ALLOW) {
                Room room = repo.findRoom(event.locationId()).orElseThrow();
                Building building = repo.findBuilding(room.buildingId()).orElseThrow();
                found.add(new EventInfo(event.id(), event.title(), event.eventType(), event.startTime(), event.endTime(),
                        room.id(), building.name(), room.roomNumber(), event.courseId(), event.projectId(), event.examId()));
            }
        }
        return allow(found);
    }

    private PersonInfo info(Person person) {
        return new PersonInfo(person.id(), person.displayName(), person.departmentId(), repo.rolesOf(person.id()));
    }

    private EmailInfo toInfo(ActorContext actor, Email email) {
        List<String> to = new ArrayList<>();
        List<String> cc = new ArrayList<>();
        List<String> bcc = new ArrayList<>();
        boolean actorBcc = false;
        for (EmailRecipient recipient : repo.recipientsOf(email.id())) {
            switch (recipient.recipientType()) {
                case "TO" -> to.add(recipient.recipientMailboxId());
                case "CC" -> cc.add(recipient.recipientMailboxId());
                case "BCC" -> {
                    if (recipient.recipientMailboxId().equals(actor.mailboxId())) {
                        actorBcc = true;
                    }
                    bcc.add(recipient.recipientMailboxId());
                }
                default -> {
                }
            }
        }
        boolean seeBcc = email.senderMailboxId().equals(actor.mailboxId()) || actorBcc;
        List<String> attachments = new ArrayList<>();
        for (EmailAttachment attachment : repo.attachmentsOf(email.id())) {
            attachments.add(attachment.documentId());
        }
        return new EmailInfo(email.id(), email.threadId(), email.senderMailboxId(), to, cc, seeBcc ? List.copyOf(bcc) : List.of(),
                email.subject(), email.body(), email.sentAt(), attachments, email.securityCaseId(), email.securityKind());
    }

    private boolean participates(ActorContext actor, Email email) {
        if (email.senderMailboxId().equals(actor.mailboxId())) {
            return true;
        }
        for (EmailRecipient recipient : repo.recipientsOf(email.id())) {
            if (recipient.recipientMailboxId().equals(actor.mailboxId())) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(String haystack, String needle) {
        if (needle == null || needle.isBlank()) {
            return true;
        }
        return haystack != null && haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private static <T> Gated<T> allow(T value) {
        return new Gated<>(AccessDecision.ALLOW, value);
    }

    private static <T> Gated<T> deny() {
        return new Gated<>(AccessDecision.ACCESS_DENIED, null);
    }
}
