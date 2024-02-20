package br.com.orcamentaria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter @Setter
public abstract class Transaction extends BaseEntity{

    @Column(nullable = false)
    private String description;
    @Column(name = "transaction_value", nullable = false)
    @DecimalMin(value = "0.01", message = "The value must be greater than 0")
    private Double value;
    @Column(nullable = false)
    private LocalDateTime occurrenceDate;
}
