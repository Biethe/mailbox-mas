package edu.aldermere.university.service;

import edu.aldermere.university.domain.Model.AccessDecision;
import edu.aldermere.university.domain.Model.Action;
import edu.aldermere.university.domain.Model.ActorContext;
import edu.aldermere.university.domain.Model.Course;
import edu.aldermere.university.domain.Model.CourseInstructor;
import edu.aldermere.university.domain.Model.Document;
import edu.aldermere.university.domain.Model.Email;
import edu.aldermere.university.domain.Model.EmailRecipient;
import edu.aldermere.university.domain.Model.Enrollment;
import edu.aldermere.university.domain.Model.Event;
import edu.aldermere.university.domain.Model.EventParticipant;
import edu.aldermere.university.domain.Model.Exam;
import edu.aldermere.university.domain.Model.Mailbox;
import edu.aldermere.university.domain.Model.PermissionPolicy;
import edu.aldermere.university.domain.Model.Person;
import edu.aldermere.university.domain.Model.ResearchProject;
import edu.aldermere.university.domain.Model.ResearchPublication;
import edu.aldermere.university.domain.Model.ResourceFacts;
import edu.aldermere.university.domain.Model.ResourceType;
import edu.aldermere.university.repository.WorldRepository;

import java.util.ArrayList;
import java.util.List;

/** Explicit allow/deny. No role is omnipotent, and a missing policy is a denial. */
public final class AccessControlService {
    private final WorldRepository repo;

    public AccessControlService(WorldRepository repo) {
        this.repo = repo;
    }

    public AccessDecision check(ActorContext actor, String action, String resourceType, String resourceId) {
        try {
            Action.valueOf(action);
            ResourceType.valueOf(resourceType);
        } catch (IllegalArgumentException e) {
            return AccessDecision.ACCESS_DENIED;
        }
        ResourceFacts facts = facts(resourceType, resourceId);
        if (facts == null) {
            return AccessDecision.ACCESS_DENIED;
        }
        for (PermissionPolicy policy : repo.policiesFor(action, resourceType)) {
            if ("DENY".equals(policy.effect()) && subjectMatches(policy, actor) && scopeHolds(policy, actor, facts, resourceType, resourceId)) {
                return AccessDecision.ACCESS_DENIED;
            }
        }
        if ("READ".equals(action) && same(actor.personId(), facts.ownerId())) {
            return AccessDecision.ALLOW;
        }
        for (PermissionPolicy policy : repo.policiesFor(action, resourceType)) {
            if ("ALLOW".equals(policy.effect()) && subjectMatches(policy, actor)
                    && scopeHolds(policy, actor, facts, resourceType, resourceId)
                    && visibilityAllows(actor, facts)) {
                return AccessDecision.ALLOW;
            }
        }
        return AccessDecision.ACCESS_DENIED;
    }

    private ResourceFacts facts(String resourceType, String resourceId) {
        return switch (resourceType) {
            case "PERSONAL_DATA" -> personFacts(resourceId);
            case "COURSE" -> courseFacts(resourceId);
            case "ENROLLMENT", "GRADE" -> enrollmentFacts(resourceId);
            case "EXAM" -> examFacts(resourceId);
            case "DOCUMENT", "COURSE_MATERIAL", "ADMINISTRATIVE_RECORD" -> documentFacts(resourceId);
            case "RESEARCH_PROJECT" -> projectFacts(resourceId);
            case "PUBLICATION" -> publicationFacts(resourceId);
            case "EMAIL" -> emailFacts(resourceId);
            case "EVENT" -> eventFacts(resourceId);
            default -> null;
        };
    }

    private ResourceFacts personFacts(String personId) {
        return repo.findPerson(personId)
                .map(person -> facts(person.id(), person.departmentId(), null, null, null, null, List.of(person.id()), List.of()))
                .orElse(null);
    }

    private ResourceFacts courseFacts(String courseId) {
        return repo.findCourse(courseId).map(course -> facts(primaryInstructor(course.id()), course.departmentId(), course.id(),
                null, null, null, List.of(), List.of())).orElse(null);
    }

    private ResourceFacts enrollmentFacts(String enrollmentId) {
        Enrollment enrollment = repo.findEnrollment(enrollmentId).orElse(null);
        if (enrollment == null) {
            return null;
        }
        Course course = repo.findCourse(enrollment.courseId()).orElse(null);
        if (course == null) {
            return null;
        }
        return facts(enrollment.studentId(), course.departmentId(), course.id(), null, null, "ROLE_RESTRICTED",
                List.of(enrollment.studentId()), List.of());
    }

    private ResourceFacts examFacts(String examId) {
        Exam exam = repo.findExam(examId).orElse(null);
        if (exam == null) {
            return null;
        }
        Course course = repo.findCourse(exam.courseId()).orElse(null);
        if (course == null) {
            return null;
        }
        return facts(primaryInstructor(course.id()), course.departmentId(), course.id(), null, null, null, List.of(), List.of());
    }

    private ResourceFacts documentFacts(String documentId) {
        Document document = repo.findDocument(documentId).orElse(null);
        if (document == null) {
            return null;
        }
        return facts(document.ownerId(), document.departmentId(), document.courseId(), document.projectId(),
                document.adminUnitId(), document.visibility(), List.of(), List.of());
    }

    private ResourceFacts projectFacts(String projectId) {
        ResearchProject project = repo.findProject(projectId).orElse(null);
        if (project == null) {
            return null;
        }
        return facts(project.principalInvestigatorId(), project.departmentId(), null, project.id(), null, null, List.of(), List.of());
    }

    private ResourceFacts publicationFacts(String publicationId) {
        ResearchPublication publication = null;
        for (ResearchPublication row : repo.publications()) {
            if (row.id().equals(publicationId)) {
                publication = row;
            }
        }
        if (publication == null) {
            return null;
        }
        return projectFacts(publication.projectId());
    }

    private ResourceFacts emailFacts(String emailId) {
        Email email = repo.findEmail(emailId).orElse(null);
        if (email == null) {
            return null;
        }
        List<String> mailboxes = new ArrayList<>();
        List<String> people = new ArrayList<>();
        addMailbox(mailboxes, people, email.senderMailboxId());
        for (EmailRecipient recipient : repo.recipientsOf(email.id())) {
            addMailbox(mailboxes, people, recipient.recipientMailboxId());
        }
        return facts(null, null, null, null, null, null, people, mailboxes);
    }

    private ResourceFacts eventFacts(String eventId) {
        Event event = repo.findEvent(eventId).orElse(null);
        if (event == null) {
            return null;
        }
        List<String> people = new ArrayList<>();
        people.add(event.organizerId());
        for (EventParticipant participant : repo.participantsOf(event.id())) {
            if (!people.contains(participant.personId())) {
                people.add(participant.personId());
            }
        }
        String departmentId = null;
        if (event.courseId() != null) {
            departmentId = repo.findCourse(event.courseId()).map(Course::departmentId).orElse(null);
        } else if (event.projectId() != null) {
            departmentId = repo.findProject(event.projectId()).map(ResearchProject::departmentId).orElse(null);
        }
        return facts(event.organizerId(), departmentId, event.courseId(), event.projectId(), null, null, people, List.of());
    }

    private void addMailbox(List<String> mailboxes, List<String> people, String mailboxId) {
        if (!mailboxes.contains(mailboxId)) {
            mailboxes.add(mailboxId);
        }
        Mailbox mailbox = repo.findMailbox(mailboxId).orElse(null);
        if (mailbox != null && "PERSON".equals(mailbox.ownerType()) && !people.contains(mailbox.ownerId())) {
            people.add(mailbox.ownerId());
        }
    }

    private String primaryInstructor(String courseId) {
        for (CourseInstructor instructor : repo.instructors()) {
            if (instructor.courseId().equals(courseId) && "PRIMARY".equals(instructor.role())) {
                return instructor.personId();
            }
        }
        return null;
    }

    private boolean subjectMatches(PermissionPolicy policy, ActorContext actor) {
        return switch (policy.subjectType()) {
            case "ROLE" -> actor.roles().contains(policy.subjectId());
            case "PERSON" -> same(actor.personId(), policy.subjectId());
            case "ADMIN_UNIT" -> actor.adminUnitIds().contains(policy.subjectId());
            default -> false;
        };
    }

    private boolean scopeHolds(PermissionPolicy policy, ActorContext actor, ResourceFacts facts, String resourceType, String resourceId) {
        return switch (policy.scope()) {
            case "OWN" -> switch (resourceType) {
                case "PERSONAL_DATA" -> same(actor.personId(), resourceId);
                case "EMAIL" -> facts.participantMailboxIds().contains(actor.mailboxId());
                case "EVENT" -> same(actor.personId(), facts.ownerId()) || facts.participantPersonIds().contains(actor.personId());
                default -> same(actor.personId(), facts.ownerId());
            };
            case "ENROLLED" -> actor.personId() != null && facts.courseId() != null && repo.isEnrolled(actor.personId(), facts.courseId());
            case "TEACHING" -> teaching(actor, facts, resourceType, resourceId);
            case "PROJECT_MEMBER" -> actor.personId() != null && facts.projectId() != null && repo.isProjectMember(actor.personId(), facts.projectId());
            case "DEPARTMENT" -> same(actor.departmentId(), facts.departmentId());
            case "UNIT" -> facts.adminUnitId() != null && actor.adminUnitIds().contains(facts.adminUnitId());
            case "PUBLIC" -> "PUBLIC".equals(facts.visibility()) || "UNIVERSITY".equals(facts.visibility());
            default -> false;
        };
    }

    private boolean teaching(ActorContext actor, ResourceFacts facts, String resourceType, String resourceId) {
        if (actor.personId() == null) {
            return false;
        }
        if ("PERSONAL_DATA".equals(resourceType)) {
            return repo.professorTeachesStudent(actor.personId(), resourceId);
        }
        return facts.courseId() != null && repo.isInstructor(actor.personId(), facts.courseId());
    }

    private boolean visibilityAllows(ActorContext actor, ResourceFacts facts) {
        if (facts.visibility() == null) {
            return true;
        }
        return switch (facts.visibility()) {
            case "PUBLIC", "UNIVERSITY" -> true;
            case "DEPARTMENT" -> same(actor.departmentId(), facts.departmentId());
            case "COURSE" -> actor.personId() != null && facts.courseId() != null
                    && (repo.isEnrolled(actor.personId(), facts.courseId()) || repo.isInstructor(actor.personId(), facts.courseId()));
            case "PROJECT" -> actor.personId() != null && facts.projectId() != null && repo.isProjectMember(actor.personId(), facts.projectId());
            case "ROLE_RESTRICTED" -> true;
            case "PRIVATE" -> same(actor.personId(), facts.ownerId());
            case "CONFIDENTIAL" -> same(actor.personId(), facts.ownerId())
                    || (facts.adminUnitId() != null && actor.adminUnitIds().contains(facts.adminUnitId()));
            default -> false;
        };
    }

    private static ResourceFacts facts(String ownerId, String departmentId, String courseId, String projectId, String adminUnitId,
                                       String visibility, List<String> people, List<String> mailboxes) {
        return new ResourceFacts(ownerId, departmentId, courseId, projectId, adminUnitId, visibility, people, mailboxes);
    }

    private static boolean same(String left, String right) {
        return left != null && left.equals(right);
    }
}
