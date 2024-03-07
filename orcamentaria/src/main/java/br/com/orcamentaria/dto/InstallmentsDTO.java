package br.com.orcamentaria.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class InstallmentsDTO extends RecurrenceDTO {
    private Integer quantity;
}
