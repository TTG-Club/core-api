package club.ttg.dnd5.domain.moderation.repository;

import club.ttg.dnd5.domain.moderation.model.ModerationEntity;
import club.ttg.dnd5.domain.moderation.model.StatusType;
import club.ttg.dnd5.domain.common.model.SectionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ModerationRepository extends JpaRepository<ModerationEntity, String> {

    Optional<ModerationEntity> findByUrl(String url);

    List<ModerationEntity> findByStatusTypeInAndSectionTypeIn(Collection<StatusType> statusTypes, Collection<SectionType> sectionTypes, Pageable pageable);

    List<ModerationEntity> findByStatusTypeIn(Collection<StatusType> statusTypes);
}
