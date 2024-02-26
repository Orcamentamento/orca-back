package br.com.orcamentaria.desserializer;

import br.com.orcamentaria.model.Income;
import br.com.orcamentaria.model.Indefinite;
import br.com.orcamentaria.model.Installments;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalDateTime;

public class TransactionDesserializer extends JsonDeserializer {
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String description = node.get("description").asText();
        Double value = node.get("value").asDouble();
        LocalDateTime occurrenceDate = LocalDateTime.parse(node.get("occurrenceDate").toString());
        Integer installmentsValue = node.has("recurrence") ? node.get("recurrence").asInt() : null;

        Income income = new Income();
        income.setDescription(description);
        income.setValue(value);
        income.setOccurrenceDate(occurrenceDate);

        if (installmentsValue == null || installmentsValue == 0) {
            income.setRecurrence(new Indefinite());
        } else {
            income.setRecurrence(new Installments(installmentsValue));
        }

        return income;
    }
}
