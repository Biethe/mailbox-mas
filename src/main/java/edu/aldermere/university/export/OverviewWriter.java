package edu.aldermere.university.export;

import edu.aldermere.university.domain.Model.AcademicProgram;
import edu.aldermere.university.domain.Model.Course;
import edu.aldermere.university.domain.Model.Department;
import edu.aldermere.university.domain.Model.Faculty;
import edu.aldermere.university.domain.Model.University;
import edu.aldermere.university.repository.WorldRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class OverviewWriter {
    private final WorldRepository repo;

    public OverviewWriter(WorldRepository repo) {
        this.repo = repo;
    }

    public void write(Path directory) throws IOException {
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("university-overview.md"), overview());
        Files.writeString(directory.resolve("university-relationships.md"), relationships());
    }

    private String overview() {
        University university = repo.university();
        Map<String, Integer> counts = repo.counts();
        StringBuilder out = new StringBuilder();
        out.append("# ").append(university.name()).append("\n\n");
        out.append(university.description()).append("\n\n");
        out.append("- Location: ").append(university.location()).append('\n');
        out.append("- Website: ").append(university.website()).append('\n');
        out.append("- Mail domain: ").append(university.emailDomain()).append('\n');
        out.append("- MAS mailbox: ").append(repo.meta().masEmail()).append("\n\n");
        out.append("## Counts\n\n");
        for (var entry : counts.entrySet()) {
            out.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append('\n');
        }
        out.append("\n## Organization\n\n");
        out.append("```mermaid\nflowchart TD\n");
        out.append("  ").append(node(university.id())).append("[\"").append(escape(university.shortName())).append("\"]\n");
        for (Faculty faculty : repo.faculties()) {
            out.append("  ").append(node(university.id())).append(" --> ").append(node(faculty.id()))
                    .append("[\"").append(escape(faculty.name())).append("\"]\n");
            for (Department department : repo.departments()) {
                if (!department.facultyId().equals(faculty.id())) {
                    continue;
                }
                int courseCount = 0;
                for (Course course : repo.courses()) {
                    if (course.departmentId().equals(department.id())) {
                        courseCount++;
                    }
                }
                out.append("  ").append(node(faculty.id())).append(" --> ").append(node(department.id()))
                        .append("[\"").append(escape(department.name() + " (" + courseCount + " courses)")).append("\"]\n");
                for (AcademicProgram program : repo.programs()) {
                    if (program.departmentId().equals(department.id())) {
                        out.append("  ").append(node(department.id())).append(" --> ").append(node(program.id()))
                                .append("[\"").append(escape(program.name())).append("\"]\n");
                    }
                }
            }
        }
        out.append("```\n\n");
        out.append("Courses, exams, offices, and events point at rooms in the generated campus. ");
        out.append("The chart above is built from the rows in this database.\n");
        return out.toString();
    }

    private String relationships() {
        University university = repo.university();
        Map<String, Integer> counts = repo.counts();
        StringBuilder out = new StringBuilder();
        out.append("# Relationships in ").append(university.name()).append("\n\n");
        out.append("The synthetic university is the world. A later multi-agent system, benchmark, and external mailbox provider stay outside it.\n\n");
        out.append("People act through a university account and a mailbox. ");
        out.append("The centralized assistant is a service principal with mailbox ")
                .append(repo.meta().masEmail())
                .append(". Its messages use that mailbox as the sender. ");
        out.append("Text inside an email is not proof of who sent it; the sender is the mailbox row.\n\n");
        out.append("Permissions are rows evaluated by `checkPermission`. ");
        out.append("There is no rule that an administrator can do everything. The default is deny.\n\n");
        out.append("Structured facts live in tables (enrollment, membership, exam time, room). ");
        out.append("Unstructured facts live in document text and email bodies, including the late-submission policy and labeled security fixtures. ");
        out.append("Security fixtures stay ordinary content. They are marked with a security case and the handling `UNTRUSTED_CONTENT`.\n\n");
        out.append("```mermaid\nflowchart TD\n");
        out.append("  Person[\"Person (").append(counts.get("People")).append(")\"]\n");
        out.append("  Account[\"UniversityAccount (").append(counts.get("Accounts")).append(")\"]\n");
        out.append("  Mailbox[\"Mailbox (").append(counts.get("Mailboxes")).append(")\"]\n");
        out.append("  Enrollment[\"Enrollment (").append(counts.get("Enrollments")).append(")\"]\n");
        out.append("  Course[\"Course (").append(counts.get("Courses")).append(")\"]\n");
        out.append("  Project[\"ResearchProject (").append(counts.get("Research projects")).append(")\"]\n");
        out.append("  Document[\"Document (").append(counts.get("Documents")).append(")\"]\n");
        out.append("  Email[\"Email (").append(counts.get("Emails")).append(")\"]\n");
        out.append("  Event[\"Event (").append(counts.get("Events")).append(")\"]\n");
        out.append("  Person --> Account --> Mailbox\n");
        out.append("  Person --> Enrollment --> Course\n");
        out.append("  Person --> Project\n");
        out.append("  Person --> Document\n");
        out.append("  Mailbox --> Email\n");
        out.append("  Person --> Event\n");
        out.append("```\n");
        return out.toString();
    }

    private static String node(String id) {
        return id.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static String escape(String label) {
        return label.replace("\"", "'");
    }
}
