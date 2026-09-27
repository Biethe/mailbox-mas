package edu.aldermere.university.api;

import edu.aldermere.university.api.Views.EmailInfo;
import edu.aldermere.university.api.Views.EventInfo;
import edu.aldermere.university.api.Views.ExamInfo;
import edu.aldermere.university.api.Views.Gated;
import edu.aldermere.university.api.Views.PersonInfo;
import edu.aldermere.university.api.Views.ProjectInfo;
import edu.aldermere.university.api.Views.ThreadInfo;
import edu.aldermere.university.domain.Model.AccessDecision;
import edu.aldermere.university.domain.Model.ActorContext;
import edu.aldermere.university.domain.Model.Course;
import edu.aldermere.university.domain.Model.Document;
import edu.aldermere.university.service.UniversityServices;

import java.util.List;

/** Tool-shaped methods for a future MCP adapter. Every call takes an authenticated actor. */
public final class UniversityTools {
    private final UniversityServices services;

    public UniversityTools(UniversityServices services) {
        this.services = services;
    }

    public ActorContext authenticate(String mailboxId) {
        return services.authenticate(mailboxId);
    }

    public Gated<List<PersonInfo>> searchPeople(ActorContext actor, String query) {
        return services.searchPeople(actor, query);
    }

    public Gated<List<Course>> searchCourses(ActorContext actor, String query) {
        return services.searchCourses(actor, query);
    }

    public Gated<Course> getCourse(ActorContext actor, String courseId) {
        return services.findCourse(actor, courseId);
    }

    public Gated<List<Course>> getStudentCourses(ActorContext actor, String studentId) {
        return services.findCoursesForStudent(actor, studentId);
    }

    public Gated<List<PersonInfo>> getCourseStudents(ActorContext actor, String courseId) {
        return services.findStudentsInCourse(actor, courseId);
    }

    public Gated<List<Document>> searchDocuments(ActorContext actor, String query) {
        return services.findDocuments(actor, query);
    }

    public Gated<Document> getDocument(ActorContext actor, String documentId) {
        return services.findDocument(actor, documentId);
    }

    public Gated<List<ProjectInfo>> searchResearchProjects(ActorContext actor, String query) {
        return services.findResearchProjects(actor, query);
    }

    public Gated<List<PersonInfo>> getProjectMembers(ActorContext actor, String projectId) {
        return services.findProjectMembers(actor, projectId);
    }

    public Gated<List<EmailInfo>> searchEmails(ActorContext actor, String query) {
        return services.findEmails(actor, query);
    }

    public Gated<ThreadInfo> getEmailThread(ActorContext actor, String threadId) {
        return services.getEmailThread(actor, threadId);
    }

    public Gated<List<EventInfo>> searchEvents(ActorContext actor, String query) {
        return services.findEvents(actor, query);
    }

    public Gated<List<ExamInfo>> examsForCourse(ActorContext actor, String courseId) {
        return services.findExamsForCourse(actor, courseId);
    }

    public AccessDecision checkAccess(ActorContext actor, String action, String resourceType, String resourceId) {
        return services.checkPermission(actor, action, resourceType, resourceId);
    }
}
