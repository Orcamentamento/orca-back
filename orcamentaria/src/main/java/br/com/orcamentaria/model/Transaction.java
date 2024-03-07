package br.com.orcamentaria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter @Setter @EqualsAndHashCode(callSuper = true)
public abstract class Transaction extends BaseEntity{
    @Column(nullable = false)
    @NotBlank(message = "must be informed")
    private String description;
    @Column(name = "transaction_value", nullable = false)
    @DecimalMin(value = "0.01", message = "must be greater than 0")
    private Double value;
    @Column(nullable = false)
    @NotNull(message = "must be informed")
    private LocalDateTime occurrenceDate;
    @ManyToOne(cascade = CascadeType.ALL)
    private Recurrence recurrence;
}
