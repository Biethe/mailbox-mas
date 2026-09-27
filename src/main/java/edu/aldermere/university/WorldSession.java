package edu.aldermere.university;

import edu.aldermere.university.api.UniversityTools;
import edu.aldermere.university.persistence.Database;
import edu.aldermere.university.repository.JdbcWorldRepository;
import edu.aldermere.university.service.AccessControlService;
import edu.aldermere.university.service.SyntheticMailService;
import edu.aldermere.university.service.UniversityServices;

import javax.sql.DataSource;

public final class WorldSession implements AutoCloseable {
    public final JdbcWorldRepository repo;
    public final AccessControlService access;
    public final UniversityServices services;
    public final UniversityTools tools;
    public final SyntheticMailService mail;

    private WorldSession(JdbcWorldRepository repo, AccessControlService access, UniversityServices services,
                         UniversityTools tools, SyntheticMailService mail) {
        this.repo = repo;
        this.access = access;
        this.services = services;
        this.tools = tools;
        this.mail = mail;
    }

    public static WorldSession open(DataSource dataSource) {
        Database.migrate(dataSource);
        JdbcWorldRepository repo = new JdbcWorldRepository(dataSource);
        AccessControlService access = new AccessControlService(repo);
        UniversityServices services = new UniversityServices(repo, access);
        return new WorldSession(repo, access, services, new UniversityTools(services), new SyntheticMailService(repo));
    }

    @Override
    public void close() {
        repo.close();
    }
}
