package edu.aldermere.university.service;

import edu.aldermere.university.domain.Model.Email;
import edu.aldermere.university.domain.Model.EmailRecipient;
import edu.aldermere.university.domain.Model.EmailThread;
import edu.aldermere.university.domain.Model.Mailbox;
import edu.aldermere.university.repository.WorldRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SyntheticMailService implements MailService {
    private final WorldRepository repo;
    private int sequence = 1;

    public SyntheticMailService(WorldRepository repo) {
        this.repo = repo;
    }

    @Override
    public Email send(String senderMailboxId, List<EmailRecipient> recipients, String subject, String body,
                      String threadIdOrNull, LocalDateTime sentAt) {
        Mailbox sender = repo.findMailbox(senderMailboxId).orElseThrow(() -> new IllegalArgumentException("unknown sender mailbox"));
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("email needs a recipient");
        }
        String threadId = threadIdOrNull;
        if (threadId == null) {
            threadId = "thread-live-" + sequence;
            repo.insertThread(new EmailThread(threadId, subject));
        } else if (repo.findThread(threadId).isEmpty()) {
            throw new IllegalArgumentException("unknown thread");
        }
        String emailId = "email-live-" + sequence++;
        Email email = new Email(emailId, threadId, sender.id(), subject, body, sentAt, "NORMAL", false, null, null);
        repo.insertEmail(email);
        for (EmailRecipient recipient : recipients) {
            repo.findMailbox(recipient.recipientMailboxId()).orElseThrow(() -> new IllegalArgumentException("unknown recipient"));
            repo.insertRecipient(new EmailRecipient(emailId, recipient.recipientMailboxId(), recipient.recipientType()));
        }
        return email;
    }

    @Override
    public List<Email> search(String actorMailboxId, String query) {
        List<Email> found = new ArrayList<>();
        for (Email email : repo.emailsForMailbox(actorMailboxId)) {
            if (matches(email, query)) {
                found.add(email);
            }
        }
        return found;
    }

    @Override
    public List<Email> thread(String actorMailboxId, String threadId) {
        List<Email> visible = new ArrayList<>();
        for (Email email : repo.emailsInThread(threadId)) {
            if (participates(actorMailboxId, email)) {
                visible.add(email);
            }
        }
        return visible;
    }

    private boolean participates(String mailboxId, Email email) {
        if (email.senderMailboxId().equals(mailboxId)) {
            return true;
        }
        for (EmailRecipient recipient : repo.recipientsOf(email.id())) {
            if (recipient.recipientMailboxId().equals(mailboxId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(Email email, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String needle = query.toLowerCase(Locale.ROOT);
        return email.subject().toLowerCase(Locale.ROOT).contains(needle)
                || email.body().toLowerCase(Locale.ROOT).contains(needle);
    }
}
