package br.com.orcamentaria.dto;

import br.com.orcamentaria.model.Recurrence;
import com.fasterxml.jackson.annotation.JsonMerge;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class IncomeDTO {
    private UUID id;
    private String description;
    private Double value;
    private LocalDateTime occurrenceDate;
    @JsonMerge
    private RecurrenceDTO recurrence;
}
