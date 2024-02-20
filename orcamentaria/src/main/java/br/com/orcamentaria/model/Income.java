package br.com.orcamentaria.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter @Setter
@SQLDelete(sql = "UPDATE income SET active = false WHERE id = ?")
public class Income extends Transaction{
}
