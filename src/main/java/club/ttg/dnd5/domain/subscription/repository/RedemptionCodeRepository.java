package club.ttg.dnd5.domain.subscription.repository;

import club.ttg.dnd5.domain.subscription.model.RedemptionCode;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RedemptionCodeRepository extends JpaRepository<RedemptionCode, UUID> {
    Optional<RedemptionCode> findByCode(String code);

    boolean existsByCode(String code);

    /** Все выпущенные коды, новые сверху (для админского списка). */
    List<RedemptionCode> findAllByOrderByCreatedAtDesc();

    /** Коды, погашенные пользователем, новые сверху (для личного кабинета). */
    @EntityGraph(attributePaths = {"perks", "achievements"})
    List<RedemptionCode> findByRedeemedByOrderByRedeemedAtDesc(String redeemedBy);

    /**
     * Атомарно помечает код использованным. Условие {@code redeemed_by IS NULL}
     * гарантирует, что при гонке двух параллельных погашений ровно одно обновит строку.
     *
     * @return 1 — код захвачен этим вызовом; 0 — код уже был использован
     */
    @Modifying
    @Query("update RedemptionCode c set c.redeemedBy = :username, c.redeemedAt = :now "
            + "where c.code = :code and c.redeemedBy is null")
    int claim(@Param("code") String code, @Param("username") String username, @Param("now") Instant now);
}
