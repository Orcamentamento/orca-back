package br.com.orcamentaria.integration;

import br.com.orcamentaria.dto.IncomeDTO;
import br.com.orcamentaria.exception.ExceptionResponse;
import br.com.orcamentaria.model.Income;
import br.com.orcamentaria.repository.IncomeRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class IncomeIntegrationTest extends AbstractContainerBaseTest{

    @Autowired
    IncomeRepository repository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        repository.deleteAllInBatch();
    }

    @Test
    void insertIncomeWithSuccess() {
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

        Income persisted = repository.findById(response.getId()).get();
        assertEquals(dto.getDescription(), persisted.getDescription());
        assertEquals(dto.getValue(), persisted.getValue());
        assertTrue(persisted.isActive());
    }

    @Test
    void insertIncomeWithInvalidValue() {
        IncomeDTO dto = new IncomeDTO(null, "salario", 0., LocalDateTime.now());

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
    }

    @Test
    void insertIncomeWithNullDate() {
        IncomeDTO dto = new IncomeDTO(null, "salario", 1200., null);

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
    }

    @Test
    void insertIncomeWithNullDescription() {
        IncomeDTO dto = new IncomeDTO(null, null, 1200., LocalDateTime.now());

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
    }

    @Test
    void insertIncomeWithBlankDescription() {
        IncomeDTO dto = new IncomeDTO(null, "", 1200., LocalDateTime.now());

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
    }

    @Test
    void disablingIncomeWithSuccess() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        Income persistedIncome = repository.save(income);

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .pathParam("id", persistedIncome.getId())
                .when()
                    .delete("/incomes/{id}")
                .then()
                    .statusCode(204);

        var disabledIncome = repository.findById(persistedIncome.getId()).get();
        assertFalse(disabledIncome.isActive());
    }

    @Test
    void disablingIncomeWithBlanckIdInformed() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        Income persistedIncome = repository.save(income);

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .pathParam("id", "")
                .when()
                    .delete("/incomes/{id}")
                .then()
                    .statusCode(404);

        var disabledIncome = repository.findById(persistedIncome.getId()).get();
        assertTrue(disabledIncome.isActive());
    }

    @Test
    void disablingIncomeWithWrongId() {

        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
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
    }

}
