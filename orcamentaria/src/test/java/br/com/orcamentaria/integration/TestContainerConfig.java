package br.com.orcamentaria.integration;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainerConfig {
    private static final PostgreSQLContainer<?> postgresContainer;

    static {
        postgresContainer = new PostgreSQLContainer<>("postgres:latest").withReuse(true);
        postgresContainer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(postgresContainer::stop));
    }

    public static PostgreSQLContainer<?> getInstance() {
        return postgresContainer;
    }
}
