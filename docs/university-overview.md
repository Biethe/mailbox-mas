# Aldermere University

A fictional university used as a controlled world for multi-agent experiments.

- Location: Aldermere, Northshore
- Website: https://www.aldermere.edu
- Mail domain: aldermere.edu
- MAS mailbox: assistant@aldermere.edu

## Counts

- People: 50
- Accounts: 51
- Mailboxes: 51
- Faculties: 3
- Departments: 6
- Programs: 8
- Courses: 20
- Enrollments: 94
- Research projects: 10
- Documents: 100
- Emails: 500
- Email threads: 204
- Events: 50
- Buildings: 3
- Rooms: 30

## Organization

```mermaid
flowchart TD
  university_00001["Aldermere"]
  university_00001 --> faculty_00001["Faculty of Science"]
  faculty_00001 --> department_00001["Computer Science (4 courses)"]
  department_00001 --> program_00001["BSc Computer Science"]
  department_00001 --> program_00007["MSc Computer Science"]
  faculty_00001 --> department_00004["Mathematics (3 courses)"]
  department_00004 --> program_00004["BSc Mathematics"]
  university_00001 --> faculty_00002["Faculty of Engineering"]
  faculty_00002 --> department_00002["Electrical Engineering (4 courses)"]
  department_00002 --> program_00002["BSc Electrical Engineering"]
  department_00002 --> program_00008["MSc Electrical Engineering"]
  faculty_00002 --> department_00005["Mechanical Engineering (3 courses)"]
  department_00005 --> program_00005["BSc Mechanical Engineering"]
  university_00001 --> faculty_00003["Faculty of Humanities"]
  faculty_00003 --> department_00003["History (3 courses)"]
  department_00003 --> program_00003["BSc History"]
  faculty_00003 --> department_00006["Linguistics (3 courses)"]
  department_00006 --> program_00006["BSc Linguistics"]
```

Courses, exams, offices, and events point at rooms in the generated campus. The chart above is built from the rows in this database.
