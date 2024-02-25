package br.com.orcamentaria.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Entity
@Data
public class Recurrence {
    @Id
    @GeneratedValue
    private UUID id;
    private LocalDate endDate;
}
