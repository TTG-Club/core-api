package club.ttg.dnd5.domain.item.repository;

import club.ttg.dnd5.domain.item.model.weapon.WeaponProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeaponPropertiesRepository extends JpaRepository<WeaponProperties, String> {
}
