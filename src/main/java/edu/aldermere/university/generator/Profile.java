package edu.aldermere.university.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record Profile(
        String name,
        String rawJson,
        UniversitySpec university,
        CalendarSpec calendar,
        Counts counts
) {
    public record UniversitySpec(String name, String shortName, String description, String location,
                                 String website, String emailDomain, String masLocalPart) {
        public String masEmail() {
            return masLocalPart + "@" + emailDomain;
        }
    }

    public record CalendarSpec(String academicYear, String currentSemester, String epoch) {}

    public record Counts(
            int faculties, int departments, int programs, int buildings, int people,
            int students, int professors, int researchers, int administrators, int professorResearchers,
            int courses, int researchProjects, int documents, int emails, int events
    ) {}

    public static Profile load(Path path) throws IOException {
        String raw = Files.readString(path);
        JsonNode root = new ObjectMapper().readTree(raw);
        JsonNode u = root.get("university");
        JsonNode c = root.get("calendar");
        JsonNode n = root.get("counts");
        Profile profile = new Profile(
                root.get("name").asText(),
                raw.strip() + "\n",
                new UniversitySpec(
                        u.get("name").asText(),
                        u.get("shortName").asText(),
                        u.get("description").asText(),
                        u.get("location").asText(),
                        u.get("website").asText(),
                        u.get("emailDomain").asText(),
                        u.get("masLocalPart").asText()
                ),
                new CalendarSpec(
                        c.get("academicYear").asText(),
                        c.get("currentSemester").asText(),
                        c.get("epoch").asText()
                ),
                new Counts(
                        n.get("faculties").asInt(),
                        n.get("departments").asInt(),
                        n.get("programs").asInt(),
                        n.get("buildings").asInt(),
                        n.get("people").asInt(),
                        n.get("students").asInt(),
                        n.get("professors").asInt(),
                        n.get("researchers").asInt(),
                        n.get("administrators").asInt(),
                        n.get("professorResearchers").asInt(),
                        n.get("courses").asInt(),
                        n.get("researchProjects").asInt(),
                        n.get("documents").asInt(),
                        n.get("emails").asInt(),
                        n.get("events").asInt()
                )
        );
        profile.validate();
        return profile;
    }

    public void validate() {
        Counts n = counts;
        int sum = n.students() + n.professors() + n.researchers() + n.administrators();
        if (sum != n.people()) {
            throw new IllegalArgumentException("role counts " + sum + " must equal people " + n.people());
        }
        require(n.faculties() >= 1, "faculties");
        require(n.departments() >= n.faculties(), "departments must be at least the faculty count");
        require(n.programs() >= 1, "programs");
        require(n.buildings() >= 1, "buildings");
        require(n.professors() >= n.departments(), "professors must cover every department");
        require(n.students() >= n.departments(), "students must cover every department");
        require(n.students() >= 2, "students");
        require(n.researchers() >= 1, "researchers");
        require(n.administrators() >= 1, "administrators");
        require(n.professorResearchers() >= 0 && n.professorResearchers() <= n.professors(), "professorResearchers");
        require(n.courses() >= n.departments(), "courses must cover every department");
        require(n.researchProjects() >= 1, "researchProjects");
        require(n.documents() >= 1, "documents");
        require(n.emails() >= 12, "emails");
        require(n.events() >= 2, "events");
    }

    private static void require(boolean ok, String message) {
        if (!ok) {
            throw new IllegalArgumentException(message);
        }
    }
}
