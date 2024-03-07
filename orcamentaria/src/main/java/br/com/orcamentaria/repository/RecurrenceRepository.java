package br.com.orcamentaria.repository;

import br.com.orcamentaria.model.Recurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecurrenceRepository extends JpaRepository<Recurrence, UUID> {
}
