package br.com.orcamentaria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class IncomeDTO {
    private UUID id;
    private String description;
    private Double value;
    private LocalDateTime occurrenceDate;
}
