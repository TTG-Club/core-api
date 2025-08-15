package club.ttg.dnd5.domain.common.repository;

import club.ttg.dnd5.domain.common.model.notification.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, UUID> {
}
