package edu.aldermere.university.cli;

import edu.aldermere.university.WorldSession;
import edu.aldermere.university.export.OverviewWriter;
import edu.aldermere.university.export.UniversityExporter;
import edu.aldermere.university.generator.Profile;
import edu.aldermere.university.generator.UniversityGenerator;
import edu.aldermere.university.persistence.Database;
import edu.aldermere.university.validation.UniversityValidator;
import edu.aldermere.university.validation.ValidationReport;

import java.nio.file.Path;
import java.util.Map;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        String command = System.getProperty("uni.command", "generate");
        long seed = 42;
        String profileName = "small";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--seed" -> seed = Long.parseLong(args[++i]);
                case "--profile" -> profileName = args[++i];
                case "generate", "validate", "export" -> command = args[i];
                default -> throw new IllegalArgumentException("unknown argument " + args[i]);
            }
        }
        int code = switch (command) {
            case "generate" -> generate(profileName, seed);
            case "validate" -> validate();
            case "export" -> export(profileName, seed);
            default -> throw new IllegalArgumentException(command);
        };
        if (code != 0) {
            System.exit(code);
        }
    }

    private static int generate(String profileName, long seed) throws Exception {
        Profile profile = Profile.load(Path.of("config/profiles/" + profileName + ".json"));
        Database.wipeFile();
        try (WorldSession session = WorldSession.open(Database.open(Database.FILE_URL))) {
            new UniversityGenerator(session.repo).generate(profile, seed);
            ValidationReport report = new UniversityValidator(session.repo, session.services).validate();
            Path exportDir = Path.of("exports", profile.name() + "-seed" + seed);
            new UniversityExporter(session.repo).export(exportDir);
            new OverviewWriter(session.repo).write(Path.of("docs"));
            session.repo.commit();
            printSummary(seed, profile.name(), session.repo.counts(), report, exportDir);
            return report.ok() ? 0 : 1;
        }
    }

    private static int validate() throws Exception {
        try (WorldSession session = WorldSession.open(Database.open(Database.FILE_URL))) {
            ValidationReport report = new UniversityValidator(session.repo, session.services).validate();
            printReport(report);
            return report.ok() ? 0 : 1;
        }
    }

    private static int export(String profileName, long seed) throws Exception {
        try (WorldSession session = WorldSession.open(Database.open(Database.FILE_URL))) {
            Path exportDir = Path.of("exports", profileName + "-seed" + seed);
            new UniversityExporter(session.repo).export(exportDir);
            new OverviewWriter(session.repo).write(Path.of("docs"));
            ValidationReport report = new UniversityValidator(session.repo, session.services).validate();
            printSummary(session.repo.meta().seed(), session.repo.meta().datasetProfile(), session.repo.counts(), report, exportDir);
            return report.ok() ? 0 : 1;
        }
    }

    private static void printSummary(long seed, String profile, Map<String, Integer> counts, ValidationReport report, Path exportDir) {
        System.out.println(report.ok() ? "University generated successfully." : "University generation failed validation.");
        System.out.println();
        System.out.println("Seed: " + seed);
        System.out.println("Profile: " + profile);
        System.out.println();
        System.out.println("Entities:");
        for (var entry : counts.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println();
        printReport(report);
        System.out.println();
        System.out.println("Exports:");
        System.out.println("  " + exportDir);
        System.out.println();
        System.out.println("Documentation:");
        System.out.println("  docs/university-overview.md");
        System.out.println("  docs/university-relationships.md");
    }

    private static void printReport(ValidationReport report) {
        System.out.println("Validation:");
        System.out.println("  Errors: " + report.errors().size());
        System.out.println("  Warnings: " + report.warnings().size());
        for (String error : report.errors()) {
            System.out.println("  error: " + error);
        }
        for (String warning : report.warnings()) {
            System.out.println("  warning: " + warning);
        }
    }
}
