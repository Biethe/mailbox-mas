package edu.aldermere.university.persistence;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Database {
    public static final String FILE_URL = "jdbc:h2:file:./data/university;DB_CLOSE_DELAY=-1";

    private Database() {}

    public static DataSource open(String jdbcUrl) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(jdbcUrl);
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    public static void migrate(DataSource dataSource) {
        Logger flyway = Logger.getLogger("org.flywaydb");
        Level previous = flyway.getLevel();
        flyway.setLevel(Level.SEVERE);
        Logger root = Logger.getLogger("");
        Handler[] handlers = root.getHandlers();
        Level[] handlerLevels = new Level[handlers.length];
        for (int i = 0; i < handlers.length; i++) {
            handlerLevels[i] = handlers[i].getLevel();
            handlers[i].setLevel(Level.SEVERE);
        }
        try {
            Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();
        } finally {
            flyway.setLevel(previous);
            for (int i = 0; i < handlers.length; i++) {
                handlers[i].setLevel(handlerLevels[i]);
            }
        }
    }

    /** Deletes the file database so the next migration starts empty. */
    public static void wipeFile() throws IOException {
        Files.createDirectories(Path.of("data"));
        for (String name : new String[] {
                "university.mv.db", "university.trace.db", "university.lock.db"
        }) {
            Files.deleteIfExists(Path.of("data", name));
        }
    }
}
