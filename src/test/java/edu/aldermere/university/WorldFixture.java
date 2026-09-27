package edu.aldermere.university;

import edu.aldermere.university.generator.Profile;
import edu.aldermere.university.generator.UniversityGenerator;
import edu.aldermere.university.persistence.Database;

import javax.sql.DataSource;
import java.nio.file.Path;

public final class WorldFixture implements AutoCloseable {
    public final WorldSession session;

    private WorldFixture(WorldSession session) {
        this.session = session;
    }

    public static WorldFixture generate(String name, long seed) throws Exception {
        DataSource dataSource = Database.open("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
        WorldSession session = WorldSession.open(dataSource);
        Profile profile = Profile.load(Path.of("config/profiles/small.json"));
        new UniversityGenerator(session.repo).generate(profile, seed);
        session.repo.commit();
        return new WorldFixture(session);
    }

    @Override
    public void close() {
        session.close();
    }
}
