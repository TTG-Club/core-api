package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.spell.mapper.SpellMapper;
import club.ttg.dnd5.domain.spell.model.*;
import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import club.ttg.dnd5.domain.spell.model.enums.DistanceUnit;
import club.ttg.dnd5.domain.spell.model.enums.DurationUnit;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpellService {
    private final SpellRepository spellRepository;
    private final SpellMapper spellMapper;

    public List<SpellShortResponse> findAll() {
        return spellRepository.findAll().stream()
                .map(spellMapper::toSpeciesShortResponse)
                .collect(Collectors.toList());
    }

    public SpellDetailedResponse findByUrl(String url) {
        return spellRepository.findById(url)
                .map(spellMapper::toSpellDetailedResponse)
                .orElseThrow();
    }


//    @PostConstruct
//    public void postConstruct(){
//        Spell spell = new Spell();
//        spell.setUrl("magic_stone");
//        spell.setName("Волшебный камень");
//        spell.setEnglish("Волшебный камень");
//        spell.setAlternative("Почему это вообще существует");
//        spell.setDescription("""
//                Вы кидаете кислотный шарик.
//
//                Выберите одно видимое1 вами существо в пределах дистанции или два видимых2 вами существа в пределах дистанции, находящихся в пределах 5 футов друг от друга. Цель должна преуспеть в спасброске Ловкости, иначе она получает 1к6 урона кислотой.
//
//                       """);
//        spell.setLevel(0L);
//        spell.setSchool(SpellSchool.builder()
//                        .school(MagicSchool.ABJURATION)
//                        .additionalType("АБЖУРАШН")
//                .build());
//        spell.setRitual(false);
//        spell.setConcentration(false);
//        spell.setComponents(SpellComponents.builder()
//                        .v(true)
//                        .s(true)
//                        .m(MaterialComponent.builder()
//                                .component("Банка пива, расходуемая при сотворении заклинания")
//                                .consumable(true)
//                                .build())
//                .build());
//        spell.setDistance(List.of(SpellDistance.builder()
//                .distanceUnit(DistanceUnit.SELF)
//                .build()));
//        spell.setCustomDistance("на товарища");
//        spell.setDuration(List.of(SpellDuration.builder()
//                .durationUnit(DurationUnit.HOUR)
//                .build()));
//        spell.setCustomDuration("дольше");
//        spell.setCastingTime(List.of(SpellCastingTime.builder()
//                .castingUnit(CastingUnit.ACTION)
//                .build()));
//        spell.setCustomCastingTime("дольше");
//        spell.setUpper("""
//                Урон этого заклинания увеличивается на 1к6, когда вы достигаете 5 уровня (2к6), 11 уровня (3к6) и 17 уровня (4к6).
//                """);
//        spell.setSourcePage(0L);
//        spellRepository.save(spell);
//    }
}
