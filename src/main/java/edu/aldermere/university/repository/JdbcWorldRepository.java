package edu.aldermere.university.repository;

import edu.aldermere.university.domain.Model.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JdbcWorldRepository implements WorldRepository {
    private final Connection conn;

    public JdbcWorldRepository(DataSource dataSource) {
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void commit() {
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void insertMeta(WorldMeta row) {
        exec("""
                INSERT INTO world_meta (id, seed, generator_version, schema_version, dataset_profile,
                    academic_year, current_semester, email_domain, mas_email, configuration)
                VALUES ('meta', ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, row.seed(), row.generatorVersion(), row.schemaVersion(), row.datasetProfile(),
                row.academicYear(), row.currentSemester(), row.emailDomain(), row.masEmail(), row.configuration());
    }

    @Override
    public void insertUniversity(University row) {
        exec("""
                INSERT INTO university (id, name, short_name, description, location, website, email_domain)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.name(), row.shortName(), row.description(), row.location(), row.website(), row.emailDomain());
    }

    @Override
    public void insertFaculty(Faculty row) {
        exec("INSERT INTO faculty (id, university_id, name, description) VALUES (?, ?, ?, ?)",
                row.id(), row.universityId(), row.name(), row.description());
    }

    @Override
    public void insertDepartment(Department row) {
        exec("INSERT INTO department (id, faculty_id, name, description, code_prefix) VALUES (?, ?, ?, ?, ?)",
                row.id(), row.facultyId(), row.name(), row.description(), row.codePrefix());
    }

    @Override
    public void insertProgram(AcademicProgram row) {
        exec("INSERT INTO academic_program (id, department_id, name, degree_level, description) VALUES (?, ?, ?, ?, ?)",
                row.id(), row.departmentId(), row.name(), row.degreeLevel(), row.description());
    }

    @Override
    public void insertAdminUnit(AdministrativeUnit row) {
        exec("INSERT INTO administrative_unit (id, university_id, name, description) VALUES (?, ?, ?, ?)",
                row.id(), row.universityId(), row.name(), row.description());
    }

    @Override
    public void insertBuilding(Building row) {
        exec("INSERT INTO building (id, university_id, name, address) VALUES (?, ?, ?, ?)",
                row.id(), row.universityId(), row.name(), row.address());
    }

    @Override
    public void insertRoom(Room row) {
        exec("INSERT INTO room (id, building_id, room_number, room_type, capacity) VALUES (?, ?, ?, ?, ?)",
                row.id(), row.buildingId(), row.roomNumber(), row.roomType(), row.capacity());
    }

    @Override
    public void insertPerson(Person row) {
        exec("""
                INSERT INTO person (id, first_name, last_name, display_name, date_of_birth, department_id, phone, office_id, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.firstName(), row.lastName(), row.displayName(), row.dateOfBirth(),
                row.departmentId(), row.phone(), row.officeId(), row.status());
    }

    @Override
    public void insertRole(PersonRole row) {
        exec("INSERT INTO person_role (person_id, role) VALUES (?, ?)", row.personId(), row.role());
    }

    @Override
    public void insertAdminAssignment(AdministratorAssignment row) {
        exec("INSERT INTO administrator_assignment (person_id, unit_id) VALUES (?, ?)", row.personId(), row.unitId());
    }

    @Override
    public void insertService(ServicePrincipal row) {
        exec("INSERT INTO service_principal (id, name, description) VALUES (?, ?, ?)", row.id(), row.name(), row.description());
    }

    @Override
    public void insertMailbox(Mailbox row) {
        exec("INSERT INTO mailbox (id, owner_type, owner_id, email_address, provider, status) VALUES (?, ?, ?, ?, ?, ?)",
                row.id(), row.ownerType(), row.ownerId(), row.emailAddress(), row.provider(), row.status());
    }

    @Override
    public void insertAccount(UniversityAccount row) {
        exec("""
                INSERT INTO university_account (id, person_id, service_principal_id, username, university_email, status, created_at, mailbox_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.personId(), row.servicePrincipalId(), row.username(), row.universityEmail(),
                row.status(), row.createdAt(), row.mailboxId());
    }

    @Override
    public void insertCourse(Course row) {
        exec("""
                INSERT INTO course (id, course_code, name, description, department_id, credits, semester, academic_year)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.courseCode(), row.name(), row.description(), row.departmentId(),
                row.credits(), row.semester(), row.academicYear());
    }

    @Override
    public void insertInstructor(CourseInstructor row) {
        exec("INSERT INTO course_instructor (course_id, person_id, role) VALUES (?, ?, ?)",
                row.courseId(), row.personId(), row.role());
    }

    @Override
    public void insertEnrollment(Enrollment row) {
        exec("""
                INSERT INTO enrollment (id, student_id, course_id, academic_year, semester, status, enrollment_date, final_grade)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.studentId(), row.courseId(), row.academicYear(), row.semester(),
                row.status(), row.enrollmentDate(), row.finalGrade());
    }

    @Override
    public void insertAssignment(Assignment row) {
        exec("INSERT INTO assignment (id, course_id, title, description, deadline, document_id) VALUES (?, ?, ?, ?, ?, ?)",
                row.id(), row.courseId(), row.title(), row.description(), row.deadline(), row.documentId());
    }

    @Override
    public void insertExam(Exam row) {
        exec("""
                INSERT INTO exam (id, course_id, title, description, exam_date, duration_minutes, location_id, status, document_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.courseId(), row.title(), row.description(), row.examDate(),
                row.durationMinutes(), row.locationId(), row.status(), row.documentId());
    }

    @Override
    public void insertProject(ResearchProject row) {
        exec("""
                INSERT INTO research_project (id, title, description, department_id, principal_investigator_id, start_date, end_date, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.title(), row.description(), row.departmentId(), row.principalInvestigatorId(),
                row.startDate(), row.endDate(), row.status());
    }

    @Override
    public void insertMember(ResearchProjectMember row) {
        exec("INSERT INTO research_project_member (project_id, person_id, role) VALUES (?, ?, ?)",
                row.projectId(), row.personId(), row.role());
    }

    @Override
    public void insertPublication(ResearchPublication row) {
        exec("""
                INSERT INTO research_publication (id, project_id, title, abstract_text, pub_year, venue, doi)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.projectId(), row.title(), row.abstractText(), row.year(), row.venue(), row.doi());
    }

    @Override
    public void insertSecurityCase(SecurityCase row) {
        exec("""
                INSERT INTO security_case (id, kind, carrier, carrier_id, expected_handling, note)
                VALUES (?, ?, ?, ?, ?, ?)
                """, row.id(), row.kind(), row.carrier(), row.carrierId(), row.expectedHandling(), row.note());
    }

    @Override
    public void insertDocument(Document row) {
        exec("""
                INSERT INTO document (id, title, document_type, owner_id, created_at, updated_at, content, mime_type,
                    sensitivity, visibility, course_id, project_id, department_id, admin_unit_id, security_case_id, security_kind)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.title(), row.documentType(), row.ownerId(), row.createdAt(), row.updatedAt(),
                row.content(), row.mimeType(), row.sensitivity(), row.visibility(), row.courseId(), row.projectId(),
                row.departmentId(), row.adminUnitId(), row.securityCaseId(), row.securityKind());
    }

    @Override
    public void insertThread(EmailThread row) {
        exec("INSERT INTO email_thread (id, subject) VALUES (?, ?)", row.id(), row.subject());
    }

    @Override
    public void insertEmail(Email row) {
        exec("""
                INSERT INTO email (id, thread_id, sender_mailbox_id, subject, body, sent_at, priority, is_read, security_case_id, security_kind)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.threadId(), row.senderMailboxId(), row.subject(), row.body(), row.sentAt(),
                row.priority(), row.messageRead(), row.securityCaseId(), row.securityKind());
    }

    @Override
    public void insertRecipient(EmailRecipient row) {
        exec("INSERT INTO email_recipient (email_id, recipient_mailbox_id, recipient_type) VALUES (?, ?, ?)",
                row.emailId(), row.recipientMailboxId(), row.recipientType());
    }

    @Override
    public void insertAttachment(EmailAttachment row) {
        exec("INSERT INTO email_attachment (id, email_id, document_id, filename, mime_type) VALUES (?, ?, ?, ?, ?)",
                row.id(), row.emailId(), row.documentId(), row.filename(), row.mimeType());
    }

    @Override
    public void insertEvent(Event row) {
        exec("""
                INSERT INTO event (id, title, description, event_type, start_time, end_time, location_id, organizer_id,
                    course_id, project_id, exam_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.title(), row.description(), row.eventType(), row.startTime(), row.endTime(),
                row.locationId(), row.organizerId(), row.courseId(), row.projectId(), row.examId());
    }

    @Override
    public void insertParticipant(EventParticipant row) {
        exec("INSERT INTO event_participant (event_id, person_id, role) VALUES (?, ?, ?)",
                row.eventId(), row.personId(), row.role());
    }

    @Override
    public void insertPolicy(PermissionPolicy row) {
        exec("""
                INSERT INTO permission_policy (id, subject_type, subject_id, action, resource_type, scope, effect)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.subjectType(), row.subjectId(), row.action(), row.resourceType(), row.scope(), row.effect());
    }

    @Override
    public void insertScenario(CanonicalScenario row) {
        exec("""
                INSERT INTO canonical_scenario (id, scenario_type, user_person_id, question, expected_entities,
                    expected_facts, required_permissions, security_case_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, row.id(), row.scenarioType(), row.userPersonId(), row.question(), row.expectedEntities(),
                row.expectedFacts(), row.requiredPermissions(), row.securityCaseId());
    }

    @Override
    public void updateAssignmentDocument(String assignmentId, String documentId) {
        exec("UPDATE assignment SET document_id = ? WHERE id = ?", documentId, assignmentId);
    }

    @Override
    public void updateExamDocument(String examId, String documentId) {
        exec("UPDATE exam SET document_id = ? WHERE id = ?", documentId, examId);
    }

    @Override
    public void deletePersonRole(String personId, String role) {
        exec("DELETE FROM person_role WHERE person_id = ? AND role = ?", personId, role);
    }

    @Override
    public void deletePolicy(String policyId) {
        exec("DELETE FROM permission_policy WHERE id = ?", policyId);
    }

    @Override
    public WorldMeta meta() {
        return one("SELECT * FROM world_meta WHERE id = 'meta'", this::meta);
    }

    @Override
    public University university() {
        return one("SELECT * FROM university ORDER BY id", this::university);
    }

    @Override public List<Faculty> faculties() { return query("SELECT * FROM faculty ORDER BY id", this::faculty); }
    @Override public List<Department> departments() { return query("SELECT * FROM department ORDER BY id", this::department); }
    @Override public List<AcademicProgram> programs() { return query("SELECT * FROM academic_program ORDER BY id", this::program); }
    @Override public List<AdministrativeUnit> adminUnits() { return query("SELECT * FROM administrative_unit ORDER BY id", this::adminUnit); }
    @Override public List<Building> buildings() { return query("SELECT * FROM building ORDER BY id", this::building); }
    @Override public List<Room> rooms() { return query("SELECT * FROM room ORDER BY id", this::room); }
    @Override public List<Person> people() { return query("SELECT * FROM person ORDER BY id", this::person); }
    @Override public List<PersonRole> roles() { return query("SELECT * FROM person_role ORDER BY person_id, role", this::role); }
    @Override public List<AdministratorAssignment> adminAssignments() { return query("SELECT * FROM administrator_assignment ORDER BY person_id, unit_id", this::adminAssignment); }
    @Override public List<ServicePrincipal> servicePrincipals() { return query("SELECT * FROM service_principal ORDER BY id", this::service); }
    @Override public List<Mailbox> mailboxes() { return query("SELECT * FROM mailbox ORDER BY id", this::mailbox); }
    @Override public List<UniversityAccount> accounts() { return query("SELECT * FROM university_account ORDER BY id", this::account); }
    @Override public List<Course> courses() { return query("SELECT * FROM course ORDER BY id", this::course); }
    @Override public List<CourseInstructor> instructors() { return query("SELECT * FROM course_instructor ORDER BY course_id, person_id", this::instructor); }
    @Override public List<Enrollment> enrollments() { return query("SELECT * FROM enrollment ORDER BY id", this::enrollment); }
    @Override public List<Assignment> assignments() { return query("SELECT * FROM assignment ORDER BY id", this::assignment); }
    @Override public List<Exam> exams() { return query("SELECT * FROM exam ORDER BY id", this::exam); }
    @Override public List<ResearchProject> projects() { return query("SELECT * FROM research_project ORDER BY id", this::project); }
    @Override public List<ResearchProjectMember> members() { return query("SELECT * FROM research_project_member ORDER BY project_id, person_id", this::member); }
    @Override public List<ResearchPublication> publications() { return query("SELECT * FROM research_publication ORDER BY id", this::publication); }
    @Override public List<SecurityCase> securityCases() { return query("SELECT * FROM security_case ORDER BY id", this::securityCase); }
    @Override public List<Document> documents() { return query("SELECT * FROM document ORDER BY id", this::document); }
    @Override public List<EmailThread> threads() { return query("SELECT * FROM email_thread ORDER BY id", this::thread); }
    @Override public List<Email> emails() { return query("SELECT * FROM email ORDER BY id", this::email); }
    @Override public List<EmailRecipient> recipients() { return query("SELECT * FROM email_recipient ORDER BY email_id, recipient_type, recipient_mailbox_id", this::recipient); }
    @Override public List<EmailAttachment> attachments() { return query("SELECT * FROM email_attachment ORDER BY id", this::attachment); }
    @Override public List<Event> events() { return query("SELECT * FROM event ORDER BY id", this::event); }
    @Override public List<EventParticipant> participants() { return query("SELECT * FROM event_participant ORDER BY event_id, person_id", this::participant); }
    @Override public List<PermissionPolicy> policies() { return query("SELECT * FROM permission_policy ORDER BY id", this::policy); }
    @Override public List<CanonicalScenario> scenarios() { return query("SELECT * FROM canonical_scenario ORDER BY id", this::scenario); }

    @Override public Optional<Person> findPerson(String id) { return optional("SELECT * FROM person WHERE id = ?", this::person, id); }
    @Override public Optional<Mailbox> findMailbox(String id) { return optional("SELECT * FROM mailbox WHERE id = ?", this::mailbox, id); }
    @Override public Optional<Mailbox> findMailboxByAddress(String email) { return optional("SELECT * FROM mailbox WHERE email_address = ?", this::mailbox, email); }
    @Override public Optional<Mailbox> findMailboxByOwner(String ownerType, String ownerId) {
        return optional("SELECT * FROM mailbox WHERE owner_type = ? AND owner_id = ? ORDER BY id LIMIT 1", this::mailbox, ownerType, ownerId);
    }
    @Override public Optional<UniversityAccount> findAccountByMailbox(String mailboxId) {
        return optional("SELECT * FROM university_account WHERE mailbox_id = ?", this::account, mailboxId);
    }
    @Override public Optional<Course> findCourse(String id) { return optional("SELECT * FROM course WHERE id = ?", this::course, id); }
    @Override public Optional<Document> findDocument(String id) { return optional("SELECT * FROM document WHERE id = ?", this::document, id); }
    @Override public Optional<Exam> findExam(String id) { return optional("SELECT * FROM exam WHERE id = ?", this::exam, id); }
    @Override public Optional<Room> findRoom(String id) { return optional("SELECT * FROM room WHERE id = ?", this::room, id); }
    @Override public Optional<Enrollment> findEnrollment(String id) { return optional("SELECT * FROM enrollment WHERE id = ?", this::enrollment, id); }
    @Override public Optional<Assignment> findAssignment(String id) { return optional("SELECT * FROM assignment WHERE id = ?", this::assignment, id); }
    @Override public Optional<ResearchProject> findProject(String id) { return optional("SELECT * FROM research_project WHERE id = ?", this::project, id); }
    @Override public Optional<Email> findEmail(String id) { return optional("SELECT * FROM email WHERE id = ?", this::email, id); }
    @Override public Optional<EmailThread> findThread(String id) { return optional("SELECT * FROM email_thread WHERE id = ?", this::thread, id); }
    @Override public Optional<Event> findEvent(String id) { return optional("SELECT * FROM event WHERE id = ?", this::event, id); }
    @Override public Optional<Building> findBuilding(String id) { return optional("SELECT * FROM building WHERE id = ?", this::building, id); }
    @Override public Optional<SecurityCase> findSecurityCase(String id) { return optional("SELECT * FROM security_case WHERE id = ?", this::securityCase, id); }

    @Override
    public List<String> rolesOf(String personId) {
        return query("SELECT role FROM person_role WHERE person_id = ? ORDER BY role", rs -> rs.getString(1), personId);
    }

    @Override
    public List<String> adminUnitsOf(String personId) {
        return query("SELECT unit_id FROM administrator_assignment WHERE person_id = ? ORDER BY unit_id", rs -> rs.getString(1), personId);
    }

    @Override public boolean hasRole(String personId, String role) { return scalar("SELECT COUNT(*) FROM person_role WHERE person_id = ? AND role = ?", personId, role) > 0; }
    @Override public boolean isEnrolled(String personId, String courseId) {
        return scalar("SELECT COUNT(*) FROM enrollment WHERE student_id = ? AND course_id = ? AND status IN ('ACTIVE', 'COMPLETED')", personId, courseId) > 0;
    }
    @Override public boolean isInstructor(String personId, String courseId) {
        return scalar("SELECT COUNT(*) FROM course_instructor WHERE person_id = ? AND course_id = ?", personId, courseId) > 0;
    }
    @Override public boolean isProjectMember(String personId, String projectId) {
        return scalar("SELECT COUNT(*) FROM research_project_member WHERE person_id = ? AND project_id = ?", personId, projectId) > 0;
    }
    @Override public boolean professorTeachesStudent(String professorId, String studentId) {
        return scalar("""
                SELECT COUNT(*) FROM enrollment e
                JOIN course_instructor ci ON ci.course_id = e.course_id
                WHERE ci.person_id = ? AND e.student_id = ? AND e.status = 'ACTIVE'
                """, professorId, studentId) > 0;
    }

    @Override
    public List<Course> coursesForStudent(String studentId, String year, String semester) {
        return query("""
                SELECT c.* FROM course c
                JOIN enrollment e ON e.course_id = c.id
                WHERE e.student_id = ? AND e.academic_year = ? AND e.semester = ? AND e.status = 'ACTIVE'
                ORDER BY c.id
                """, this::course, studentId, year, semester);
    }

    @Override
    public List<Person> studentsInCourse(String courseId) {
        return query("""
                SELECT p.* FROM person p
                JOIN enrollment e ON e.student_id = p.id
                WHERE e.course_id = ? AND e.status = 'ACTIVE'
                ORDER BY p.id
                """, this::person, courseId);
    }

    @Override
    public List<Course> coursesForProfessor(String personId) {
        return query("""
                SELECT c.* FROM course c
                JOIN course_instructor ci ON ci.course_id = c.id
                WHERE ci.person_id = ?
                ORDER BY c.id
                """, this::course, personId);
    }

    @Override
    public List<Exam> examsForCourse(String courseId) {
        return query("SELECT * FROM exam WHERE course_id = ? ORDER BY id", this::exam, courseId);
    }

    @Override
    public List<Assignment> assignmentsForCourse(String courseId) {
        return query("SELECT * FROM assignment WHERE course_id = ? ORDER BY id", this::assignment, courseId);
    }

    @Override
    public List<Email> emailsForMailbox(String mailboxId) {
        return query("""
                SELECT e.* FROM email e
                WHERE e.sender_mailbox_id = ?
                   OR EXISTS (SELECT 1 FROM email_recipient r WHERE r.email_id = e.id AND r.recipient_mailbox_id = ?)
                ORDER BY e.sent_at, e.id
                """, this::email, mailboxId, mailboxId);
    }

    @Override
    public List<Email> emailsInThread(String threadId) {
        return query("SELECT * FROM email WHERE thread_id = ? ORDER BY sent_at, id", this::email, threadId);
    }

    @Override
    public List<EmailRecipient> recipientsOf(String emailId) {
        return query("SELECT * FROM email_recipient WHERE email_id = ? ORDER BY recipient_type, recipient_mailbox_id", this::recipient, emailId);
    }

    @Override
    public List<EmailAttachment> attachmentsOf(String emailId) {
        return query("SELECT * FROM email_attachment WHERE email_id = ? ORDER BY id", this::attachment, emailId);
    }

    @Override
    public List<ResearchProjectMember> membersOf(String projectId) {
        return query("SELECT * FROM research_project_member WHERE project_id = ? ORDER BY person_id", this::member, projectId);
    }

    @Override
    public List<EventParticipant> participantsOf(String eventId) {
        return query("SELECT * FROM event_participant WHERE event_id = ? ORDER BY person_id", this::participant, eventId);
    }

    @Override
    public List<Enrollment> enrollmentsForStudent(String studentId) {
        return query("SELECT * FROM enrollment WHERE student_id = ? ORDER BY id", this::enrollment, studentId);
    }

    @Override
    public List<PermissionPolicy> policiesFor(String action, String resourceType) {
        return query("SELECT * FROM permission_policy WHERE action = ? AND resource_type = ? ORDER BY id", this::policy, action, resourceType);
    }

    @Override
    public Map<String, Integer> counts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("People", scalar("SELECT COUNT(*) FROM person"));
        counts.put("Accounts", scalar("SELECT COUNT(*) FROM university_account"));
        counts.put("Mailboxes", scalar("SELECT COUNT(*) FROM mailbox"));
        counts.put("Faculties", scalar("SELECT COUNT(*) FROM faculty"));
        counts.put("Departments", scalar("SELECT COUNT(*) FROM department"));
        counts.put("Programs", scalar("SELECT COUNT(*) FROM academic_program"));
        counts.put("Courses", scalar("SELECT COUNT(*) FROM course"));
        counts.put("Enrollments", scalar("SELECT COUNT(*) FROM enrollment"));
        counts.put("Research projects", scalar("SELECT COUNT(*) FROM research_project"));
        counts.put("Documents", scalar("SELECT COUNT(*) FROM document"));
        counts.put("Emails", scalar("SELECT COUNT(*) FROM email"));
        counts.put("Email threads", scalar("SELECT COUNT(*) FROM email_thread"));
        counts.put("Events", scalar("SELECT COUNT(*) FROM event"));
        counts.put("Buildings", scalar("SELECT COUNT(*) FROM building"));
        counts.put("Rooms", scalar("SELECT COUNT(*) FROM room"));
        return counts;
    }

    @Override
    public IntegrityFindings integrity() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        problem(errors, "person without account", """
                SELECT p.id FROM person p
                WHERE NOT EXISTS (SELECT 1 FROM university_account a WHERE a.person_id = p.id)
                """);
        problem(errors, "person without mailbox", """
                SELECT p.id FROM person p
                WHERE NOT EXISTS (SELECT 1 FROM mailbox m WHERE m.owner_type = 'PERSON' AND m.owner_id = p.id)
                """);
        problem(errors, "mailbox owner missing", """
                SELECT m.id FROM mailbox m
                WHERE (m.owner_type = 'PERSON' AND NOT EXISTS (SELECT 1 FROM person p WHERE p.id = m.owner_id))
                   OR (m.owner_type = 'UNIVERSITY_SERVICE' AND NOT EXISTS (SELECT 1 FROM service_principal s WHERE s.id = m.owner_id))
                """);
        problem(errors, "account mailbox mismatch", """
                SELECT a.id FROM university_account a
                JOIN mailbox m ON m.id = a.mailbox_id
                WHERE (a.person_id IS NOT NULL AND (m.owner_type <> 'PERSON' OR m.owner_id <> a.person_id))
                   OR (a.service_principal_id IS NOT NULL AND (m.owner_type <> 'UNIVERSITY_SERVICE' OR m.owner_id <> a.service_principal_id))
                   OR a.university_email <> m.email_address
                """);
        problem(errors, "missing MAS mailbox", "SELECT 'mas' WHERE NOT EXISTS (SELECT 1 FROM mailbox WHERE owner_type = 'UNIVERSITY_SERVICE')");
        problem(errors, "instructor without PROFESSOR role", """
                SELECT ci.person_id || ':' || ci.course_id FROM course_instructor ci
                WHERE NOT EXISTS (SELECT 1 FROM person_role pr WHERE pr.person_id = ci.person_id AND pr.role = 'PROFESSOR')
                """);
        problem(errors, "instructor outside course department", """
                SELECT ci.course_id || ':' || ci.person_id FROM course_instructor ci
                JOIN course c ON c.id = ci.course_id
                JOIN person p ON p.id = ci.person_id
                WHERE p.department_id IS NULL OR p.department_id <> c.department_id
                """);
        problem(errors, "enrollment without STUDENT role", """
                SELECT e.id FROM enrollment e
                WHERE NOT EXISTS (SELECT 1 FROM person_role pr WHERE pr.person_id = e.student_id AND pr.role = 'STUDENT')
                """);
        problem(errors, "enrollment semester mismatch", """
                SELECT e.id FROM enrollment e
                JOIN course c ON c.id = e.course_id
                WHERE e.academic_year <> c.academic_year OR e.semester <> c.semester
                """);
        problem(errors, "project PI missing suitable role", """
                SELECT rp.id FROM research_project rp
                WHERE NOT EXISTS (
                    SELECT 1 FROM person_role pr
                    WHERE pr.person_id = rp.principal_investigator_id AND pr.role IN ('PROFESSOR', 'RESEARCHER')
                )
                """);
        problem(errors, "project PI not a PI member", """
                SELECT rp.id FROM research_project rp
                WHERE NOT EXISTS (
                    SELECT 1 FROM research_project_member m
                    WHERE m.project_id = rp.id AND m.person_id = rp.principal_investigator_id AND m.role = 'PI'
                )
                """);
        problem(errors, "project member outside department", """
                SELECT m.project_id || ':' || m.person_id FROM research_project_member m
                JOIN research_project rp ON rp.id = m.project_id
                JOIN person p ON p.id = m.person_id
                WHERE p.department_id IS NULL OR p.department_id <> rp.department_id
                """);
        problem(errors, "PI outside project department", """
                SELECT rp.id FROM research_project rp
                JOIN person p ON p.id = rp.principal_investigator_id
                WHERE p.department_id IS NULL OR p.department_id <> rp.department_id
                """);
        problem(errors, "email without recipient", """
                SELECT e.id FROM email e
                WHERE NOT EXISTS (SELECT 1 FROM email_recipient r WHERE r.email_id = e.id)
                """);
        problem(errors, "empty thread", """
                SELECT t.id FROM email_thread t
                WHERE NOT EXISTS (SELECT 1 FROM email e WHERE e.thread_id = t.id)
                """);
        problem(errors, "security case carrier missing", """
                SELECT s.id FROM security_case s
                WHERE (s.carrier = 'EMAIL' AND NOT EXISTS (SELECT 1 FROM email e WHERE e.id = s.carrier_id))
                   OR (s.carrier = 'DOCUMENT' AND NOT EXISTS (SELECT 1 FROM document d WHERE d.id = s.carrier_id))
                """);
        problem(errors, "security label mismatch", """
                SELECT e.id FROM email e
                WHERE e.security_case_id IS NOT NULL AND NOT EXISTS (
                    SELECT 1 FROM security_case s
                    WHERE s.id = e.security_case_id AND s.carrier = 'EMAIL' AND s.carrier_id = e.id AND s.kind = e.security_kind
                )
                UNION ALL
                SELECT d.id FROM document d
                WHERE d.security_case_id IS NOT NULL AND NOT EXISTS (
                    SELECT 1 FROM security_case s
                    WHERE s.id = d.security_case_id AND s.carrier = 'DOCUMENT' AND s.carrier_id = d.id AND s.kind = d.security_kind
                )
                """);
        problem(errors, "document owner missing", "SELECT id FROM document WHERE owner_id NOT IN (SELECT id FROM person)");
        problem(errors, "empty document", "SELECT id FROM document WHERE content IS NULL OR CHAR_LENGTH(content) = 0");
        problem(errors, "empty email", "SELECT id FROM email WHERE body IS NULL OR CHAR_LENGTH(body) = 0");
        problem(errors, "office is not an office", """
                SELECT p.id FROM person p
                WHERE p.office_id IS NOT NULL AND NOT EXISTS (
                    SELECT 1 FROM room r WHERE r.id = p.office_id AND r.room_type = 'OFFICE'
                )
                """);
        problem(errors, "event time invalid", "SELECT id FROM event WHERE end_time <= start_time");
        problem(warnings, "exam room capacity below active enrollment", """
                SELECT ex.id FROM exam ex
                JOIN room r ON r.id = ex.location_id
                WHERE r.capacity < (
                    SELECT COUNT(*) FROM enrollment en WHERE en.course_id = ex.course_id AND en.status = 'ACTIVE'
                )
                """);
        return new IntegrityFindings(List.copyOf(errors), List.copyOf(warnings));
    }

    private void problem(List<String> into, String label, String sql) {
        for (String id : query(sql, rs -> rs.getString(1))) {
            into.add(label + ": " + id);
        }
    }

    private void exec(String sql, Object... args) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, args);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(sql, e);
        }
    }

    private int scalar(String sql, Object... args) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException(sql);
                }
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(sql, e);
        }
    }

    private <T> List<T> query(String sql, Row<T> row, Object... args) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(row.map(rs));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(sql, e);
        }
    }

    private <T> Optional<T> optional(String sql, Row<T> row, Object... args) {
        List<T> rows = query(sql, row, args);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    private <T> T one(String sql, Row<T> row, Object... args) {
        return optional(sql, row, args).orElseThrow(() -> new IllegalStateException("missing row: " + sql));
    }

    private static void bind(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            Object value = args[i];
            int index = i + 1;
            if (value == null) {
                ps.setNull(index, Types.VARCHAR);
            } else if (value instanceof LocalDateTime time) {
                ps.setTimestamp(index, Timestamp.valueOf(time));
            } else if (value instanceof LocalDate date) {
                ps.setDate(index, Date.valueOf(date));
            } else if (value instanceof Integer number) {
                ps.setInt(index, number);
            } else if (value instanceof Long number) {
                ps.setLong(index, number);
            } else if (value instanceof Boolean flag) {
                ps.setBoolean(index, flag);
            } else {
                ps.setString(index, value.toString());
            }
        }
    }

    private WorldMeta meta(ResultSet rs) throws SQLException {
        return new WorldMeta(rs.getLong("seed"), rs.getString("generator_version"), rs.getString("schema_version"),
                rs.getString("dataset_profile"), rs.getString("academic_year"), rs.getString("current_semester"),
                rs.getString("email_domain"), rs.getString("mas_email"), rs.getString("configuration"));
    }

    private University university(ResultSet rs) throws SQLException {
        return new University(rs.getString("id"), rs.getString("name"), rs.getString("short_name"), rs.getString("description"),
                rs.getString("location"), rs.getString("website"), rs.getString("email_domain"));
    }

    private Faculty faculty(ResultSet rs) throws SQLException {
        return new Faculty(rs.getString("id"), rs.getString("university_id"), rs.getString("name"), rs.getString("description"));
    }

    private Department department(ResultSet rs) throws SQLException {
        return new Department(rs.getString("id"), rs.getString("faculty_id"), rs.getString("name"), rs.getString("description"), rs.getString("code_prefix"));
    }

    private AcademicProgram program(ResultSet rs) throws SQLException {
        return new AcademicProgram(rs.getString("id"), rs.getString("department_id"), rs.getString("name"), rs.getString("degree_level"), rs.getString("description"));
    }

    private AdministrativeUnit adminUnit(ResultSet rs) throws SQLException {
        return new AdministrativeUnit(rs.getString("id"), rs.getString("university_id"), rs.getString("name"), rs.getString("description"));
    }

    private Building building(ResultSet rs) throws SQLException {
        return new Building(rs.getString("id"), rs.getString("university_id"), rs.getString("name"), rs.getString("address"));
    }

    private Room room(ResultSet rs) throws SQLException {
        return new Room(rs.getString("id"), rs.getString("building_id"), rs.getString("room_number"), rs.getString("room_type"), rs.getInt("capacity"));
    }

    private Person person(ResultSet rs) throws SQLException {
        return new Person(rs.getString("id"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("display_name"),
                date(rs, "date_of_birth"), rs.getString("department_id"), rs.getString("phone"), rs.getString("office_id"), rs.getString("status"));
    }

    private PersonRole role(ResultSet rs) throws SQLException {
        return new PersonRole(rs.getString("person_id"), rs.getString("role"));
    }

    private AdministratorAssignment adminAssignment(ResultSet rs) throws SQLException {
        return new AdministratorAssignment(rs.getString("person_id"), rs.getString("unit_id"));
    }

    private ServicePrincipal service(ResultSet rs) throws SQLException {
        return new ServicePrincipal(rs.getString("id"), rs.getString("name"), rs.getString("description"));
    }

    private Mailbox mailbox(ResultSet rs) throws SQLException {
        return new Mailbox(rs.getString("id"), rs.getString("owner_type"), rs.getString("owner_id"), rs.getString("email_address"),
                rs.getString("provider"), rs.getString("status"));
    }

    private UniversityAccount account(ResultSet rs) throws SQLException {
        return new UniversityAccount(rs.getString("id"), rs.getString("person_id"), rs.getString("service_principal_id"),
                rs.getString("username"), rs.getString("university_email"), rs.getString("status"),
                time(rs, "created_at"), rs.getString("mailbox_id"));
    }

    private Course course(ResultSet rs) throws SQLException {
        return new Course(rs.getString("id"), rs.getString("course_code"), rs.getString("name"), rs.getString("description"),
                rs.getString("department_id"), rs.getInt("credits"), rs.getString("semester"), rs.getString("academic_year"));
    }

    private CourseInstructor instructor(ResultSet rs) throws SQLException {
        return new CourseInstructor(rs.getString("course_id"), rs.getString("person_id"), rs.getString("role"));
    }

    private Enrollment enrollment(ResultSet rs) throws SQLException {
        return new Enrollment(rs.getString("id"), rs.getString("student_id"), rs.getString("course_id"), rs.getString("academic_year"),
                rs.getString("semester"), rs.getString("status"), date(rs, "enrollment_date"), rs.getString("final_grade"));
    }

    private Assignment assignment(ResultSet rs) throws SQLException {
        return new Assignment(rs.getString("id"), rs.getString("course_id"), rs.getString("title"), rs.getString("description"),
                time(rs, "deadline"), rs.getString("document_id"));
    }

    private Exam exam(ResultSet rs) throws SQLException {
        return new Exam(rs.getString("id"), rs.getString("course_id"), rs.getString("title"), rs.getString("description"),
                time(rs, "exam_date"), rs.getInt("duration_minutes"), rs.getString("location_id"), rs.getString("status"), rs.getString("document_id"));
    }

    private ResearchProject project(ResultSet rs) throws SQLException {
        return new ResearchProject(rs.getString("id"), rs.getString("title"), rs.getString("description"), rs.getString("department_id"),
                rs.getString("principal_investigator_id"), date(rs, "start_date"), date(rs, "end_date"), rs.getString("status"));
    }

    private ResearchProjectMember member(ResultSet rs) throws SQLException {
        return new ResearchProjectMember(rs.getString("project_id"), rs.getString("person_id"), rs.getString("role"));
    }

    private ResearchPublication publication(ResultSet rs) throws SQLException {
        return new ResearchPublication(rs.getString("id"), rs.getString("project_id"), rs.getString("title"), rs.getString("abstract_text"),
                rs.getInt("pub_year"), rs.getString("venue"), rs.getString("doi"));
    }

    private SecurityCase securityCase(ResultSet rs) throws SQLException {
        return new SecurityCase(rs.getString("id"), rs.getString("kind"), rs.getString("carrier"), rs.getString("carrier_id"),
                rs.getString("expected_handling"), rs.getString("note"));
    }

    private Document document(ResultSet rs) throws SQLException {
        return new Document(rs.getString("id"), rs.getString("title"), rs.getString("document_type"), rs.getString("owner_id"),
                time(rs, "created_at"), time(rs, "updated_at"), rs.getString("content"), rs.getString("mime_type"),
                rs.getString("sensitivity"), rs.getString("visibility"), rs.getString("course_id"), rs.getString("project_id"),
                rs.getString("department_id"), rs.getString("admin_unit_id"), rs.getString("security_case_id"), rs.getString("security_kind"));
    }

    private EmailThread thread(ResultSet rs) throws SQLException {
        return new EmailThread(rs.getString("id"), rs.getString("subject"));
    }

    private Email email(ResultSet rs) throws SQLException {
        return new Email(rs.getString("id"), rs.getString("thread_id"), rs.getString("sender_mailbox_id"), rs.getString("subject"),
                rs.getString("body"), time(rs, "sent_at"), rs.getString("priority"), rs.getBoolean("is_read"),
                rs.getString("security_case_id"), rs.getString("security_kind"));
    }

    private EmailRecipient recipient(ResultSet rs) throws SQLException {
        return new EmailRecipient(rs.getString("email_id"), rs.getString("recipient_mailbox_id"), rs.getString("recipient_type"));
    }

    private EmailAttachment attachment(ResultSet rs) throws SQLException {
        return new EmailAttachment(rs.getString("id"), rs.getString("email_id"), rs.getString("document_id"), rs.getString("filename"), rs.getString("mime_type"));
    }

    private Event event(ResultSet rs) throws SQLException {
        return new Event(rs.getString("id"), rs.getString("title"), rs.getString("description"), rs.getString("event_type"),
                time(rs, "start_time"), time(rs, "end_time"), rs.getString("location_id"), rs.getString("organizer_id"),
                rs.getString("course_id"), rs.getString("project_id"), rs.getString("exam_id"));
    }

    private EventParticipant participant(ResultSet rs) throws SQLException {
        return new EventParticipant(rs.getString("event_id"), rs.getString("person_id"), rs.getString("role"));
    }

    private PermissionPolicy policy(ResultSet rs) throws SQLException {
        return new PermissionPolicy(rs.getString("id"), rs.getString("subject_type"), rs.getString("subject_id"),
                rs.getString("action"), rs.getString("resource_type"), rs.getString("scope"), rs.getString("effect"));
    }

    private CanonicalScenario scenario(ResultSet rs) throws SQLException {
        return new CanonicalScenario(rs.getString("id"), rs.getString("scenario_type"), rs.getString("user_person_id"),
                rs.getString("question"), rs.getString("expected_entities"), rs.getString("expected_facts"),
                rs.getString("required_permissions"), rs.getString("security_case_id"));
    }

    private static LocalDate date(ResultSet rs, String column) throws SQLException {
        Date value = rs.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    private static LocalDateTime time(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    @FunctionalInterface
    private interface Row<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
