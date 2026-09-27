package edu.aldermere.benchmark;

import edu.aldermere.university.WorldSession;
import edu.aldermere.university.generator.Profile;
import edu.aldermere.university.generator.UniversityGenerator;
import edu.aldermere.university.persistence.Database;

import java.nio.file.Path;
import java.util.List;

public final class BenchmarkMain {
    private BenchmarkMain() {}

    public static void main(String[] args) throws Exception {
        String profileName = "smoke";
        String mas = "oracle";
        long universitySeed = 42;
        long datasetSeed = 1;
        String universityProfile = "small";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--profile" -> profileName = args[++i];
                case "--mas" -> mas = args[++i];
                case "--university-seed" -> universitySeed = Long.parseLong(args[++i]);
                case "--dataset-seed" -> datasetSeed = Long.parseLong(args[++i]);
                case "--university-profile" -> universityProfile = args[++i];
                default -> throw new IllegalArgumentException("unknown argument " + args[i]);
            }
        }
        BenchmarkProfile profile = BenchmarkProfile.load(Path.of("config/benchmark/" + profileName + ".json"));
        String jdbc = "jdbc:h2:mem:benchmark-" + profileName + "-" + universitySeed + ";DB_CLOSE_DELAY=-1";
        try (WorldSession session = WorldSession.open(Database.open(jdbc))) {
            new UniversityGenerator(session.repo).generate(Profile.load(Path.of("config/profiles/" + universityProfile + ".json")), universitySeed);
            session.repo.commit();
            List<BenchmarkTask> tasks = new TaskGenerator(session.repo).generate(profile, datasetSeed);
            List<BenchmarkRunner.Scored> scored = new BenchmarkRunner(session).run(tasks, Baselines.named(mas));
            Path out = Path.of("exports/benchmark", profileName + "-u" + universitySeed + "-d" + datasetSeed + "-" + mas);
            ReportWriter.write(out, mas, profile, universitySeed, datasetSeed, scored);
            System.out.println("Benchmark report written to " + out);
            System.out.println("Tasks: " + tasks.size());
            System.out.println("MAS: " + mas);
        }
    }
}
