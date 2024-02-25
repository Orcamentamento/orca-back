package br.com.orcamentaria.integration;

import br.com.orcamentaria.dto.IncomeDTO;
import br.com.orcamentaria.exception.ExceptionResponse;
import br.com.orcamentaria.model.Income;
import br.com.orcamentaria.repository.IncomeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;


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


    @Test
    void findIncomeWithSuccess() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
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
    }
    @Test
    void findIncomeWithBlanckIdInformed() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        Income persistedIncome = repository.save(income);

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .pathParam("id", "")
                .when()
                    .get("/incomes/{id}")
                .then()
                    .statusCode(404);

        var disabledIncome = repository.findById(persistedIncome.getId()).get();
        assertTrue(disabledIncome.isActive());
    }

    @Test
    void findIncomeWithWrongId() {

        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
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

        var disabledIncome = repository.findById(persistedIncome.getId()).get();
        assertTrue(disabledIncome.isActive());
    }

    @Test
    void updateIncomeWithSuccess() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        repository.save(income);

        IncomeDTO update = new IncomeDTO(income.getId(), "update", 50., LocalDateTime.now());

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(update)
                .when()
                        .put("/incomes")
                .then()
                    .statusCode(200).extract().as(IncomeDTO.class);

        assertEquals(update.getId(), response.getId());
        assertEquals(update.getDescription(), response.getDescription());
        assertEquals(update.getValue(), response.getValue());
        assertEquals(update.getOccurrenceDate(), response.getOccurrenceDate());
    }

    @Test
    void updateIncomeWithInvalidDescription() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        Income persisted = repository.save(income);

        IncomeDTO update = new IncomeDTO(income.getId(), "", 50., LocalDateTime.now());

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
        assertEquals(unmodifiedObject.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), persisted.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void updateIncomeWithInvalidValue() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        Income persisted = repository.save(income);

        IncomeDTO update = new IncomeDTO(income.getId(), "salario", 0., LocalDateTime.now());

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
        assertEquals(unmodifiedObject.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), persisted.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void updateIncomeWithInvalidDate() {
        Income income = new Income();
        income.setValue(1200.); income.setDescription("salario");income.setOccurrenceDate(LocalDateTime.now());
        Income persisted = repository.save(income);

        IncomeDTO update = new IncomeDTO(income.getId(), "salario", 50., null);

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
        assertEquals(unmodifiedObject.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), persisted.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));

    }

    @Test
    public void findAllWithNoParameters() throws IOException {
        for (int i = 1; i <= 20; i++) {
            Income income = new Income();
            income.setValue(i*1000d); income.setDescription("salario " + i); income.setOccurrenceDate(LocalDateTime.now());
            repository.save(income);
        }

        var response =
                given()
                .when()
                    .get("/incomes")
                .then()
                .statusCode(200)
                .extract().body().asString();

        List<IncomeDTO> parsedResponse = getIncomeDTOSFromPageableResponse(response);
        assertEquals(12, parsedResponse.size());
        parsedResponse.forEach(r -> {
            Income income = repository.findById(r.getId()).get();
            assertEquals(income.getDescription(), r.getDescription());
            assertEquals(income.getValue(), r.getValue());
            assertEquals(income.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), r.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
        });
    }

    @Test
    public void findAllIncomesUsingPagination() throws JsonProcessingException {
        for (int i = 1; i <= 20; i++) {
            Income income = new Income();
            income.setValue(i*1000d); income.setDescription("salario " + i); income.setOccurrenceDate(LocalDateTime.now());
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

        List<IncomeDTO> parsedResponse = getIncomeDTOSFromPageableResponse(response);

        assertEquals(10, parsedResponse.size());
        parsedResponse.forEach(r -> {
            Income income = repository.findById(r.getId()).get();
            assertEquals(income.getDescription(), r.getDescription());
            assertEquals(income.getValue(), r.getValue());
            assertEquals(income.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), r.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
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
        parsedResponse = getIncomeDTOSFromPageableResponse(response);

        parsedResponse.forEach(r -> {
            Income income = repository.findById(r.getId()).get();
            assertEquals(income.getDescription(), r.getDescription());
            assertEquals(income.getValue(), r.getValue());
            assertEquals(income.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS), r.getOccurrenceDate().truncatedTo(ChronoUnit.SECONDS));
        });


    }

    private static List<IncomeDTO> getIncomeDTOSFromPageableResponse(String response) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode contentNode = jsonNode.path("content");
        return objectMapper.readValue(contentNode.toString(), new TypeReference<List<IncomeDTO>>() {});
    }
}
