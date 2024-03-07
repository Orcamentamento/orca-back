package br.com.orcamentaria.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = IndefiniteDTO.class, name = "indefinite"),
        @JsonSubTypes.Type(value = InstallmentsDTO.class, name = "installments")
})
@Data
public class RecurrenceDTO {
}
