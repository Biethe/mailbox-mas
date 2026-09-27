package edu.aldermere.benchmark.review;

import edu.aldermere.benchmark.BenchmarkTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * One task from every non-empty category, then extra slots in proportion to category size.
 * The review seed only chooses which rows represent a category.
 */
public final class ReviewSampler {
    private ReviewSampler() {}

    public static List<BenchmarkTask> sample(Map<String, List<BenchmarkTask>> pools, int requested, long seed) {
        List<String> categories = new ArrayList<>();
        int population = 0;
        for (Map.Entry<String, List<BenchmarkTask>> entry : pools.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                categories.add(entry.getKey());
                population += entry.getValue().size();
            }
        }
        Collections.sort(categories);
        int target = Math.max(requested, categories.size());
        Map<String, Integer> quota = quotas(categories, pools, target, population);
        Random random = new Random(seed);
        List<BenchmarkTask> chosen = new ArrayList<>();
        for (String category : categories) {
            List<BenchmarkTask> copy = new ArrayList<>(pools.get(category));
            Collections.shuffle(copy, random);
            int take = Math.min(copy.size(), quota.get(category));
            chosen.addAll(copy.subList(0, take));
        }
        chosen.sort((left, right) -> {
            int byCategory = left.truth().category().compareTo(right.truth().category());
            if (byCategory != 0) {
                return byCategory;
            }
            return ReviewIds.stableKey(left).compareTo(ReviewIds.stableKey(right));
        });
        return chosen;
    }

    public static Map<String, Integer> quotas(List<String> categories, Map<String, List<BenchmarkTask>> pools,
                                              int target, int population) {
        Map<String, Integer> quota = new LinkedHashMap<>();
        for (String category : categories) {
            quota.put(category, 1);
        }
        int extra = target - categories.size();
        if (extra <= 0 || population == 0) {
            return quota;
        }
        int[] bonus = new int[categories.size()];
        double[] remainder = new double[categories.size()];
        int assigned = 0;
        for (int i = 0; i < categories.size(); i++) {
            int room = pools.get(categories.get(i)).size() - 1;
            double share = extra * (pools.get(categories.get(i)).size() / (double) population);
            int base = (int) Math.floor(share);
            if (base > room) {
                base = Math.max(0, room);
            }
            bonus[i] = base;
            assigned += base;
            remainder[i] = share - base;
        }
        while (assigned < extra) {
            int best = -1;
            for (int i = 0; i < categories.size(); i++) {
                int room = pools.get(categories.get(i)).size() - 1 - bonus[i];
                if (room <= 0) {
                    continue;
                }
                if (best < 0 || remainder[i] > remainder[best]) {
                    best = i;
                }
            }
            if (best < 0) {
                break;
            }
            bonus[best]++;
            assigned++;
            remainder[best] = -1;
        }
        for (int i = 0; i < categories.size(); i++) {
            quota.put(categories.get(i), 1 + bonus[i]);
        }
        return quota;
    }
}
