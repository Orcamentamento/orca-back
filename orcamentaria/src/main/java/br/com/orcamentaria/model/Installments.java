package br.com.orcamentaria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Data @EqualsAndHashCode(callSuper = true)
@AllArgsConstructor @NoArgsConstructor
@SQLDelete(sql = "UPDATE recurrence SET active = false WHERE id = ?")
public class Installments extends Recurrence{
    @Min(value = 1, message = "must be at least 1")
    private Integer quantity;
    @PrePersist
    private void preSave() {
        if (quantity == null) {
            quantity = 1;
        }
    }

}
