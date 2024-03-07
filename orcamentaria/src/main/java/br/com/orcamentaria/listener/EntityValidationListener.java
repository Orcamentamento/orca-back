package br.com.orcamentaria.listener;

import br.com.orcamentaria.model.BaseEntity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.*;

import java.util.Set;

public class EntityValidationListener {

    @PrePersist
    @PreUpdate
    public void validate(BaseEntity object) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<BaseEntity>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
