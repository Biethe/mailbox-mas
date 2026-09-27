package edu.aldermere.benchmark.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class ReviewStore {
    private final Path directory;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    public ReviewStore(Path directory) {
        this.directory = directory;
    }

    public Path directory() {
        return directory;
    }

    public Optional<ReviewGold> load(String fileId) throws IOException {
        Path path = directory.resolve(fileId + ".json");
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        return Optional.of(mapper.readValue(path.toFile(), ReviewGold.class));
    }

    public void save(ReviewGold gold) throws IOException {
        Files.createDirectories(directory);
        mapper.writeValue(directory.resolve(gold.fileId() + ".json").toFile(), gold);
    }
}
