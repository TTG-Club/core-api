package club.ttg.dnd5.domain.user.repository;

import club.ttg.dnd5.domain.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String roleName);
}
