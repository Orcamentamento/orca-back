package br.com.orcamentaria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter @Setter
public abstract class Transaction extends BaseEntity{

    private String description;
    @Column(name = "transaction_value")
    private Double value;
    private LocalDate occurrenceDate;
}
