package edu.aldermere.university.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.aldermere.university.domain.Model.CanonicalScenario;
import edu.aldermere.university.domain.Model.Document;
import edu.aldermere.university.domain.Model.Email;
import edu.aldermere.university.domain.Model.EmailAttachment;
import edu.aldermere.university.domain.Model.EmailRecipient;
import edu.aldermere.university.domain.Model.WorldMeta;
import edu.aldermere.university.repository.WorldRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

public final class UniversityExporter {
    private final WorldRepository repo;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public UniversityExporter(WorldRepository repo) {
        this.repo = repo;
    }

    public void export(Path directory) throws IOException {
        Files.createDirectories(directory.resolve("entities"));
        writeLines(directory.resolve("entities/university.jsonl"), List.of(repo.university()));
        writeLines(directory.resolve("entities/world_meta.jsonl"), List.of(repo.meta()));
        writeLines(directory.resolve("entities/faculty.jsonl"), repo.faculties());
        writeLines(directory.resolve("entities/department.jsonl"), repo.departments());
        writeLines(directory.resolve("entities/academic_program.jsonl"), repo.programs());
        writeLines(directory.resolve("entities/administrative_unit.jsonl"), repo.adminUnits());
        writeLines(directory.resolve("entities/building.jsonl"), repo.buildings());
        writeLines(directory.resolve("entities/room.jsonl"), repo.rooms());
        writeLines(directory.resolve("entities/person.jsonl"), repo.people());
        writeLines(directory.resolve("entities/person_role.jsonl"), repo.roles());
        writeLines(directory.resolve("entities/administrator_assignment.jsonl"), repo.adminAssignments());
        writeLines(directory.resolve("entities/service_principal.jsonl"), repo.servicePrincipals());
        writeLines(directory.resolve("entities/mailbox.jsonl"), repo.mailboxes());
        writeLines(directory.resolve("entities/university_account.jsonl"), repo.accounts());
        writeLines(directory.resolve("entities/course.jsonl"), repo.courses());
        writeLines(directory.resolve("entities/course_instructor.jsonl"), repo.instructors());
        writeLines(directory.resolve("entities/enrollment.jsonl"), repo.enrollments());
        writeLines(directory.resolve("entities/assignment.jsonl"), repo.assignments());
        writeLines(directory.resolve("entities/exam.jsonl"), repo.exams());
        writeLines(directory.resolve("entities/research_project.jsonl"), repo.projects());
        writeLines(directory.resolve("entities/research_project_member.jsonl"), repo.members());
        writeLines(directory.resolve("entities/research_publication.jsonl"), repo.publications());
        writeLines(directory.resolve("entities/security_case.jsonl"), repo.securityCases());
        writeLines(directory.resolve("entities/document.jsonl"), repo.documents());
        writeLines(directory.resolve("entities/email_thread.jsonl"), repo.threads());
        writeLines(directory.resolve("entities/email.jsonl"), repo.emails());
        writeLines(directory.resolve("entities/email_recipient.jsonl"), repo.recipients());
        writeLines(directory.resolve("entities/email_attachment.jsonl"), repo.attachments());
        writeLines(directory.resolve("entities/event.jsonl"), repo.events());
        writeLines(directory.resolve("entities/event_participant.jsonl"), repo.participants());
        writeLines(directory.resolve("entities/permission_policy.jsonl"), repo.policies());
        writeLines(directory.resolve("entities/canonical_scenario.jsonl"), repo.scenarios());
        writeDocuments(directory.resolve("documents.jsonl"));
        writeEmails(directory.resolve("emails.jsonl"));
        writeScenarios(directory.resolve("scenarios.jsonl"));
        writeManifest(directory.resolve("manifest.json"));
    }

    /** Stable text of the authoritative rows. Manifest timestamps are not included. */
    public String canonicalSnapshot() throws IOException {
        Path temp = Files.createTempDirectory("university-snapshot");
        try {
            export(temp);
            StringBuilder out = new StringBuilder();
            try (var paths = Files.walk(temp.resolve("entities"))) {
                paths.filter(Files::isRegularFile).sorted().forEach(path -> {
                    try {
                        out.append(path.getFileName()).append('\n').append(Files.readString(path));
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
            }
            out.append("documents\n").append(Files.readString(temp.resolve("documents.jsonl")));
            out.append("emails\n").append(Files.readString(temp.resolve("emails.jsonl")));
            out.append("scenarios\n").append(Files.readString(temp.resolve("scenarios.jsonl")));
            return out.toString();
        } finally {
            try (var paths = Files.walk(temp)) {
                paths.sorted((a, b) -> b.compareTo(a)).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
            }
        }
    }

    private void writeManifest(Path path) throws IOException {
        WorldMeta meta = repo.meta();
        ObjectNode manifest = mapper.createObjectNode();
        manifest.put("seed", meta.seed());
        manifest.put("generatorVersion", meta.generatorVersion());
        manifest.put("schemaVersion", meta.schemaVersion());
        manifest.put("datasetProfile", meta.datasetProfile());
        manifest.put("generatedAt", Instant.now().toString());
        manifest.set("configuration", mapper.readTree(meta.configuration()));
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), manifest);
    }

    private void writeDocuments(Path path) throws IOException {
        StringBuilder out = new StringBuilder();
        for (Document document : repo.documents()) {
            ObjectNode node = mapper.createObjectNode();
            node.put("document_id", document.id());
            node.put("title", document.title());
            node.put("content", document.content());
            node.put("document_type", document.documentType());
            node.put("visibility", document.visibility());
            node.put("sensitivity", document.sensitivity());
            node.put("owner_id", document.ownerId());
            node.put("department_id", document.departmentId());
            node.put("course_id", document.courseId());
            node.put("project_id", document.projectId());
            node.put("admin_unit_id", document.adminUnitId());
            node.put("security_case_id", document.securityCaseId());
            node.put("security_kind", document.securityKind());
            out.append(mapper.writeValueAsString(node)).append('\n');
        }
        Files.writeString(path, out.toString());
    }

    private void writeEmails(Path path) throws IOException {
        StringBuilder out = new StringBuilder();
        for (Email email : repo.emails()) {
            ObjectNode node = mapper.createObjectNode();
            node.put("email_id", email.id());
            node.put("thread_id", email.threadId());
            node.put("sender_mailbox_id", email.senderMailboxId());
            var recipients = node.putArray("recipients");
            for (EmailRecipient recipient : repo.recipientsOf(email.id())) {
                recipients.addObject().put("mailbox_id", recipient.recipientMailboxId()).put("type", recipient.recipientType());
            }
            node.put("subject", email.subject());
            node.put("body", email.body());
            node.put("sent_at", email.sentAt().toString());
            var attachments = node.putArray("attachments");
            for (EmailAttachment attachment : repo.attachmentsOf(email.id())) {
                attachments.addObject().put("document_id", attachment.documentId()).put("filename", attachment.filename());
            }
            node.put("security_case_id", email.securityCaseId());
            node.put("security_kind", email.securityKind());
            out.append(mapper.writeValueAsString(node)).append('\n');
        }
        Files.writeString(path, out.toString());
    }

    private void writeScenarios(Path path) throws IOException {
        StringBuilder out = new StringBuilder();
        for (CanonicalScenario scenario : repo.scenarios()) {
            ObjectNode node = mapper.createObjectNode();
            node.put("scenario_id", scenario.id());
            node.put("scenario_type", scenario.scenarioType());
            node.put("user_id", scenario.userPersonId());
            node.put("question", scenario.question());
            node.set("expected_entities", mapper.readTree(scenario.expectedEntities()));
            node.set("expected_answer_facts", mapper.readTree(scenario.expectedFacts()));
            node.set("required_permissions", mapper.readTree(scenario.requiredPermissions()));
            node.put("security_case_id", scenario.securityCaseId());
            out.append(mapper.writeValueAsString(node)).append('\n');
        }
        Files.writeString(path, out.toString());
    }

    private void writeLines(Path path, List<?> rows) throws IOException {
        StringBuilder out = new StringBuilder();
        for (Object row : rows) {
            out.append(mapper.writeValueAsString(row)).append('\n');
        }
        Files.writeString(path, out.toString());
    }
}
