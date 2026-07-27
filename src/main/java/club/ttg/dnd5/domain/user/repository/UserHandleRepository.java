package club.ttg.dnd5.domain.user.repository;

import club.ttg.dnd5.domain.user.model.UserHandle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserHandleRepository extends JpaRepository<UserHandle, UUID> {

    /** Регистронезависимая проверка занятости хендла (уникальность хендла — независимо от регистра). */
    boolean existsByHandleIgnoreCase(String handle);
}
