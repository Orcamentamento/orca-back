package br.com.orcamentaria.integration;

import br.com.orcamentaria.dto.IncomeDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IncomeTestUtil {
    @Autowired
    private ObjectMapper objectMapper;
    public List<IncomeDTO> getIncomeDTOSFromPageableResponse(String response) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode contentNode = jsonNode.path("content");
        return objectMapper.readValue(contentNode.toString(), new TypeReference<List<IncomeDTO>>() {});
    }
}
