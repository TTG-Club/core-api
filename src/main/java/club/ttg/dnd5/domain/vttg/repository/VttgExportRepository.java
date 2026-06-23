package club.ttg.dnd5.domain.vttg.repository;

import club.ttg.dnd5.domain.vttg.model.VttgExport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface VttgExportRepository extends JpaRepository<VttgExport, VttgExport.Key> {

    /** Предрассчитанные payload одного типа по набору естественных ключей. */
    List<VttgExport> findByTypeAndUrlIn(String type, Collection<String> urls);
}
