package club.ttg.dnd5.domain.moderation.service;

import club.ttg.dnd5.domain.moderation.model.StatusType;
import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.item.model.Item;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ModerationAspectService {
    private final ModerationService service;

    @AfterReturning(
            pointcut = "execution(* addBackground(*))",
            returning = "background")
    public void addBackground(Background background) {
        service.addAdminEntity(background.getUrl(), SectionType.BACKGROUND, background);
    }

    @AfterReturning(
            pointcut = "execution(* addFeats(*))",
            returning = "feat")
    public void addFeats(Feat feat) {
        service.addAdminEntity(feat.getUrl(), SectionType.FEAT, feat);
    }

    @AfterReturning(
            pointcut = "execution(* addMagicItem(*))",
            returning = "magicItem")
    public void addMagicItem(MagicItem magicItem) {
        service.addAdminEntity(magicItem.getUrl(), SectionType.FEAT, magicItem);
    }

    @AfterReturning(
            pointcut = "execution(* addItem(*))",
            returning = "item")
    public void addItem(Item item) {
        service.addAdminEntity(item.getUrl(), SectionType.FEAT, item);
    }

    @AfterReturning(
            pointcut = """
                    execution(* updateBackgrounds(..))
                    || execution(* updateFeat(..))
                    || execution(* updateSpell(..))
                    || execution(* updateCreature(..))
                    || execution(* updateMagicItem(..))
                    || execution(* updateItem(..))
                    || execution(* updateGlossary(..))
                    || execution(* updateClass(..))
                    """,
            returning = "url")
    public void updateEntity(String url) {
        service.updateAdminEntity(url, StatusType.REVIEW);
    }

    @AfterReturning(
            pointcut = """
            execution(* deleteBackgrounds(*))
            || execution(* deleteFeats(*))
            || execution(* deleteSpell(*))
            || execution(* deleteCreature(*))
            || execution(* deleteMagicItem(*))
            || execution(* deleteItem(*))
            || execution(* deleteGlossary(*))
            """,
            returning = "url")
    public void deleteEntity(String url) {
        service.updateAdminEntity(url, StatusType.HIDDEN);
    }
}
