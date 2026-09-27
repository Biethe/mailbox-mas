CREATE TABLE university (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(80) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    location VARCHAR(200) NOT NULL,
    website VARCHAR(300) NOT NULL,
    email_domain VARCHAR(200) NOT NULL
);

CREATE TABLE world_meta (
    id VARCHAR(16) PRIMARY KEY,
    seed BIGINT NOT NULL,
    generator_version VARCHAR(32) NOT NULL,
    schema_version VARCHAR(32) NOT NULL,
    dataset_profile VARCHAR(64) NOT NULL,
    academic_year VARCHAR(32) NOT NULL,
    current_semester VARCHAR(16) NOT NULL,
    email_domain VARCHAR(200) NOT NULL,
    mas_email VARCHAR(200) NOT NULL,
    configuration CLOB NOT NULL
);

CREATE TABLE faculty (
    id VARCHAR(64) PRIMARY KEY,
    university_id VARCHAR(64) NOT NULL REFERENCES university(id),
    name VARCHAR(200) NOT NULL,
    description VARCHAR(4000) NOT NULL
);

CREATE TABLE department (
    id VARCHAR(64) PRIMARY KEY,
    faculty_id VARCHAR(64) NOT NULL REFERENCES faculty(id),
    name VARCHAR(200) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    code_prefix VARCHAR(8) NOT NULL
);

CREATE TABLE academic_program (
    id VARCHAR(64) PRIMARY KEY,
    department_id VARCHAR(64) NOT NULL REFERENCES department(id),
    name VARCHAR(200) NOT NULL,
    degree_level VARCHAR(32) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    CHECK (degree_level IN ('BACHELOR', 'MASTER', 'PHD'))
);

CREATE TABLE administrative_unit (
    id VARCHAR(64) PRIMARY KEY,
    university_id VARCHAR(64) NOT NULL REFERENCES university(id),
    name VARCHAR(200) NOT NULL,
    description VARCHAR(4000) NOT NULL
);

CREATE TABLE building (
    id VARCHAR(64) PRIMARY KEY,
    university_id VARCHAR(64) NOT NULL REFERENCES university(id),
    name VARCHAR(200) NOT NULL,
    address VARCHAR(300) NOT NULL
);

CREATE TABLE room (
    id VARCHAR(64) PRIMARY KEY,
    building_id VARCHAR(64) NOT NULL REFERENCES building(id),
    room_number VARCHAR(32) NOT NULL,
    room_type VARCHAR(32) NOT NULL,
    capacity INT NOT NULL,
    CHECK (room_type IN ('CLASSROOM', 'OFFICE', 'LABORATORY', 'MEETING_ROOM', 'LECTURE_HALL')),
    CHECK (capacity > 0),
    UNIQUE (building_id, room_number)
);

CREATE TABLE person (
    id VARCHAR(64) PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    display_name VARCHAR(160) NOT NULL,
    date_of_birth DATE NOT NULL,
    department_id VARCHAR(64) REFERENCES department(id),
    phone VARCHAR(40),
    office_id VARCHAR(64) REFERENCES room(id),
    status VARCHAR(32) NOT NULL,
    CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE person_role (
    person_id VARCHAR(64) NOT NULL REFERENCES person(id),
    role VARCHAR(32) NOT NULL,
    PRIMARY KEY (person_id, role),
    CHECK (role IN ('STUDENT', 'PROFESSOR', 'RESEARCHER', 'ADMINISTRATOR'))
);

CREATE TABLE administrator_assignment (
    person_id VARCHAR(64) NOT NULL REFERENCES person(id),
    unit_id VARCHAR(64) NOT NULL REFERENCES administrative_unit(id),
    PRIMARY KEY (person_id, unit_id)
);

CREATE TABLE service_principal (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(4000) NOT NULL
);

CREATE TABLE mailbox (
    id VARCHAR(64) PRIMARY KEY,
    owner_type VARCHAR(32) NOT NULL,
    owner_id VARCHAR(64) NOT NULL,
    email_address VARCHAR(200) NOT NULL UNIQUE,
    provider VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    CHECK (owner_type IN ('PERSON', 'UNIVERSITY_SERVICE')),
    CHECK (provider IN ('SYNTHETIC')),
    CHECK (status IN ('ACTIVE', 'UNAVAILABLE'))
);

CREATE TABLE university_account (
    id VARCHAR(64) PRIMARY KEY,
    person_id VARCHAR(64) REFERENCES person(id),
    service_principal_id VARCHAR(64) REFERENCES service_principal(id),
    username VARCHAR(80) NOT NULL UNIQUE,
    university_email VARCHAR(200) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    mailbox_id VARCHAR(64) NOT NULL UNIQUE REFERENCES mailbox(id),
    CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    CHECK (
        (person_id IS NOT NULL AND service_principal_id IS NULL)
        OR (person_id IS NULL AND service_principal_id IS NOT NULL)
    )
);

CREATE TABLE course (
    id VARCHAR(64) PRIMARY KEY,
    course_code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(300) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    department_id VARCHAR(64) NOT NULL REFERENCES department(id),
    credits INT NOT NULL,
    semester VARCHAR(16) NOT NULL,
    academic_year VARCHAR(16) NOT NULL,
    CHECK (credits > 0),
    CHECK (semester IN ('FALL', 'SPRING'))
);

CREATE TABLE course_instructor (
    course_id VARCHAR(64) NOT NULL REFERENCES course(id),
    person_id VARCHAR(64) NOT NULL REFERENCES person(id),
    role VARCHAR(32) NOT NULL,
    PRIMARY KEY (course_id, person_id),
    CHECK (role IN ('PRIMARY', 'ASSISTANT'))
);

CREATE TABLE enrollment (
    id VARCHAR(64) PRIMARY KEY,
    student_id VARCHAR(64) NOT NULL REFERENCES person(id),
    course_id VARCHAR(64) NOT NULL REFERENCES course(id),
    academic_year VARCHAR(16) NOT NULL,
    semester VARCHAR(16) NOT NULL,
    status VARCHAR(32) NOT NULL,
    enrollment_date DATE NOT NULL,
    final_grade VARCHAR(8),
    UNIQUE (student_id, course_id, academic_year, semester),
    CHECK (status IN ('ACTIVE', 'COMPLETED', 'DROPPED', 'FAILED'))
);

CREATE TABLE research_project (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    department_id VARCHAR(64) NOT NULL REFERENCES department(id),
    principal_investigator_id VARCHAR(64) NOT NULL REFERENCES person(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    CHECK (end_date > start_date)
);

CREATE TABLE research_project_member (
    project_id VARCHAR(64) NOT NULL REFERENCES research_project(id),
    person_id VARCHAR(64) NOT NULL REFERENCES person(id),
    role VARCHAR(32) NOT NULL,
    PRIMARY KEY (project_id, person_id),
    CHECK (role IN ('PI', 'RESEARCHER', 'ASSISTANT', 'STUDENT'))
);

CREATE TABLE research_publication (
    id VARCHAR(64) PRIMARY KEY,
    project_id VARCHAR(64) NOT NULL REFERENCES research_project(id),
    title VARCHAR(300) NOT NULL,
    abstract_text VARCHAR(4000) NOT NULL,
    pub_year INT NOT NULL,
    venue VARCHAR(200) NOT NULL,
    doi VARCHAR(200) NOT NULL
);

CREATE TABLE security_case (
    id VARCHAR(64) PRIMARY KEY,
    kind VARCHAR(64) NOT NULL,
    carrier VARCHAR(32) NOT NULL,
    carrier_id VARCHAR(64) NOT NULL,
    expected_handling VARCHAR(64) NOT NULL,
    note VARCHAR(2000) NOT NULL,
    CHECK (kind IN (
        'DIRECT_INJECTION', 'INDIRECT_INJECTION', 'FAKE_AUTHORIZATION',
        'MALICIOUS_TOOL', 'SYSTEM_IMPERSONATION', 'EXFILTRATION'
    )),
    CHECK (carrier IN ('EMAIL', 'DOCUMENT')),
    CHECK (expected_handling IN ('UNTRUSTED_CONTENT'))
);

CREATE TABLE document (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    document_type VARCHAR(64) NOT NULL,
    owner_id VARCHAR(64) NOT NULL REFERENCES person(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    content CLOB NOT NULL,
    mime_type VARCHAR(80) NOT NULL,
    sensitivity VARCHAR(32) NOT NULL,
    visibility VARCHAR(32) NOT NULL,
    course_id VARCHAR(64) REFERENCES course(id),
    project_id VARCHAR(64) REFERENCES research_project(id),
    department_id VARCHAR(64) REFERENCES department(id),
    admin_unit_id VARCHAR(64) REFERENCES administrative_unit(id),
    security_case_id VARCHAR(64) REFERENCES security_case(id),
    security_kind VARCHAR(64),
    CHECK (document_type IN (
        'COURSE_MATERIAL', 'COURSE_GUIDELINE', 'EXAM_DOCUMENT', 'RESEARCH_PAPER',
        'RESEARCH_REPORT', 'PROJECT_DOCUMENT', 'UNIVERSITY_POLICY',
        'ADMINISTRATIVE_DOCUMENT', 'MEETING_NOTES', 'MEMO', 'STUDENT_DOCUMENT'
    )),
    CHECK (visibility IN (
        'PUBLIC', 'UNIVERSITY', 'DEPARTMENT', 'COURSE', 'PROJECT',
        'ROLE_RESTRICTED', 'PRIVATE', 'CONFIDENTIAL'
    )),
    CHECK (sensitivity IN ('LOW', 'MODERATE', 'HIGH'))
);

CREATE TABLE assignment (
    id VARCHAR(64) PRIMARY KEY,
    course_id VARCHAR(64) NOT NULL REFERENCES course(id),
    title VARCHAR(300) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    deadline TIMESTAMP NOT NULL,
    document_id VARCHAR(64) REFERENCES document(id)
);

CREATE TABLE exam (
    id VARCHAR(64) PRIMARY KEY,
    course_id VARCHAR(64) NOT NULL REFERENCES course(id),
    title VARCHAR(300) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    exam_date TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL,
    location_id VARCHAR(64) NOT NULL REFERENCES room(id),
    status VARCHAR(32) NOT NULL,
    document_id VARCHAR(64) REFERENCES document(id),
    CHECK (duration_minutes > 0),
    CHECK (status IN ('SCHEDULED', 'CANCELLED', 'COMPLETED'))
);

CREATE TABLE email_thread (
    id VARCHAR(64) PRIMARY KEY,
    subject VARCHAR(300) NOT NULL
);

CREATE TABLE email (
    id VARCHAR(64) PRIMARY KEY,
    thread_id VARCHAR(64) NOT NULL REFERENCES email_thread(id),
    sender_mailbox_id VARCHAR(64) NOT NULL REFERENCES mailbox(id),
    subject VARCHAR(300) NOT NULL,
    body CLOB NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    priority VARCHAR(16) NOT NULL,
    is_read BOOLEAN NOT NULL,
    security_case_id VARCHAR(64) REFERENCES security_case(id),
    security_kind VARCHAR(64),
    CHECK (priority IN ('LOW', 'NORMAL', 'HIGH'))
);

CREATE TABLE email_recipient (
    email_id VARCHAR(64) NOT NULL REFERENCES email(id),
    recipient_mailbox_id VARCHAR(64) NOT NULL REFERENCES mailbox(id),
    recipient_type VARCHAR(8) NOT NULL,
    PRIMARY KEY (email_id, recipient_mailbox_id, recipient_type),
    CHECK (recipient_type IN ('TO', 'CC', 'BCC'))
);

CREATE TABLE email_attachment (
    id VARCHAR(64) PRIMARY KEY,
    email_id VARCHAR(64) NOT NULL REFERENCES email(id),
    document_id VARCHAR(64) NOT NULL REFERENCES document(id),
    filename VARCHAR(200) NOT NULL,
    mime_type VARCHAR(80) NOT NULL
);

CREATE TABLE event (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    location_id VARCHAR(64) NOT NULL REFERENCES room(id),
    organizer_id VARCHAR(64) NOT NULL REFERENCES person(id),
    course_id VARCHAR(64) REFERENCES course(id),
    project_id VARCHAR(64) REFERENCES research_project(id),
    exam_id VARCHAR(64) REFERENCES exam(id),
    CHECK (event_type IN (
        'LECTURE', 'EXAM', 'MEETING', 'CONFERENCE', 'DEFENSE', 'DEADLINE', 'ADMINISTRATIVE_EVENT'
    )),
    CHECK (end_time > start_time)
);

CREATE TABLE event_participant (
    event_id VARCHAR(64) NOT NULL REFERENCES event(id),
    person_id VARCHAR(64) NOT NULL REFERENCES person(id),
    role VARCHAR(32) NOT NULL,
    PRIMARY KEY (event_id, person_id),
    CHECK (role IN ('ORGANIZER', 'SPEAKER', 'ATTENDEE', 'CANDIDATE'))
);

CREATE TABLE permission_policy (
    id VARCHAR(64) PRIMARY KEY,
    subject_type VARCHAR(32) NOT NULL,
    subject_id VARCHAR(64) NOT NULL,
    action VARCHAR(16) NOT NULL,
    resource_type VARCHAR(64) NOT NULL,
    scope VARCHAR(32) NOT NULL,
    effect VARCHAR(8) NOT NULL,
    CHECK (subject_type IN ('ROLE', 'PERSON', 'ADMIN_UNIT')),
    CHECK (action IN ('READ', 'CREATE', 'UPDATE', 'DELETE', 'SHARE')),
    CHECK (resource_type IN (
        'PERSONAL_DATA', 'COURSE', 'COURSE_MATERIAL', 'ENROLLMENT', 'GRADE', 'EXAM',
        'RESEARCH_PROJECT', 'PUBLICATION', 'DOCUMENT', 'EMAIL', 'EVENT', 'ADMINISTRATIVE_RECORD'
    )),
    CHECK (scope IN ('OWN', 'ENROLLED', 'TEACHING', 'PROJECT_MEMBER', 'DEPARTMENT', 'UNIT', 'PUBLIC')),
    CHECK (effect IN ('ALLOW', 'DENY'))
);

CREATE TABLE canonical_scenario (
    id VARCHAR(64) PRIMARY KEY,
    scenario_type VARCHAR(64) NOT NULL,
    user_person_id VARCHAR(64) NOT NULL REFERENCES person(id),
    question VARCHAR(2000) NOT NULL,
    expected_entities CLOB NOT NULL,
    expected_facts CLOB NOT NULL,
    required_permissions CLOB NOT NULL,
    security_case_id VARCHAR(64) REFERENCES security_case(id)
);

CREATE INDEX idx_person_department ON person(department_id);
CREATE INDEX idx_enrollment_student ON enrollment(student_id);
CREATE INDEX idx_enrollment_course ON enrollment(course_id);
CREATE INDEX idx_email_sender ON email(sender_mailbox_id);
CREATE INDEX idx_email_thread ON email(thread_id);
CREATE INDEX idx_recipient_mailbox ON email_recipient(recipient_mailbox_id);
CREATE INDEX idx_document_course ON document(course_id);
CREATE INDEX idx_document_project ON document(project_id);
CREATE INDEX idx_event_course ON event(course_id);
