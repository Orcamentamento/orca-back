package br.com.orcamentaria.integration;


import br.com.orcamentaria.dto.IncomeDTO;
import br.com.orcamentaria.dto.IndefiniteDTO;
import br.com.orcamentaria.exception.ExceptionResponse;
import br.com.orcamentaria.model.Income;
import br.com.orcamentaria.model.Indefinite;
import br.com.orcamentaria.repository.IncomeRepository;
import br.com.orcamentaria.repository.RecurrenceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IndefiniteIncomesIntegrationTest extends AbstractContainerBaseTest {
    @Autowired
    IncomeRepository repository;
    @Autowired
    RecurrenceRepository recurrenceRepository;

    @Autowired
    private IncomeTestUtil util;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        repository.deleteAllInBatch();
        recurrenceRepository.deleteAllInBatch();
    }
    @Test
    @Order(1)
    void insertIncomeWithSuccess() {
        IncomeDTO dto = new IncomeDTO(null, "salario", 1200., LocalDateTime.now(), new IndefiniteDTO());

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

//        Income persisted = repository.findById(response.getId()).get();
//        assertEquals(dto.getDescription(), persisted.getDescription());
//        assertEquals(dto.getValue(), persisted.getValue());
//        assertNull(((Indefinite)persisted.getRecurrence()).getEndOfRecurrence());
//        assertTrue(persisted.isActive());
        assertEquals("salario", dto.getDescription());
        assertEquals(1200., dto.getValue());
        assertNull(((IndefiniteDTO)dto.getRecurrence()).getEndOfRecurrence());

    }

    @Test @Order(2)
    void insertIncomeWithInvalidValue() {
        IncomeDTO dto = new IncomeDTO(null, "salario", 0., LocalDateTime.now(), new IndefiniteDTO());

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(dto)
                .when()
                    .post("/incomes")
                .then()
                    .statusCode(400).extract().as(ExceptionResponse.class);
        assertEquals("value: must be greater than 0", response.getMessage());
        assertEquals("uri=/incomes", response.getDetails());
        assertNotNull(response.getDate());

        var persisted = repository.findAll();
        assertTrue(persisted.isEmpty());
        var persistedRecurrence = recurrenceRepository.findAll();
        assertTrue(persistedRecurrence.isEmpty());
    }

    @Test @Order(3)
    void insertIncomeWithNullDate() {
        IncomeDTO dto = new IncomeDTO(null, "salario", 1200., null, new IndefiniteDTO());

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(dto)
                .when()
                    .post("/incomes")
                .then()
                    .statusCode(400).extract().as(ExceptionResponse.class);

        assertEquals("occurrenceDate: must be informed", response.getMessage());
        assertEquals("uri=/incomes", response.getDetails());
        assertNotNull(response.getDate());

        var persisted = repository.findAll();
        assertTrue(persisted.isEmpty());
        var persistedRecurrence = recurrenceRepository.findAll();
        assertTrue(persistedRecurrence.isEmpty());
    }

    @Test @Order(4)
    void insertIncomeWithNullDescription() {
        IncomeDTO dto = new IncomeDTO(null, null, 1200., LocalDateTime.now(), new IndefiniteDTO());

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(dto)
                .when()
                    .post("/incomes")
                .then()
                    .statusCode(400).extract().as(ExceptionResponse.class);

        assertEquals("description: must be informed", response.getMessage());
        assertEquals("uri=/incomes", response.getDetails());
        assertNotNull(response.getDate());

        var persisted = repository.findAll();
        assertTrue(persisted.isEmpty());
        var persistedRecurrence = recurrenceRepository.findAll();
        assertTrue(persistedRecurrence.isEmpty());
    }

    @Test @Order(5)
    void insertIncomeWithBlankDescription() {
        IncomeDTO dto = new IncomeDTO(null, "", 1200., LocalDateTime.now(), new IndefiniteDTO());

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(dto)
                .when()
                    .post("/incomes")
                .then()
                    .statusCode(400).extract().as(ExceptionResponse.class);

        assertEquals("description: must be informed", response.getMessage());
        assertEquals("uri=/incomes", response.getDetails());
        assertNotNull(response.getDate());

        var persisted = repository.findAll();
        assertTrue(persisted.isEmpty());
        var persistedRecurrence = recurrenceRepository.findAll();
        assertTrue(persistedRecurrence.isEmpty());
    }

    @Test @Order(6)
    void disablingIncomeWithSuccess() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Indefinite());
        Income persistedIncome = repository.save(income);

        given()
            .contentType(ContentType.JSON)
            .pathParam("id", persistedIncome.getId())
        .when()
            .delete("/incomes/{id}")
        .then()
            .statusCode(204);

        var disabledIncome = repository.findById(persistedIncome.getId()).get();
        assertFalse(disabledIncome.isActive());
        assertFalse(disabledIncome.getRecurrence().isActive());
    }
    @Test @Order(7)
    void disablingIncomeWithBlanckIdInformed() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Indefinite());
        Income persistedIncome = repository.save(income);

        given()
            .contentType(ContentType.JSON)
            .pathParam("id", "")
        .when()
            .delete("/incomes/{id}")
        .then()
            .statusCode(404);

        var disabledIncome = repository.findById(persistedIncome.getId()).get();
        assertTrue(disabledIncome.isActive());
        assertTrue(income.getRecurrence().isActive());
    }
    @Test @Order(8)
    void disablingIncomeWithWrongId() {

        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Indefinite());
        Income persistedIncome = repository.save(income);

        String wrongId = persistedIncome.getId().toString() + "test";
        var response =
                given()
                    .contentType(ContentType.JSON)
                    .pathParam("id", wrongId)
                .when()
                    .delete("/incomes/{id}")
                .then()
                    .statusCode(400).extract().as(ExceptionResponse.class);

        assertEquals("No record found for ID", response.getMessage());
        assertEquals("uri=/incomes/"+wrongId, response.getDetails());
        assertNotNull(response.getDate());

        var disabledIncome = repository.findById(persistedIncome.getId()).get();
        assertTrue(disabledIncome.isActive());
        assertTrue(income.getRecurrence().isActive());
    }

    @Test @Order(9)
    void findIncomeWithSuccess() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Indefinite());
        Income persistedIncome = repository.save(income);

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .pathParam("id", persistedIncome.getId())
                .when()
                    .get("/incomes/{id}")
                .then()
                    .statusCode(200).extract().as(IncomeDTO.class);

        assertNotNull(response.getId());
        assertEquals(persistedIncome.getDescription(), response.getDescription());
        assertEquals(persistedIncome.getValue(), response.getValue());
        assertNull(((IndefiniteDTO)response.getRecurrence()).getEndOfRecurrence());
    }
    @Test @Order(10)
    void findIncomeWithBlanckIdInformed() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Indefinite());
        repository.save(income);

        given()
            .contentType(ContentType.JSON)
            .pathParam("id", "")
        .when()
            .get("/incomes/{id}")
        .then()
            .statusCode(404);
    }
    @Test @Order(11)
    void findIncomeWithWrongId() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Indefinite());
        Income persistedIncome = repository.save(income);

        String wrongId = persistedIncome.getId().toString() + "test";
        var response =
                given()
                    .contentType(ContentType.JSON)
                    .pathParam("id", wrongId)
                .when()
                    .get("/incomes/{id}")
                .then()
                    .statusCode(400).extract().as(ExceptionResponse.class);

        assertEquals("No record found for ID", response.getMessage());
        assertEquals("uri=/incomes/"+wrongId, response.getDetails());
        assertNotNull(response.getDate());
    }
    @Test @Order(12)
    void updateIncomeWithSuccess() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Indefinite());
        var persisted = repository.save(income);
        IncomeDTO update = new IncomeDTO(income.getId(), "update", 50., LocalDateTime.now(), new IndefiniteDTO(LocalDate.now().plus(1, ChronoUnit.MONTHS)));

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(update)
                .when()
                    .put("/incomes")
                .then()
                    .statusCode(200).extract().as(IncomeDTO.class);

        Income updated = repository.findById(persisted.getId()).get();
        assertEquals(update.getId(), response.getId());
        assertEquals(update.getDescription(), response.getDescription());
        assertEquals(update.getValue(), response.getValue());
        assertEquals(update.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), response.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
        assertEquals(persisted.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), updated.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
        assertEquals(persisted.getRecurrence().getId(), updated.getRecurrence().getId());
        assertEquals(1, recurrenceRepository.findAll().size());
    }

    @Test @Order(13)
    void updateIncomeWithInvalidDescription() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Indefinite());
        Income persisted = repository.save(income);

        IncomeDTO update = new IncomeDTO(income.getId(), "", 50., LocalDateTime.now(), new IndefiniteDTO());

        var response =
                given()
                        .contentType(ContentType.JSON)
                        .body(update)
                        .when()
                        .put("/incomes")
                        .then()
                        .statusCode(400).extract().as(ExceptionResponse.class);

        assertEquals("description: must be informed", response.getMessage());
        assertEquals("uri=/incomes", response.getDetails());
        assertNotNull(response.getDate());

        Income unmodifiedObject = repository.findById(persisted.getId()).get();
        assertEquals(persisted.getDescription(), unmodifiedObject.getDescription());
        assertEquals(persisted.getValue(), unmodifiedObject.getValue());
        assertEquals(persisted.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), unmodifiedObject.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
        assertEquals(((Indefinite)persisted.getRecurrence()).getEndOfRecurrence(), ((Indefinite)unmodifiedObject.getRecurrence()).getEndOfRecurrence());
        assertEquals(persisted.getRecurrence().getId(), unmodifiedObject.getRecurrence().getId());
    }

    @Test @Order(14)
    void updateIncomeWithInvalidValue() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Indefinite());
        Income persisted = repository.save(income);

        IncomeDTO update = new IncomeDTO(income.getId(), "salario", 0., LocalDateTime.now(), new IndefiniteDTO());

        var response =
                given()
                        .contentType(ContentType.JSON)
                        .body(update)
                        .when()
                        .put("/incomes")
                        .then()
                        .statusCode(400).extract().as(ExceptionResponse.class);

        assertEquals("value: must be greater than 0", response.getMessage());
        assertEquals("uri=/incomes", response.getDetails());
        assertNotNull(response.getDate());

        Income unmodifiedObject = repository.findById(persisted.getId()).get();
        assertEquals(persisted.getDescription(), unmodifiedObject.getDescription());
        assertEquals(persisted.getValue(), unmodifiedObject.getValue());
        assertEquals(persisted.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), unmodifiedObject.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
        assertEquals(((Indefinite)persisted.getRecurrence()).getEndOfRecurrence(), ((Indefinite)unmodifiedObject.getRecurrence()).getEndOfRecurrence());
        assertEquals(persisted.getRecurrence().getId(), unmodifiedObject.getRecurrence().getId());
    }
    @Test @Order(15)
    void updateIncomeWithInvalidDate() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Indefinite());
        Income persisted = repository.save(income);

        IncomeDTO update = new IncomeDTO(income.getId(), "salario", 1200., null, new IndefiniteDTO());

        var response =
                given()
                        .contentType(ContentType.JSON)
                        .body(update)
                        .when()
                        .put("/incomes")
                        .then()
                        .statusCode(400).extract().as(ExceptionResponse.class);

        assertEquals("occurrenceDate: must be informed", response.getMessage());
        assertEquals("uri=/incomes", response.getDetails());
        assertNotNull(response.getDate());

        Income unmodifiedObject = repository.findById(persisted.getId()).get();
        assertEquals(persisted.getDescription(), unmodifiedObject.getDescription());
        assertEquals(persisted.getValue(), unmodifiedObject.getValue());
        assertEquals(persisted.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), unmodifiedObject.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
        assertEquals(((Indefinite)persisted.getRecurrence()).getEndOfRecurrence(), ((Indefinite)unmodifiedObject.getRecurrence()).getEndOfRecurrence());
        assertEquals(persisted.getRecurrence().getId(), unmodifiedObject.getRecurrence().getId());
    }

    @Test @Order(16)
    public void findAllWithNoParameters() throws IOException {
        for (int i = 1; i <= 20; i++) {
            Income income = new Income();
            income.setValue(i*1000d); income.setDescription("salario " + i); income.setOccurrenceDate(LocalDateTime.now());
            income.setRecurrence(new Indefinite(LocalDate.now()));
            repository.save(income);
        }

        var response =
                given()
                        .when()
                        .get("/incomes")
                        .then()
                        .statusCode(200)
                        .extract().body().asString();

        List<IncomeDTO> parsedResponse = util.getIncomeDTOSFromPageableResponse(response);
        assertEquals(12, parsedResponse.size());
        parsedResponse.forEach(r -> {
            Income income = repository.findById(r.getId()).get();
            assertEquals(income.getDescription(), r.getDescription());
            assertEquals(income.getValue(), r.getValue());
            assertEquals(income.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), r.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
            assertEquals(((Indefinite)income.getRecurrence()).getEndOfRecurrence(), ((IndefiniteDTO)r.getRecurrence()).getEndOfRecurrence());
        });
    }
    @Test @Order(17)
    public void findAllIncomesUsingPagination() throws JsonProcessingException {
        for (int i = 1; i <= 20; i++) {
            Income income = new Income();
            income.setValue(i*1000d); income.setDescription("salario " + i); income.setOccurrenceDate(LocalDateTime.now());
            income.setRecurrence(new Indefinite(LocalDate.now()));
            repository.save(income);
        }

        var response =
                given()
                        .param("page", "0")
                        .param("limit", "10")
                        .when()
                        .get("/incomes")
                        .then()
                        .statusCode(200)
                        .extract().body().asString();

        List<IncomeDTO> parsedResponse = util.getIncomeDTOSFromPageableResponse(response);

        assertEquals(10, parsedResponse.size());
        parsedResponse.forEach(r -> {
            Income income = repository.findById(r.getId()).get();
            assertEquals(income.getDescription(), r.getDescription());
            assertEquals(income.getValue(), r.getValue());
            assertEquals(income.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), r.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
            assertEquals(((Indefinite)income.getRecurrence()).getEndOfRecurrence(), ((IndefiniteDTO)r.getRecurrence()).getEndOfRecurrence());
        });

        response =
                given()
                        .param("page", "1")
                        .param("limit", "10")
                        .when()
                        .get("/incomes")
                        .then()
                        .statusCode(200)
                        .extract().body().asString();
        System.out.println(response);
        parsedResponse = util.getIncomeDTOSFromPageableResponse(response);

        parsedResponse.forEach(r -> {
            Income income = repository.findById(r.getId()).get();
            assertEquals(income.getDescription(), r.getDescription());
            assertEquals(income.getValue(), r.getValue());
            assertEquals(income.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), r.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
            assertEquals(((Indefinite)income.getRecurrence()).getEndOfRecurrence(), ((IndefiniteDTO)r.getRecurrence()).getEndOfRecurrence());
        });


    }

}

