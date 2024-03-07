package br.com.orcamentaria.model;


import br.com.orcamentaria.listener.EntityValidationListener;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

@JsonIgnoreProperties(value = {"active"})
@MappedSuperclass
@Getter @Setter
@Where(clause = "active = true")
@EntityListeners(EntityValidationListener.class)
public class BaseEntity {
    @Id @GeneratedValue
    private UUID id;
    private boolean active = true;
}
