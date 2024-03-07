package br.com.orcamentaria.integration;


import br.com.orcamentaria.dto.IncomeDTO;
import br.com.orcamentaria.dto.InstallmentsDTO;
import br.com.orcamentaria.exception.ExceptionResponse;
import br.com.orcamentaria.model.Income;
import br.com.orcamentaria.model.Installments;
import br.com.orcamentaria.repository.IncomeRepository;
import br.com.orcamentaria.repository.RecurrenceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InstallmentIncomesIntegrationTest extends AbstractContainerBaseTest {
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
    @Test @Order(1)
    void insertIncomeWithSuccess() {
        IncomeDTO dto = new IncomeDTO(null, "salario", 1200., LocalDateTime.now(), new InstallmentsDTO());

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

        Income persisted = repository.findById(response.getId()).get();
        assertEquals(dto.getDescription(), persisted.getDescription());
        assertEquals(dto.getValue(), persisted.getValue());
        assertEquals(1, ((Installments)persisted.getRecurrence()).getQuantity());
        assertTrue(persisted.isActive());
    }

    @Test @Order(2)
    void insertIncomeWithInvalidValue() {
        IncomeDTO dto = new IncomeDTO(null, "salario", 0., LocalDateTime.now(), new InstallmentsDTO());

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
        IncomeDTO dto = new IncomeDTO(null, "salario", 1200., null, new InstallmentsDTO());

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
        IncomeDTO dto = new IncomeDTO(null, null, 1200., LocalDateTime.now(), new InstallmentsDTO());

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
        IncomeDTO dto = new IncomeDTO(null, "", 1200., LocalDateTime.now(), new InstallmentsDTO());

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
        income.setRecurrence(new Installments(10));
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
        income.setRecurrence(new Installments(10));
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
        income.setRecurrence(new Installments(10));
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
        income.setRecurrence(new Installments(10));
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
        assertEquals(10, ((InstallmentsDTO)response.getRecurrence()).getQuantity());
    }
    @Test @Order(10)
    void findIncomeWithBlanckIdInformed() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Installments(10));
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
        income.setRecurrence(new Installments(10));
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
        income.setRecurrence(new Installments(10));
        Income saved = repository.save(income);

        IncomeDTO update = new IncomeDTO(income.getId(), "update", 50., LocalDateTime.now(), new InstallmentsDTO(5));

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(update)
                .when()
                        .put("/incomes")
                .then()
                    .statusCode(200).extract().as(IncomeDTO.class);

        Income updated = repository.findById(response.getId()).get();

        assertEquals(update.getId(), response.getId());
        assertEquals(update.getDescription(), response.getDescription());
        assertEquals(update.getValue(), response.getValue());
        assertEquals(update.getOccurrenceDate(), response.getOccurrenceDate());
        assertEquals(((InstallmentsDTO)update.getRecurrence()).getQuantity(), ((InstallmentsDTO)response.getRecurrence()).getQuantity());
        assertEquals(saved.getRecurrence().getId(), updated.getRecurrence().getId());
        assertEquals(1, recurrenceRepository.findAll().size());
    }

    @Test @Order(13)
    void updateIncomeWithInvalidRecurrence() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Installments(10));
        var unmodifiedObject = repository.save(income);

        IncomeDTO update = new IncomeDTO(income.getId(), "update", 50., LocalDateTime.now(), new InstallmentsDTO(0));

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(update)
                .when()
                    .put("/incomes")
                .then()
                    .statusCode(400).extract().as(ExceptionResponse.class);

        assertEquals("quantity: must be at least 1", response.getMessage());
        assertEquals("uri=/incomes", response.getDetails());
        assertNotNull(response.getDate());

        var saved = repository.findById(update.getId()).get();
        assertEquals(10, ((Installments)saved.getRecurrence()).getQuantity());
        assertEquals(unmodifiedObject.getRecurrence().getId(), saved.getRecurrence().getId());
        assertEquals(1200., saved.getValue());
        assertEquals("salario", saved.getDescription());
        assertEquals("salario", saved.getDescription());
        assertEquals(unmodifiedObject.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), saved.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test @Order(14)
    void updateIncomeWithInvalidDescription() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Installments(10));
        Income persisted = repository.save(income);

        IncomeDTO update = new IncomeDTO(income.getId(), "", 50., LocalDateTime.now(), new InstallmentsDTO(10));

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
        assertEquals(((Installments)persisted.getRecurrence()).getQuantity(), ((Installments)unmodifiedObject.getRecurrence()).getQuantity());
        assertEquals(((Installments)persisted.getRecurrence()).getId(), ((Installments)unmodifiedObject.getRecurrence()).getId());
    }

    @Test @Order(15)
    void updateIncomeWithInvalidValue() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Installments(10));
        Income persisted = repository.save(income);

        IncomeDTO update = new IncomeDTO(income.getId(), "salario", 0., LocalDateTime.now(), new InstallmentsDTO(10));

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
        assertEquals(((Installments)persisted.getRecurrence()).getQuantity(), ((Installments)unmodifiedObject.getRecurrence()).getQuantity());
        assertEquals(((Installments)persisted.getRecurrence()).getId(), ((Installments)unmodifiedObject.getRecurrence()).getId());
    }

    @Test @Order(16)
    void updateIncomeWithInvalidDate() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        income.setRecurrence(new Installments(10));
        Income persisted = repository.save(income);

        IncomeDTO update = new IncomeDTO(income.getId(), "salario", 1200., null, new InstallmentsDTO(10));

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
        assertEquals(((Installments)persisted.getRecurrence()).getQuantity(), ((Installments)unmodifiedObject.getRecurrence()).getQuantity());
        assertEquals(((Installments)persisted.getRecurrence()).getId(), ((Installments)unmodifiedObject.getRecurrence()).getId());
    }
    @Test @Order(17)
    public void findAllWithNoParameters() throws IOException {
        for (int i = 1; i <= 20; i++) {
            Income income = new Income();
            income.setValue(i*1000d); income.setDescription("salario " + i); income.setOccurrenceDate(LocalDateTime.now());
            income.setRecurrence(new Installments(i));
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
            assertEquals(((Installments)income.getRecurrence()).getQuantity(), ((InstallmentsDTO)r.getRecurrence()).getQuantity());
        });
    }

    @Test @Order(18)
    public void findAllIncomesUsingPagination() throws JsonProcessingException {
        for (int i = 1; i <= 20; i++) {
            Income income = new Income();
            income.setValue(i*1000d); income.setDescription("salario " + i); income.setOccurrenceDate(LocalDateTime.now());
            income.setRecurrence(new Installments(i));
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
            assertEquals(((Installments)income.getRecurrence()).getQuantity(), ((InstallmentsDTO)r.getRecurrence()).getQuantity());
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
            assertEquals(((Installments)income.getRecurrence()).getQuantity(), ((InstallmentsDTO)r.getRecurrence()).getQuantity());
        });
    }
}

