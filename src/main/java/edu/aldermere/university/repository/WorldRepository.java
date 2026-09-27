package edu.aldermere.university.repository;

import edu.aldermere.university.domain.Model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WorldRepository extends AutoCloseable {
    void commit();

    void insertMeta(WorldMeta row);
    void insertUniversity(University row);
    void insertFaculty(Faculty row);
    void insertDepartment(Department row);
    void insertProgram(AcademicProgram row);
    void insertAdminUnit(AdministrativeUnit row);
    void insertBuilding(Building row);
    void insertRoom(Room row);
    void insertPerson(Person row);
    void insertRole(PersonRole row);
    void insertAdminAssignment(AdministratorAssignment row);
    void insertService(ServicePrincipal row);
    void insertMailbox(Mailbox row);
    void insertAccount(UniversityAccount row);
    void insertCourse(Course row);
    void insertInstructor(CourseInstructor row);
    void insertEnrollment(Enrollment row);
    void insertAssignment(Assignment row);
    void insertExam(Exam row);
    void insertProject(ResearchProject row);
    void insertMember(ResearchProjectMember row);
    void insertPublication(ResearchPublication row);
    void insertSecurityCase(SecurityCase row);
    void insertDocument(Document row);
    void insertThread(EmailThread row);
    void insertEmail(Email row);
    void insertRecipient(EmailRecipient row);
    void insertAttachment(EmailAttachment row);
    void insertEvent(Event row);
    void insertParticipant(EventParticipant row);
    void insertPolicy(PermissionPolicy row);
    void insertScenario(CanonicalScenario row);

    void updateAssignmentDocument(String assignmentId, String documentId);
    void updateExamDocument(String examId, String documentId);
    void deletePersonRole(String personId, String role);
    void deletePolicy(String policyId);

    WorldMeta meta();
    University university();

    List<Faculty> faculties();
    List<Department> departments();
    List<AcademicProgram> programs();
    List<AdministrativeUnit> adminUnits();
    List<Building> buildings();
    List<Room> rooms();
    List<Person> people();
    List<PersonRole> roles();
    List<AdministratorAssignment> adminAssignments();
    List<ServicePrincipal> servicePrincipals();
    List<Mailbox> mailboxes();
    List<UniversityAccount> accounts();
    List<Course> courses();
    List<CourseInstructor> instructors();
    List<Enrollment> enrollments();
    List<Assignment> assignments();
    List<Exam> exams();
    List<ResearchProject> projects();
    List<ResearchProjectMember> members();
    List<ResearchPublication> publications();
    List<SecurityCase> securityCases();
    List<Document> documents();
    List<EmailThread> threads();
    List<Email> emails();
    List<EmailRecipient> recipients();
    List<EmailAttachment> attachments();
    List<Event> events();
    List<EventParticipant> participants();
    List<PermissionPolicy> policies();
    List<CanonicalScenario> scenarios();

    Optional<Person> findPerson(String id);
    Optional<Mailbox> findMailbox(String id);
    Optional<Mailbox> findMailboxByAddress(String email);
    Optional<Mailbox> findMailboxByOwner(String ownerType, String ownerId);
    Optional<UniversityAccount> findAccountByMailbox(String mailboxId);
    Optional<Course> findCourse(String id);
    Optional<Document> findDocument(String id);
    Optional<Exam> findExam(String id);
    Optional<Room> findRoom(String id);
    Optional<Enrollment> findEnrollment(String id);
    Optional<Assignment> findAssignment(String id);
    Optional<ResearchProject> findProject(String id);
    Optional<Email> findEmail(String id);
    Optional<EmailThread> findThread(String id);
    Optional<Event> findEvent(String id);
    Optional<Building> findBuilding(String id);
    Optional<SecurityCase> findSecurityCase(String id);

    List<String> rolesOf(String personId);
    List<String> adminUnitsOf(String personId);
    boolean hasRole(String personId, String role);
    boolean isEnrolled(String personId, String courseId);
    boolean isInstructor(String personId, String courseId);
    boolean isProjectMember(String personId, String projectId);
    boolean professorTeachesStudent(String professorId, String studentId);

    List<Course> coursesForStudent(String studentId, String year, String semester);
    List<Person> studentsInCourse(String courseId);
    List<Course> coursesForProfessor(String personId);
    List<Exam> examsForCourse(String courseId);
    List<Assignment> assignmentsForCourse(String courseId);
    List<Email> emailsForMailbox(String mailboxId);
    List<Email> emailsInThread(String threadId);
    List<EmailRecipient> recipientsOf(String emailId);
    List<EmailAttachment> attachmentsOf(String emailId);
    List<ResearchProjectMember> membersOf(String projectId);
    List<EventParticipant> participantsOf(String eventId);
    List<Enrollment> enrollmentsForStudent(String studentId);
    List<PermissionPolicy> policiesFor(String action, String resourceType);

    Map<String, Integer> counts();
    IntegrityFindings integrity();
}
