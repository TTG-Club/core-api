package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.ArmorProficiency;
import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.ClassFeature;
import club.ttg.dnd5.domain.character_class.model.ClassFeatureOption;
import club.ttg.dnd5.domain.character_class.model.ClassFeatureScaling;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.character_class.model.ClassTableItem;
import club.ttg.dnd5.domain.character_class.model.MulticlassProficiency;
import club.ttg.dnd5.domain.character_class.model.SkillProficiency;
import club.ttg.dnd5.domain.character_class.model.WeaponProficiency;
import club.ttg.dnd5.domain.character_class.repository.ClassRepository;
import club.ttg.dnd5.domain.character_class.rest.dto.ClassFeatureDto;
import club.ttg.dnd5.domain.character_class.rest.dto.MulticlassInfo;
import club.ttg.dnd5.domain.character_class.rest.dto.MulticlassResponse;
import club.ttg.dnd5.domain.character_class.rest.mapper.ClassFeatureMapper;
import club.ttg.dnd5.domain.character_class.rest.mapper.MulticlassMapper;
import club.ttg.dnd5.domain.common.rest.dto.MulticlassDto;
import club.ttg.dnd5.domain.common.rest.dto.MulticlassRequest;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MulticlassService {
    private final ClassRepository classRepository;
    private final MulticlassMapper multiclassMapper;
    private final ClassFeatureMapper classFeatureMapper;

    public CharacterClass findByUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new EntityNotFoundException("Class url must not be empty");
        }
        return classRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Класс с url %s не существует", url)));
    }

    private Optional<CharacterClass> findSubclass(String url, Integer level) {
        if (level == null || level < 3) {
            return Optional.empty();
        }
        if (!StringUtils.hasText(url)) {
            return Optional.empty();
        }
        return Optional.of(findByUrl(url));
    }

    public MulticlassResponse getMulticlass(final MulticlassRequest request) {
        var multiclass = new CharacterClass();
        var mainClass = findByUrl(request.getUrl());
        List<ClassFeatureDto> features = new ArrayList<>();
        int extraAttack = 0;
        boolean spellcasting = false;
        int charachterLevel = request.getLevel();
        int spellcastLevel = calculateSpellCastingLevel(mainClass.getCasterType(), request.getLevel());
        List<String> requirements = new ArrayList<>();
        List<ClassTableColumn> table = new ArrayList<>();
        for (var column :mainClass.getTable()) {
            List<ClassTableItem> list = new ArrayList<>();
            for (ClassTableItem classTableItem : column.getScaling()) {
                if (classTableItem.getLevel() <= request.getLevel()) {
                    list.add(classTableItem);
                }
            }
            column.setScaling(list);
            table.add(column);
        }
        for (ClassFeature classFeature : mainClass.getFeatures()) {
            var classFilterFeature = filterMulticlassFeature(classFeature, request.getLevel(), 0);
            if (classFilterFeature.isHideInSubclasses()) {
                continue;
            }

            if (classFilterFeature.getLevel() <= request.getLevel()) {
                if (classFilterFeature.getName().equals("Использование заклинаний")) {
                    classFilterFeature.setDescription(getSpellcastingMulticlass());
                    spellcasting = true;
                } else if (classFeature.getName().equals("Дополнительная атака")
                        || classFeature.getName().contains("дополнительные атаки")
                        || classFeature.getName().contains("дополнительных атак")) {
                    extraAttack++;
                }
                var feature = classFeatureMapper.toDto(classFilterFeature, false);
                feature.setAdditional(mainClass.getName());
                features.add(feature);
            }
        }
        var mainSubClass = findSubclass(request.getSubclass(), request.getLevel());

        List<MulticlassInfo> multiclassInfo = new ArrayList<>();
        multiclassInfo.add(MulticlassInfo.builder()
                .hitDice("1" + mainClass.getHitDice().getName() + " за каждый уровень")
                .name(mainClass.getName())
                .subclass(mainSubClass.map(CharacterClass::getName).orElse(null))
                .level(request.getLevel())
                .build());

        if (mainClass.getCasterType() == CasterType.NONE) {
            spellcastLevel += mainSubClass
                    .map(subclass -> calculateSpellCastingLevel(subclass.getCasterType(), request.getLevel()))
                    .orElse(0);
        }
        for (ClassFeature subclassFeature : mainSubClass
                .map(CharacterClass::getFeatures)
                .orElseGet(List::of)) {
            if (subclassFeature.getLevel() <= request.getLevel()) {
                if (subclassFeature.getName().equals("Использование заклинаний")) {
                    subclassFeature.setDescription(getSpellcastingMulticlass());
                    spellcasting = true;
                } else if (subclassFeature.getName().equals("Дополнительная атака")
                        || subclassFeature.getName().contains("дополнительные атаки")
                        || subclassFeature.getName().contains("дополнительных атак")) {
                    extraAttack++;
                }
                ClassFeature filteredSubclassFeature = filterMulticlassFeature(subclassFeature, request.getLevel(), 0);
                var feature = classFeatureMapper.toDto(filteredSubclassFeature, true);
                feature.setAdditional(mainSubClass.map(CharacterClass::getName).orElse(null));
                features.add(feature);
            }
        }
        var spellMulticlass = mainClass.getCasterType() != CasterType.NONE;
        var names = new ArrayList<String>(request.getClasses().size());
        multiclass.setArmorProficiency(copyArmorProficiency(mainClass.getArmorProficiency()));
        multiclass.setWeaponProficiency(copyWeaponProficiency(mainClass.getWeaponProficiency()));
        multiclass.setToolProficiency(mainClass.getToolProficiency());
        multiclass.setSkillProficiency(copySkillProficiency(mainClass.getSkillProficiency()));
        for (var multiclassRequest :  request.getClasses()) {
            var multiClass = findByUrl(multiclassRequest.getUrl());
            mergeMulticlassProficiency(multiclass, multiClass.getMulticlassProficiency());
            requirements.add(multiClass.getPrimaryCharacteristics()
                    .stream()
                    .map(ability -> ability.getName() + " 13")
                    .collect(Collectors.joining(" или ")));
            for (var column :multiClass.getTable()) {
                List<ClassTableItem> list = new ArrayList<>();
                for (ClassTableItem classTableItem : column.getScaling()) {
                    if (classTableItem.getLevel() <= multiclassRequest.getLevel()) {
                        list.add(classTableItem);
                    }
                    classTableItem.setLevel(classTableItem.getLevel() + charachterLevel);
                }
                column.setScaling(list);
                table.add(column);
            }
            spellcastLevel += calculateSpellCastingLevel(multiClass.getCasterType(), multiclassRequest.getLevel());
            var level = multiclassRequest.getLevel();
            for (ClassFeature multiclassFeature : multiClass.getFeatures()) {
                if (multiclassFeature.isHideInSubclasses()) {
                    continue;
                }
                if (multiclassFeature.getLevel() <= level) {
                    ClassFeature classFeature = filterMulticlassFeature(multiclassFeature,
                            level,
                            charachterLevel);
                    if (multiclassFeature.getName().equals("Использование заклинаний")) {
                        if (spellcasting) {
                            continue;
                        }
                        classFeature.setDescription(getSpellcastingMulticlass());
                    } else if (multiclassFeature.getName().equals("Дополнительная атака")
                            || multiclassFeature.getName().contains("дополнительные атаки")
                            || multiclassFeature.getName().contains("дополнительных атак")) {
                        if (extraAttack >= 1) {
                            classFeature.setName(getExtraAttackName(extraAttack));
                            classFeature.setDescription(
                                    "[\"Вы можете атаковать %s раза вместо одного, когда совершаете действие атака в свой ход.\"]"
                                            .formatted(extraAttack + 2));
                        }
                        extraAttack++;
                    }
                    classFeature.setLevel(multiclassFeature.getLevel() + charachterLevel);
                    var feature = classFeatureMapper.toDto(classFeature, false);
                    feature.setAdditional(multiClass.getName());
                    features.add(feature);
                }
            }
            var multiSubclass = findSubclass(multiclassRequest.getSubclass(), multiclassRequest.getLevel());
            for (ClassFeature multiSubclassFeature : multiSubclass
                    .map(CharacterClass::getFeatures)
                    .orElseGet(List::of)) {
                if (multiSubclassFeature.getLevel() <= level) {
                    ClassFeature classFeature = filterMulticlassFeature(multiSubclassFeature, level, charachterLevel);
                    classFeature.setLevel(multiSubclassFeature.getLevel() + charachterLevel);
                    var feature = classFeatureMapper.toDto(classFeature, true);
                    feature.setAdditional(multiSubclass.map(CharacterClass::getName).orElse(null));
                    features.add(feature);
                }
            }
            multiclassInfo.add(MulticlassInfo.builder()
                    .hitDice("1" + multiClass.getHitDice().getName() + " за каждый уровень")
                    .name(multiClass.getName())
                    .subclass(multiSubclass.map(CharacterClass::getName).orElse(null))
                    .level(multiclassRequest.getLevel())
                    .build());
            names.add(multiClass.getName());
            charachterLevel += multiclassRequest.getLevel();
            spellMulticlass |= multiClass.getCasterType() != CasterType.NONE;
        }
        features.sort(Comparator.comparing(ClassFeatureDto::getLevel));
        if (spellMulticlass) {
            multiclass.setCasterType(CasterType.MULTICLASS);
        }
        multiclass.setName(mainClass.getName() + " / " + String.join("/", names));
        multiclass.setHitDice(mainClass.getHitDice());
        multiclass.setSavingThrows(mainClass.getSavingThrows());
        multiclass.setPrimaryCharacteristics(mainClass.getPrimaryCharacteristics());

        multiclass.setTable(table);

        var multiclassResponse = multiclassMapper.toMulticlassResponse(multiclass);
        multiclassResponse.setCharacterLevel(request.getLevel()
                + request.getClasses().stream().mapToInt(MulticlassDto::getLevel).sum());
        multiclassResponse.setSpellcastingLevel(spellcastLevel);
        multiclassResponse.setFeatures(features);
        multiclassResponse.setMulticlass(multiclassInfo);
        multiclassResponse.setRequirements(String.join(", ", requirements));
        return multiclassResponse;
    }

    private void mergeMulticlassProficiency(CharacterClass multiclass, MulticlassProficiency proficiency) {
        if (proficiency == null) {
            return;
        }
        mergeArmorProficiency(multiclass, proficiency.getArmor());
        mergeWeaponProficiency(multiclass, proficiency.getWeapon());
        multiclass.setToolProficiency(appendText(multiclass.getToolProficiency(), proficiency.getToolProficiency()));
        if (proficiency.getSkills() > 0) {
            if (multiclass.getSkillProficiency() == null) {
                multiclass.setSkillProficiency(new SkillProficiency(0, new ArrayList<>()));
            }
            multiclass.getSkillProficiency().setCount(multiclass.getSkillProficiency().getCount() + proficiency.getSkills());
        }
    }

    private void mergeArmorProficiency(CharacterClass multiclass, ArmorProficiency proficiency) {
        if (proficiency == null) {
            return;
        }
        if (multiclass.getArmorProficiency() == null) {
            multiclass.setArmorProficiency(new ArmorProficiency(new LinkedHashSet<>(), null));
        }
        if (multiclass.getArmorProficiency().getCategory() == null) {
            multiclass.getArmorProficiency().setCategory(new LinkedHashSet<>());
        }
        if (proficiency.getCategory() != null) {
            multiclass.getArmorProficiency().getCategory().addAll(proficiency.getCategory());
        }
        multiclass.getArmorProficiency().setCustom(appendText(
                multiclass.getArmorProficiency().getCustom(),
                proficiency.getCustom()
        ));
    }

    private void mergeWeaponProficiency(CharacterClass multiclass, WeaponProficiency proficiency) {
        if (proficiency == null) {
            return;
        }
        if (multiclass.getWeaponProficiency() == null) {
            multiclass.setWeaponProficiency(new WeaponProficiency(new LinkedHashSet<>(), null));
        }
        if (multiclass.getWeaponProficiency().getCategory() == null) {
            multiclass.getWeaponProficiency().setCategory(new LinkedHashSet<>());
        }
        if (proficiency.getCategory() != null) {
            multiclass.getWeaponProficiency().getCategory().addAll(proficiency.getCategory());
        }
        multiclass.getWeaponProficiency().setCustom(appendText(
                multiclass.getWeaponProficiency().getCustom(),
                proficiency.getCustom()
        ));
    }

    private ArmorProficiency copyArmorProficiency(ArmorProficiency proficiency) {
        if (proficiency == null) {
            return null;
        }
        return new ArmorProficiency(
                proficiency.getCategory() == null ? null : new LinkedHashSet<>(proficiency.getCategory()),
                proficiency.getCustom()
        );
    }

    private WeaponProficiency copyWeaponProficiency(WeaponProficiency proficiency) {
        if (proficiency == null) {
            return null;
        }
        return new WeaponProficiency(
                proficiency.getCategory() == null ? null : new LinkedHashSet<>(proficiency.getCategory()),
                proficiency.getCustom()
        );
    }

    private SkillProficiency copySkillProficiency(SkillProficiency proficiency) {
        if (proficiency == null) {
            return null;
        }
        return new SkillProficiency(
                proficiency.getCount(),
                proficiency.getSkills() == null ? new ArrayList<>() : new ArrayList<>(proficiency.getSkills())
        );
    }

    private String appendText(String current, String addition) {
        if (!StringUtils.hasText(addition)) {
            return current;
        }
        if (!StringUtils.hasText(current)) {
            return addition;
        }
        if (List.of(current.split(", ")).contains(addition)) {
            return current;
        }
        return current + ", " + addition;
    }

    private String getExtraAttackName(final int extraAttack)
    {
        return switch (extraAttack)
        {
            case 1 -> "Две дополнительные атаки";
            case 2 -> "Три дополнительные атаки";
            case 3 -> "Четыре дополнительные атаки";
            default -> throw new IllegalArgumentException(
                    "Unsupported extraAttack value: " + extraAttack
            );
        };
    }

    private ClassFeature filterMulticlassFeature(final ClassFeature classFeature,
                                                 final int level,
                                                 final int characterLevel) {
        ClassFeature filteredFeature = copyClassFeature(classFeature);
        List<ClassFeatureScaling> list = new ArrayList<>();
        for (ClassFeatureScaling classFeatureScaling : Optional.ofNullable(classFeature.getScaling()).orElse(List.of())) {
            if (classFeatureScaling.getLevel() <= level) {
                list.add(new ClassFeatureScaling(
                        classFeatureScaling.getLevel() + characterLevel,
                        classFeatureScaling.getName(),
                        classFeatureScaling.getDescription(),
                        classFeatureScaling.getAdditional(),
                        classFeatureScaling.isHideInSubclasses()
                ));
            }
        }
        filteredFeature.setScaling(list);
        filteredFeature.setOptions(Optional.ofNullable(classFeature.getOptions())
                .orElse(List.of())
                .stream()
                .filter(option -> !option.isHideInSubclasses())
                .filter(option -> option.getRequiredClassLevel() == null || option.getRequiredClassLevel() <= level)
                .map(ClassFeatureOption::new)
                .toList());
        return filteredFeature;
    }

    private ClassFeature copyClassFeature(final ClassFeature classFeature) {
        ClassFeature copy = new ClassFeature();
        copy.setKey(classFeature.getKey());
        copy.setLevel(classFeature.getLevel());
        copy.setName(classFeature.getName());
        copy.setDescription(classFeature.getDescription());
        copy.setAdditional(classFeature.getAdditional());
        copy.setScaling(Optional.ofNullable(classFeature.getScaling())
                .orElse(List.of())
                .stream()
                .map(scaling -> new ClassFeatureScaling(
                        scaling.getLevel(),
                        scaling.getName(),
                        scaling.getDescription(),
                        scaling.getAdditional(),
                        scaling.isHideInSubclasses()
                ))
                .toList());
        copy.setOptions(Optional.ofNullable(classFeature.getOptions())
                .orElse(List.of())
                .stream()
                .map(ClassFeatureOption::new)
                .toList());
        copy.setAbilityImprovement(classFeature.isAbilityImprovement());
        copy.setHideInSubclasses(classFeature.isHideInSubclasses());
        copy.setAbilityBonus(classFeature.getAbilityBonus());
        return copy;
    }

    private int calculateSpellCastingLevel(CasterType casterType, int level) {
        return switch (casterType) {
            case FULL -> level;
            case HALF -> (int) Math.ceil(level / 2.);
            case THIRD -> (int) Math.floor(level / 3.);
            default -> 0;
        };
    }

    private String getSpellcastingMulticlass() {
        return """
                [
                  "Ваши возможности для накладывания заклинаний зависят частично от ваших комбинированных уровней во всех ваших классах, умеющих накладывать заклинания, и частично от ваших индивидуальных уровней в этих классах. Как только вы получаете умение {@i Использование заклинаний} более чем из одного класса, используйте приведённые ниже правила. Если вы персонаж с мультиклассом, но у вас есть особенность {@i Использование заклинаний} только из одного класса, следуйте правилам для этого класса.",
                  "{@b Подготовленные заклинания.} Вы определяете, какие заклинания можете подготовить для каждого класса отдельно, как если бы вы были персонажем только этого класса. Например, если вы следопыт 4 уровня / чародей 3 уровня, то вы можете подготовить 5 заклинаний следопыта 1 уровня и 6 заклинаний чародея 1 или 2 уровня (а также четыре заговора чародея). Каждое заклинание, которое вы подготовили, связано с одним из ваших классов, и вы используете заклинательную характеристику этого класса, когда накладываете это заклинание.",
                  "{@b Заговоры.} Если заговор усиливается на более высоких уровнях, это усиление основано на общем уровне вашего персонажа, а не на уровне в конкретном классе, если в описании заклинания не указано иное.",
                  {
                    "type": "table",
                    "caption": "Мультиклассовый заклинатель: ячейки заклинаний по уровням заклинаний",
                    "colLabels": ["Уровень", "1", "2", "3", "4", "5", "6", "7", "8", "9"],
                    "colStyles": ["w-10 text-center", "w-10 text-center", "w-10 text-center", "w-10 text-center", "w-10 text-center", "w-10 text-center", "w-10 text-center", "w-10 text-center", "w-10 text-center", "w-10 text-center"],
                    "rows": [
                      ["1", "2", "—", "—", "—", "—", "—", "—", "—", "—"],
                      ["2", "3", "—", "—", "—", "—", "—", "—", "—", "—"],
                      ["3", "4", "2", "—", "—", "—", "—", "—", "—", "—"],
                      ["4", "4", "3", "—", "—", "—", "—", "—", "—", "—"],
                      ["5", "4", "3", "2", "—", "—", "—", "—", "—", "—"],
                      ["6", "4", "3", "3", "—", "—", "—", "—", "—", "—"],
                      ["7", "4", "3", "3", "1", "—", "—", "—", "—", "—"],
                      ["8", "4", "3", "3", "2", "—", "—", "—", "—", "—"],
                      ["9", "4", "3", "3", "3", "1", "—", "—", "—", "—"],
                      ["10", "4", "3", "3", "3", "2", "—", "—", "—", "—"],
                      ["11", "4", "3", "3", "3", "2", "1", "—", "—", "—"],
                      ["12", "4", "3", "3", "3", "2", "1", "—", "—", "—"],
                      ["13", "4", "3", "3", "3", "2", "1", "1", "—", "—"],
                      ["14", "4", "3", "3", "3", "2", "1", "1", "—", "—"],
                      ["15", "4", "3", "3", "3", "2", "1", "1", "1", "—"],
                      ["16", "4", "3", "3", "3", "2", "1", "1", "1", "—"],
                      ["17", "4", "3", "3", "3", "2", "1", "1", "1", "1"],
                      ["18", "4", "3", "3", "3", "3", "1", "1", "1", "1"],
                      ["19", "4", "3", "3", "3", "3", "2", "1", "1", "1"],
                      ["20", "4", "3", "3", "3", "3", "2", "2", "1", "1"]
                    ]
                  },
                  "{@b Ячейки заклинаний.} Вы определяете доступные вам ячейки заклинаний, суммируя следующие показатели:",
                  {
                    "type": "list",
                    "attrs": { "type": "unordered" },
                    "content": [
                      "Все ваши уровни в классах барда, жреца, друида, чародея и волшебника.",
                      "Половину ваших уровней (округляя вверх) в классах паладина и следопыта.",
                      "Одну треть ваших уровней (округляя вниз) в классах воина или плута, если у вас есть подклассы Магический рыцарь или Мистический ловкач."
                    ]
                  },
                  "Затем найдите этот суммарный уровень в колонке «Уровень» таблицы «Мультиклассовый заклинатель». Вы используете ячейки заклинаний этого уровня, чтобы накладывать заклинания соответствующего уровня из любого класса, у которого есть способность {@i Использование заклинаний}.",
                  "Эта таблица может дать вам ячейки заклинаний более высокого уровня, чем заклинания, которые вы можете подготовить. Вы можете использовать такие ячейки только для наложения заклинаний более низкого уровня. Если заклинание имеет усиленный эффект при использовании ячейки более высокого уровня, вы можете применять этот эффект как обычно.",
                  "Например, если вы следопыт 4 уровня / чародей 3 уровня, вы считаетесь персонажем 5 уровня при определении ячеек заклинаний. У вас есть 4 ячейки заклинаний 1 уровня, 3 ячейки 2 уровня и 2 ячейки 3 уровня. Однако вы не можете подготовить заклинания 3 уровня и не можете подготовить заклинания 2 уровня следопыта. Вы всё равно можете использовать эти ячейки для накладывания подготовленных заклинаний и усиливать их эффекты.",
                  "{@b Магия договора.} Если у вас есть умение {@i Магия договора} из класса колдуна и умение {@i Использование заклинаний}, вы можете использовать ячейки заклинаний, полученные от Магии договора, для накладывания заклинаний других классов, и наоборот — использовать ячейки Использования заклинаний для сотворения заклинаний колдуна."
                ]
               """;
    }
}
