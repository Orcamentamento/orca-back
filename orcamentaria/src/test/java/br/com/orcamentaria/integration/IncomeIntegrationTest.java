package br.com.orcamentaria.integration;

import br.com.orcamentaria.dto.IncomeDTO;
import br.com.orcamentaria.model.Income;
import br.com.orcamentaria.repository.IncomeRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class IncomeIntegrationTest{

    @LocalServerPort
    private Integer port;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }
    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    @Autowired
    IncomeRepository repository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        repository.deleteAllInBatch();
    }

    @Test
    void insertCustomer() {
        IncomeDTO dto = new IncomeDTO(null, "salario", 1200., LocalDateTime.now());

        IncomeDTO response =
                given()
                    .contentType(ContentType.JSON)
                    .body(dto)
                .when()
                    .post("/incomes")
                .then()
                    .statusCode(201)
                    .extract().as(IncomeDTO.class);

        assertNotNull(response.getId());
        assertEquals(dto.getDescription(), response.getDescription());
        assertEquals(dto.getValue(), response.getValue());

        Income persisted = repository.findAll().getFirst();
        assertEquals(persisted.getId(), response.getId());
        assertEquals(dto.getDescription(), persisted.getDescription());
        assertEquals(dto.getValue(), persisted.getValue());
        assertTrue(persisted.isActive());
    }

}
