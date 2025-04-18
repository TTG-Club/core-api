package club.ttg.dnd5.domain.item.repository;

import club.ttg.dnd5.domain.item.model.weapon.WeaponMastery;
import club.ttg.dnd5.domain.item.model.weapon.WeaponProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeaponMasteryRepository extends JpaRepository<WeaponMastery, String> {
}
