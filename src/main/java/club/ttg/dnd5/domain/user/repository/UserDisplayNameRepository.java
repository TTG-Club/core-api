package club.ttg.dnd5.domain.user.repository;

import club.ttg.dnd5.domain.user.model.UserDisplayName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserDisplayNameRepository extends JpaRepository<UserDisplayName, UUID> {

    /** Занято ли имя (регистронезависимо) кем угодно — для генерации уникального дефолта. */
    boolean existsByDisplayNameIgnoreCase(String displayName);

    /** Занято ли имя (регистронезависимо) другим пользователем — для проверки при смене. */
    boolean existsByDisplayNameIgnoreCaseAndUserIdNot(String displayName, UUID userId);

    /** Является ли имя логином другого пользователя (регистронезависимо) — против подмены. */
    boolean existsByUsernameIgnoreCaseAndUserIdNot(String username, UUID userId);
}
