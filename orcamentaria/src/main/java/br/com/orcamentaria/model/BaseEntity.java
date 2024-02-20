package br.com.orcamentaria.model;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.UUID;

@MappedSuperclass
@Getter @Setter
@Where(clause = "active = true")
public class BaseEntity {

    @Id @GeneratedValue
    private UUID id;
    private boolean active = true;
}
