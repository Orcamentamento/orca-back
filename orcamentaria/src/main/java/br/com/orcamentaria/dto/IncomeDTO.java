package br.com.orcamentaria.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class IncomeDTO {
    private UUID id;
    private String description;
    private Double value;
    private LocalDateTime occurrenceDate;
}
