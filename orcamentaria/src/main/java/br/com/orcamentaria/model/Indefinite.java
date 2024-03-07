package br.com.orcamentaria.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@SQLDelete(sql = "UPDATE recurrence SET active = false WHERE id = ?")
public class Indefinite extends Recurrence{
    private LocalDate endOfRecurrence;
}
