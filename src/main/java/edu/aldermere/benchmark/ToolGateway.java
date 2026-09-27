package edu.aldermere.benchmark;

import edu.aldermere.university.api.UniversityTools;
import edu.aldermere.university.api.Views.EmailInfo;
import edu.aldermere.university.api.Views.Gated;
import edu.aldermere.university.api.Views.ThreadInfo;
import edu.aldermere.university.domain.Model.AccessDecision;
import edu.aldermere.university.domain.Model.ActorContext;
import edu.aldermere.university.domain.Model.Document;
import edu.aldermere.university.domain.Model.Email;
import edu.aldermere.university.domain.Model.EmailAttachment;
import edu.aldermere.university.domain.Model.EmailRecipient;
import edu.aldermere.university.repository.WorldRepository;
import edu.aldermere.university.service.MailService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The only surface a system under test receives: the authenticated mailbox.
 * A document is returned only when it is attached to a message in that mailbox.
 */
public final class ToolGateway {
    private final UniversityTools tools;
    private final MailService mail;
    private final WorldRepository repo;
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

    public ToolGateway(UniversityTools tools, MailService mail, WorldRepository repo, FaultSpec fault) {
        this.tools = tools;
        this.mail = mail;
        this.repo = repo;
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

    public Gated<List<EmailInfo>> searchEmails(String query) {
        String args = actorArgs("query=" + query);
        if (blocked("searchEmails", args)) {
            return empty();
        }
        Gated<List<EmailInfo>> result = tools.searchEmails(actor, query);
        if (result.allowed() && result.value() != null) {
            for (EmailInfo email : result.value()) {
                note(emails, email.emailId());
            }
        }
        calls.add(new ToolCall("searchEmails", args, result.decision().name(), false));
        return result;
    }

    public Gated<Document> getDocument(String documentId) {
        String args = actorArgs("documentId=" + documentId);
        if (blocked("getDocument", args)) {
            return empty();
        }
        if (!attachedToMailbox(documentId)) {
            calls.add(new ToolCall("getDocument", args, AccessDecision.ACCESS_DENIED.name(), false));
            return new Gated<>(AccessDecision.ACCESS_DENIED, null);
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

    private boolean attachedToMailbox(String documentId) {
        if (actor == null) {
            return false;
        }
        for (Email email : repo.emailsForMailbox(actor.mailboxId())) {
            for (EmailAttachment attachment : repo.attachmentsOf(email.id())) {
                if (documentId.equals(attachment.documentId())) {
                    return true;
                }
            }
        }
        return false;
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
