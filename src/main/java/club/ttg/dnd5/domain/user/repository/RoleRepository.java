package club.ttg.dnd5.domain.user.repository;

import club.ttg.dnd5.domain.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String roleName);
    List<Role> findAllByNameIn(Collection<String> roleNames);

}
