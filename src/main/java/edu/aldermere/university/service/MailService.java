package edu.aldermere.university.service;

import edu.aldermere.university.domain.Model.Email;
import edu.aldermere.university.domain.Model.EmailRecipient;

import java.time.LocalDateTime;
import java.util.List;

/** Mail operations a later Gmail or Microsoft adapter can implement. */
public interface MailService {
    Email send(String senderMailboxId, List<EmailRecipient> recipients, String subject, String body,
               String threadIdOrNull, LocalDateTime sentAt);

    List<Email> search(String actorMailboxId, String query);

    List<Email> thread(String actorMailboxId, String threadId);
}
