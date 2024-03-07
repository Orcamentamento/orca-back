package br.com.orcamentaria.model;

import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

@Entity
@Data @EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE income SET active = false WHERE id = ?")
public class Income extends Transaction{
}
