package br.com.orcamentaria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor @AllArgsConstructor
public class IndefiniteDTO extends RecurrenceDTO{
    private LocalDate endOfRecurrence;
}
