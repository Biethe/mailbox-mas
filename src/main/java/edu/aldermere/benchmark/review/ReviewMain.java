package edu.aldermere.benchmark.review;

import edu.aldermere.benchmark.BenchmarkTask;
import edu.aldermere.benchmark.TaskGenerator;
import edu.aldermere.university.WorldSession;
import edu.aldermere.university.generator.Profile;
import edu.aldermere.university.generator.UniversityGenerator;
import edu.aldermere.university.persistence.Database;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReviewMain {
    private ReviewMain() {}

    public static void main(String[] args) throws Exception {
        long universitySeed = 42;
        long datasetSeed = 1;
        int sample = 16;
        int port = 8765;
        String universityProfile = "small";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--university-seed" -> universitySeed = Long.parseLong(args[++i]);
                case "--dataset-seed" -> datasetSeed = Long.parseLong(args[++i]);
                case "--sample" -> sample = Integer.parseInt(args[++i]);
                case "--port" -> port = Integer.parseInt(args[++i]);
                case "--university-profile" -> universityProfile = args[++i];
                default -> throw new IllegalArgumentException("unknown argument " + args[i]);
            }
        }
        String jdbc = "jdbc:h2:mem:review-" + universitySeed + "-" + datasetSeed + ";DB_CLOSE_DELAY=-1";
        WorldSession session = WorldSession.open(Database.open(jdbc));
        new UniversityGenerator(session.repo).generate(
                Profile.load(Path.of("config/profiles/" + universityProfile + ".json")), universitySeed);
        session.repo.commit();
        Map<String, List<BenchmarkTask>> pools = new TaskGenerator(session.repo).population();
        List<BenchmarkTask> chosen = ReviewSampler.sample(pools, sample, datasetSeed);
        Map<String, Integer> stratum = new LinkedHashMap<>();
        for (Map.Entry<String, List<BenchmarkTask>> entry : pools.entrySet()) {
            stratum.put(entry.getKey(), entry.getValue().size());
        }
        Path directory = Path.of("reviews", "u" + universitySeed + "-d" + datasetSeed);
        ReviewStore store = new ReviewStore(directory);
        ReviewCatalog catalog = new ReviewCatalog(session.repo, store, chosen, stratum);
        new ReviewServer(catalog, store).start(port);
        System.out.println("Review " + chosen.size() + " scenarios at http://127.0.0.1:" + port);
        System.out.println("Gold files: " + directory.toAbsolutePath());
        System.out.println("Every category is included. Extra slots follow category size. Seed " + datasetSeed + " picks the rows.");
        Thread.currentThread().join();
    }
}
