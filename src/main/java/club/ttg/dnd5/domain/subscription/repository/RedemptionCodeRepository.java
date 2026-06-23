package club.ttg.dnd5.domain.subscription.repository;

import club.ttg.dnd5.domain.subscription.model.RedemptionCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RedemptionCodeRepository extends JpaRepository<RedemptionCode, UUID> {
    Optional<RedemptionCode> findByCode(String code);

    boolean existsByCode(String code);
}
