package club.ttg.dnd5.domain.vttg.repository;

import club.ttg.dnd5.domain.vttg.model.VttgCompendiumVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface VttgCompendiumVersionRepository extends JpaRepository<VttgCompendiumVersion, Short> {

    /**
     * Поднимает версию, только если новая строго больше текущей. Условие в самом UPDATE делает
     * проверку атомарной: из двух параллельных запросов с одной версией пройдёт только один.
     *
     * @return число обновлённых строк: 1 — версия поднята, 0 — новая версия не больше текущей
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update VttgCompendiumVersion v set v.version = :version, v.updatedAt = :updatedAt, "
            + "v.updatedBy = :updatedBy where v.id = :id and v.version < :version")
    int raise(@Param("id") short id, @Param("version") int version,
              @Param("updatedAt") Instant updatedAt, @Param("updatedBy") String updatedBy);
}
