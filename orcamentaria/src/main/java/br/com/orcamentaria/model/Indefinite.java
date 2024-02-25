package br.com.orcamentaria.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter @Setter
public class Indefinite extends Recurrence{
    private LocalDate endOfRecurrence;
}
