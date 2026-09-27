package edu.aldermere.benchmark;

import edu.aldermere.university.api.UniversityTools;
import edu.aldermere.university.api.Views.EmailInfo;
import edu.aldermere.university.api.Views.EventInfo;
import edu.aldermere.university.api.Views.ExamInfo;
import edu.aldermere.university.api.Views.Gated;
import edu.aldermere.university.api.Views.PersonInfo;
import edu.aldermere.university.api.Views.ThreadInfo;
import edu.aldermere.university.domain.Model.AccessDecision;
import edu.aldermere.university.domain.Model.ActorContext;
import edu.aldermere.university.domain.Model.Course;
import edu.aldermere.university.domain.Model.Document;
import edu.aldermere.university.domain.Model.Email;
import edu.aldermere.university.domain.Model.EmailRecipient;
import edu.aldermere.university.service.MailService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The only university surface a system under test receives. Calls are recorded here,
 * so a trace cannot invent tool results that the university did not return.
 */
public final class ToolGateway {
    private final UniversityTools tools;
    private final MailService mail;
    private final FaultSpec fault;
    private final List<ToolCall> calls = new ArrayList<>();
    private final List<String> emails = new ArrayList<>();
    private final List<String> entities = new ArrayList<>();
    private final List<String> documents = new ArrayList<>();
    private final List<String> checks = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final List<String> actions = new ArrayList<>();
    private int sends;
    private ActorContext actor;

    public ToolGateway(UniversityTools tools, MailService mail, FaultSpec fault) {
        this.tools = tools;
        this.mail = mail;
        this.fault = fault;
    }

    public ActorContext actor() {
        return actor;
    }

    public List<ToolCall> calls() { return List.copyOf(calls); }
    public List<String> emails() { return List.copyOf(emails); }
    public List<String> entities() { return List.copyOf(entities); }
    public List<String> documents() { return List.copyOf(documents); }
    public List<String> checks() { return List.copyOf(checks); }
    public List<String> errors() { return List.copyOf(errors); }
    public List<String> actions() { return List.copyOf(actions); }
    public int sends() { return sends; }
    public boolean failed() { return !errors.isEmpty(); }

    public ActorContext authenticate(String mailboxId) {
        actor = tools.authenticate(mailboxId);
        calls.add(new ToolCall("authenticate", "mailboxId=" + mailboxId, actor.personId() == null ? "service" : actor.personId(), false));
        return actor;
    }

    public Gated<List<Course>> getStudentCourses(String studentId) {
        String args = actorArgs("studentId=" + studentId);
        if (blocked("getStudentCourses", args)) {
            return empty();
        }
        Gated<List<Course>> result = tools.getStudentCourses(actor, studentId);
        if (result.allowed() && result.value() != null) {
            for (Course course : result.value()) {
                note(entities, course.id());
            }
        }
        calls.add(new ToolCall("getStudentCourses", args, result.decision().name(), false));
        return result;
    }

    public Gated<List<ExamInfo>> examsForCourse(String courseId) {
        String args = actorArgs("courseId=" + courseId);
        if (blocked("examsForCourse", args)) {
            return empty();
        }
        Gated<List<ExamInfo>> result = tools.examsForCourse(actor, courseId);
        if (result.allowed() && result.value() != null) {
            for (ExamInfo exam : result.value()) {
                note(entities, exam.examId());
                note(entities, exam.courseId());
                note(entities, exam.locationId());
            }
        }
        calls.add(new ToolCall("examsForCourse", args, result.decision().name(), false));
        return result;
    }

    public Gated<Document> getDocument(String documentId) {
        String args = actorArgs("documentId=" + documentId);
        if (blocked("getDocument", args)) {
            return empty();
        }
        Gated<Document> result = tools.getDocument(actor, documentId);
        if (result.allowed() && result.value() != null) {
            note(documents, result.value().id());
        }
        calls.add(new ToolCall("getDocument", args, result.decision().name(), false));
        return result;
    }

    public Gated<ThreadInfo> getEmailThread(String threadId) {
        String args = actorArgs("threadId=" + threadId);
        if (blocked("getEmailThread", args)) {
            return empty();
        }
        Gated<ThreadInfo> result = tools.getEmailThread(actor, threadId);
        if (result.allowed() && result.value() != null) {
            for (EmailInfo email : result.value().emails()) {
                note(emails, email.emailId());
            }
        }
        calls.add(new ToolCall("getEmailThread", args, result.decision().name(), false));
        return result;
    }

    public Gated<List<EventInfo>> searchEvents(String query) {
        String args = actorArgs("query=" + query);
        if (blocked("searchEvents", args)) {
            return empty();
        }
        Gated<List<EventInfo>> result = tools.searchEvents(actor, query);
        if (result.allowed() && result.value() != null) {
            for (EventInfo event : result.value()) {
                note(entities, event.eventId());
            }
        }
        calls.add(new ToolCall("searchEvents", args, result.decision().name(), false));
        return result;
    }

    public Gated<List<PersonInfo>> getProjectMembers(String projectId) {
        String args = actorArgs("projectId=" + projectId);
        if (blocked("getProjectMembers", args)) {
            return empty();
        }
        Gated<List<PersonInfo>> result = tools.getProjectMembers(actor, projectId);
        if (result.allowed() && result.value() != null) {
            note(entities, projectId);
            for (PersonInfo person : result.value()) {
                note(entities, person.personId());
            }
        }
        calls.add(new ToolCall("getProjectMembers", args, result.decision().name(), false));
        return result;
    }

    public AccessDecision checkAccess(String action, String resourceType, String resourceId) {
        String args = actorArgs(action + " " + resourceType + " " + resourceId);
        if (blocked("checkAccess", args)) {
            return AccessDecision.ACCESS_DENIED;
        }
        AccessDecision decision = tools.checkAccess(actor, action, resourceType, resourceId);
        checks.add(action + " " + resourceType + " " + resourceId + " " + decision.name());
        calls.add(new ToolCall("checkAccess", args, decision.name(), false));
        return decision;
    }

    public Email send(List<EmailRecipient> recipients, String subject, String body) {
        String args = actorArgs("to=" + recipients.size() + " subject=" + subject);
        if (blocked("send", args)) {
            return null;
        }
        if (actor == null) {
            errors.add("UNAVAILABLE:send");
            calls.add(new ToolCall("send", args, "ERROR", true));
            return null;
        }
        Email email = mail.send(actor.mailboxId(), recipients, subject, body, null, LocalDateTime.parse("2025-11-01T12:00:00"));
        sends++;
        actions.add("SEND_EMAIL");
        calls.add(new ToolCall("send", args, email.id(), false));
        return email;
    }

    private boolean blocked(String tool, String args) {
        if (fault == null || !fault.tool().equals(tool)) {
            return false;
        }
        calls.add(new ToolCall(tool, args, fault.mode(), !"EMPTY".equals(fault.mode())));
        errors.add(fault.mode() + ":" + tool);
        return true;
    }

    private String actorArgs(String args) {
        String id = actor == null ? "none" : actor.mailboxId();
        return "mailboxId=" + id + " " + args;
    }

    private static void note(List<String> into, String id) {
        if (id != null && !into.contains(id)) {
            into.add(id);
        }
    }

    private static <T> Gated<T> empty() {
        return new Gated<>(AccessDecision.ACCESS_DENIED, null);
    }
}
