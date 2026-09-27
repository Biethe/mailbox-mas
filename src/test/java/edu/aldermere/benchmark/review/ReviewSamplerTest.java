package edu.aldermere.benchmark.review;

import edu.aldermere.benchmark.BenchmarkTask;
import edu.aldermere.benchmark.TaskGenerator;
import edu.aldermere.university.WorldSession;
import edu.aldermere.university.generator.Profile;
import edu.aldermere.university.generator.UniversityGenerator;
import edu.aldermere.university.persistence.Database;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewSamplerTest {
    @Test
    void sampleCoversEveryCategoryAndStaysStable() throws Exception {
        try (WorldSession session = WorldSession.open(Database.open("jdbc:h2:mem:review-sample;DB_CLOSE_DELAY=-1"))) {
            new UniversityGenerator(session.repo).generate(Profile.load(Path.of("config/profiles/small.json")), 42);
            session.repo.commit();
            Map<String, List<BenchmarkTask>> pools = new TaskGenerator(session.repo).population();
            List<BenchmarkTask> first = ReviewSampler.sample(pools, 16, 1);
            List<BenchmarkTask> second = ReviewSampler.sample(pools, 16, 1);
            Set<String> categories = new HashSet<>();
            for (List<BenchmarkTask> pool : pools.values()) {
                if (!pool.isEmpty()) {
                    categories.add(pool.get(0).truth().category());
                }
            }
            Set<String> sampled = new HashSet<>();
            for (BenchmarkTask task : first) {
                sampled.add(task.truth().category());
            }
            assertEquals(categories, sampled);
            assertTrue(first.size() >= categories.size());
            assertEquals(keys(first), keys(second));
        }
    }

    @Test
    void goldRoundTrip() throws Exception {
        Path directory = Files.createTempDirectory("review-gold");
        ReviewStore store = new ReviewStore(directory);
        ReviewGold gold = new ReviewGold("UNIVERSITY_SIMPLE-abc", "key", "UNIVERSITY_SIMPLE",
                "You are enrolled in CS100.", "", null, List.of(), "tightened the wording", "reviewed", "2026-09-27T00:00:00Z");
        store.save(gold);
        ReviewGold loaded = store.load("UNIVERSITY_SIMPLE-abc").orElseThrow();
        assertEquals("You are enrolled in CS100.", loaded.response());
        assertEquals("tightened the wording", loaded.notes());
    }

    private static List<String> keys(List<BenchmarkTask> tasks) {
        return tasks.stream().map(ReviewIds::stableKey).toList();
    }
}
