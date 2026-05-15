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
import club.ttg.dnd5.domain.common.rest.dto.MulticlassLevelEntry;
import club.ttg.dnd5.domain.common.rest.dto.MulticlassRequest;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
            throw new EntityNotFoundException("URL класса не должен быть пустым");
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

    /**
     * Конвертирует устаревший формат запроса в новый упорядоченный формат уровней.
     */
    private List<MulticlassLevelEntry> toLevelEntries(MulticlassRequest request) {
        if (request.getLevels() != null && !request.getLevels().isEmpty()) {
            return request.getLevels();
        }
        // Устаревший формат: конвертируем в записи уровней
        List<MulticlassLevelEntry> entries = new ArrayList<>();
        entries.add(new MulticlassLevelEntry(request.getUrl(), request.getSubclass(), request.getLevel()));
        if (request.getClasses() != null) {
            for (MulticlassDto dto : request.getClasses()) {
                entries.add(new MulticlassLevelEntry(dto.getUrl(), dto.getSubclass(), dto.getLevel()));
            }
        }
        return entries;
    }

    public MulticlassResponse getMulticlass(final MulticlassRequest request) {
        List<MulticlassLevelEntry> entries = toLevelEntries(request);
        if (entries.isEmpty()) {
            throw new EntityNotFoundException("Необходима хотя бы одна запись уровня");
        }

        var multiclass = new CharacterClass();
        List<ClassFeatureDto> features = new ArrayList<>();
        int extraAttack = 0;
        boolean spellcasting = false;
        int characterLevel = 0;
        int spellcastLevel = 0;
        List<CasterType> casterTypes = new ArrayList<>();
        List<String> requirements = new ArrayList<>();
        List<ClassTableColumn> table = new ArrayList<>();
        List<MulticlassInfo> multiclassInfo = new ArrayList<>();
        List<String> names = new ArrayList<>();

        // Отслеживаем накопленные уровни по URL класса для фильтрации умений
        Map<String, Integer> cumulativeLevelsPerClass = new HashMap<>();

        // Первая запись — «основной» класс
        MulticlassLevelEntry firstEntry = entries.getFirst();
        CharacterClass mainClass = findByUrl(firstEntry.getUrl());

        // Устанавливаем базовые владения из основного класса
        multiclass.setArmorProficiency(copyArmorProficiency(mainClass.getArmorProficiency()));
        multiclass.setWeaponProficiency(copyWeaponProficiency(mainClass.getWeaponProficiency()));
        multiclass.setToolProficiency(mainClass.getToolProficiency());
        multiclass.setSkillProficiency(copySkillProficiency(mainClass.getSkillProficiency()));

        // Обрабатываем каждую запись по порядку
        for (int i = 0; i < entries.size(); i++) {
            MulticlassLevelEntry entry = entries.get(i);
            CharacterClass entryClass = findByUrl(entry.getUrl());

            // entry.getLevel() — целевой накопленный уровень класса после этого сегмента
            int previousClassLevel = cumulativeLevelsPerClass.getOrDefault(entry.getUrl(), 0);
            int newClassLevel = entry.getLevel();
            int segmentLevels = newClassLevel - previousClassLevel;
            cumulativeLevelsPerClass.put(entry.getUrl(), newClassLevel);

            // Объединяем владения мультикласса при первом появлении нового класса
            if (i > 0 && previousClassLevel == 0) {
                mergeMulticlassProficiency(multiclass, entryClass.getMulticlassProficiency());
                requirements.add(entryClass.getPrimaryCharacteristics()
                        .stream()
                        .map(ability -> ability.getName() + " 13")
                        .collect(Collectors.joining(" или ")));
            }

            // Расчёт уровня заклинателя
            spellcastLevel += calculateSpellCastingLevel(entryClass.getCasterType(), newClassLevel)
                    - calculateSpellCastingLevel(entryClass.getCasterType(), previousClassLevel);
            if (previousClassLevel == 0) {
                addCasterType(casterTypes, entryClass.getCasterType());
            }

            // Столбцы таблицы класса
            for (var column : entryClass.getTable()) {
                List<ClassTableItem> list = new ArrayList<>();
                for (ClassTableItem classTableItem : column.getScaling()) {
                    if (classTableItem.getLevel() > previousClassLevel && classTableItem.getLevel() <= newClassLevel) {
                        ClassTableItem copy = new ClassTableItem();
                        copy.setLevel(classTableItem.getLevel() - previousClassLevel + characterLevel);
                        copy.setValue(classTableItem.getValue());
                        list.add(copy);
                    }
                }
                if (!list.isEmpty()) {
                    // Merge into existing column with the same name if present
                    ClassTableColumn existing = table.stream()
                            .filter(c -> c.getName().equals(column.getName()))
                            .findFirst()
                            .orElse(null);
                    if (existing != null) {
                        existing.getScaling().addAll(list);
                    } else {
                        ClassTableColumn columnCopy = new ClassTableColumn();
                        columnCopy.setName(column.getName());
                        columnCopy.setScaling(new ArrayList<>(list));
                        table.add(columnCopy);
                    }
                }
            }

            // Обрабатываем умения класса для уровней (previousClassLevel, newClassLevel]
            for (ClassFeature classFeature : entryClass.getFeatures()) {
                if (classFeature.isHideInSubclasses()) {
                    continue;
                }
                if (classFeature.getLevel() > previousClassLevel && classFeature.getLevel() <= newClassLevel) {
                    ClassFeature filteredFeature = filterMulticlassFeature(classFeature, newClassLevel, characterLevel);
                    if (isSpellcastingFeature(classFeature)) {
                        if (spellcasting) {
                            continue;
                        }
                        filteredFeature.setDescription(getSpellcastingMulticlass());
                        spellcasting = true;
                    } else if (isExtraAttackFeature(classFeature)) {
                        if (extraAttack >= 1) {
                            filteredFeature.setName(getExtraAttackName(extraAttack));
                            filteredFeature.setDescription(
                                    "[\"Вы можете атаковать %s раза вместо одного, когда совершаете действие атака в свой ход.\"]"
                                            .formatted(extraAttack + 2));
                        }
                        extraAttack++;
                    }
                    // Устанавливаем уровень персонажа, на котором получено умение
                    filteredFeature.setLevel(characterLevel + (classFeature.getLevel() - previousClassLevel));
                    var feature = classFeatureMapper.toDto(filteredFeature, false);
                    feature.setAdditional(entryClass.getName());
                    features.add(feature);
                } else if (classFeature.getLevel() <= previousClassLevel && previousClassLevel > 0
                        && hasScalingOrOptionsInRange(classFeature, previousClassLevel, newClassLevel)) {
                    // Повторный сегмент класса: обновляем масштабирование/опции для ранее добавленного умения
                    ClassFeature filteredFeature = filterMulticlassFeatureForRange(classFeature, previousClassLevel, newClassLevel, characterLevel);
                    filteredFeature.setLevel(characterLevel + 1);
                    var feature = classFeatureMapper.toDto(filteredFeature, false);
                    feature.setAdditional(entryClass.getName());
                    features.add(feature);
                }
            }

            // Обрабатываем умения подкласса
            var subclass = findSubclass(entry.getSubclass(), newClassLevel);
            if (subclass.isPresent()) {
                CharacterClass sub = subclass.get();
                // Колдовство подкласса
                if (isNotCaster(entryClass.getCasterType())) {
                    spellcastLevel += calculateSpellCastingLevel(sub.getCasterType(), newClassLevel)
                            - calculateSpellCastingLevel(sub.getCasterType(), previousClassLevel);
                }
                if (previousClassLevel == 0) {
                    addCasterType(casterTypes, sub.getCasterType());
                }

                for (ClassFeature subFeature : sub.getFeatures()) {
                    if (subFeature.getLevel() > previousClassLevel && subFeature.getLevel() <= newClassLevel) {
                        ClassFeature filteredFeature = filterMulticlassFeature(subFeature, newClassLevel, characterLevel);
                        if (isSpellcastingFeature(subFeature)) {
                            if (spellcasting) {
                                continue;
                            }
                            filteredFeature.setDescription(getSpellcastingMulticlass());
                            spellcasting = true;
                        } else if (isExtraAttackFeature(subFeature)) {
                            if (extraAttack >= 1) {
                                filteredFeature.setName(getExtraAttackName(extraAttack));
                                filteredFeature.setDescription(
                                        "[\"Вы можете атаковать %s раза вместо одного, когда совершаете действие атака в свой ход.\"]"
                                                .formatted(extraAttack + 2));
                            }
                            extraAttack++;
                        }
                        filteredFeature.setLevel(characterLevel + (subFeature.getLevel() - previousClassLevel));
                        var feature = classFeatureMapper.toDto(filteredFeature, true);
                        feature.setAdditional(sub.getName());
                        features.add(feature);
                    } else if (subFeature.getLevel() <= previousClassLevel && previousClassLevel > 0
                            && hasScalingOrOptionsInRange(subFeature, previousClassLevel, newClassLevel)) {
                        // Повторный сегмент класса: обновляем масштабирование/опции для ранее добавленного умения подкласса
                        ClassFeature filteredFeature = filterMulticlassFeatureForRange(subFeature, previousClassLevel, newClassLevel, characterLevel);
                        filteredFeature.setLevel(characterLevel + 1);
                        var feature = classFeatureMapper.toDto(filteredFeature, true);
                        feature.setAdditional(sub.getName());
                        features.add(feature);
                    }
                }
            }

            // Формируем информацию о мультиклассе для этого сегмента
            multiclassInfo.add(MulticlassInfo.builder()
                    .hitDice("1" + entryClass.getHitDice().getName() + " за каждый уровень")
                    .name(entryClass.getName())
                    .subclass(subclass.map(CharacterClass::getName).orElse(null))
                    .level(segmentLevels)
                    .build());

            if (i > 0 || entries.size() == 1) {
                if (!names.contains(entryClass.getName())) {
                    names.add(entryClass.getName());
                }
            }

            characterLevel += segmentLevels;
        }

        // Умения уже идут в порядке прогрессии уровня персонажа — сортировка по уровню класса не нужна.
        // Сортируем по уровню персонажа, на котором они были получены (стабильный порядок внутри одного уровня).
        features.sort(Comparator.comparing(ClassFeatureDto::getLevel));

        multiclass.setCasterType(resolveCasterType(casterTypes));
        String nameStr = names.isEmpty()
                ? mainClass.getName()
                : mainClass.getName() + " / " + String.join(" / ", names);
        multiclass.setName(nameStr);
        multiclass.setHitDice(mainClass.getHitDice());
        multiclass.setSavingThrows(mainClass.getSavingThrows());
        multiclass.setPrimaryCharacteristics(mainClass.getPrimaryCharacteristics());
        multiclass.setTable(table);

        var multiclassResponse = multiclassMapper.toMulticlassResponse(multiclass);
        multiclassResponse.setCharacterLevel(characterLevel);
        multiclassResponse.setSpellcastingLevel(spellcastLevel);
        multiclassResponse.setFeatures(features);
        multiclassResponse.setMulticlass(multiclassInfo);
        multiclassResponse.setRequirements(String.join(", ", requirements));
        return multiclassResponse;
    }

    private boolean isExtraAttackFeature(ClassFeature feature) {
        return feature.getName().equals("Дополнительная атака")
                || feature.getName().contains("дополнительные атаки")
                || feature.getName().contains("дополнительных атак");
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

    private boolean isSpellcastingFeature(ClassFeature classFeature) {
        return classFeature.getName().equals("Использование заклинаний");
    }

    private String getExtraAttackName(final int extraAttack) {
        return switch (extraAttack) {
            case 1 -> "Две дополнительные атаки";
            case 2 -> "Три дополнительные атаки";
            case 3 -> "Четыре дополнительные атаки";
            default -> throw new IllegalArgumentException(
                    "Неподдерживаемое значение extraAttack: " + extraAttack
            );
        };
    }

    private ClassFeature filterMulticlassFeature(final ClassFeature classFeature,
                                                 final int classLevel,
                                                 final int characterLevel) {
        ClassFeature filteredFeature = copyClassFeature(classFeature);
        List<ClassFeatureScaling> list = new ArrayList<>();
        for (ClassFeatureScaling classFeatureScaling : Optional.ofNullable(classFeature.getScaling()).orElse(List.of())) {
            if (classFeatureScaling.getLevel() <= classLevel) {
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
                .filter(option -> option.getRequiredClassLevel() == null || option.getRequiredClassLevel() <= classLevel)
                .map(ClassFeatureOption::new)
                .toList());
        return filteredFeature;
    }

    /**
     * Проверяет, есть ли у умения масштабирование или опции в диапазоне уровней (previousClassLevel, newClassLevel].
     */
    private boolean hasScalingOrOptionsInRange(ClassFeature feature, int previousClassLevel, int newClassLevel) {
        boolean hasScaling = Optional.ofNullable(feature.getScaling())
                .orElse(List.of())
                .stream()
                .anyMatch(s -> s.getLevel() > previousClassLevel && s.getLevel() <= newClassLevel);
        if (hasScaling) {
            return true;
        }
        return Optional.ofNullable(feature.getOptions())
                .orElse(List.of())
                .stream()
                .anyMatch(o -> o.getRequiredClassLevel() != null
                        && o.getRequiredClassLevel() > previousClassLevel
                        && o.getRequiredClassLevel() <= newClassLevel);
    }

    /**
     * Фильтрует умение для повторного сегмента класса: включает только масштабирование и опции
     * в диапазоне (previousClassLevel, newClassLevel].
     */
    private ClassFeature filterMulticlassFeatureForRange(final ClassFeature classFeature,
                                                         final int previousClassLevel,
                                                         final int newClassLevel,
                                                         final int characterLevel) {
        ClassFeature filteredFeature = copyClassFeature(classFeature);
        List<ClassFeatureScaling> list = new ArrayList<>();
        for (ClassFeatureScaling scaling : Optional.ofNullable(classFeature.getScaling()).orElse(List.of())) {
            if (scaling.getLevel() > previousClassLevel && scaling.getLevel() <= newClassLevel) {
                list.add(new ClassFeatureScaling(
                        scaling.getLevel() - previousClassLevel + characterLevel,
                        scaling.getName(),
                        scaling.getDescription(),
                        scaling.getAdditional(),
                        scaling.isHideInSubclasses()
                ));
            }
        }
        filteredFeature.setScaling(list);
        filteredFeature.setOptions(Optional.ofNullable(classFeature.getOptions())
                .orElse(List.of())
                .stream()
                .filter(option -> !option.isHideInSubclasses())
                .filter(option -> option.getRequiredClassLevel() == null || option.getRequiredClassLevel() <= newClassLevel)
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
        if (casterType == null) {
            return 0;
        }
        return switch (casterType) {
            case FULL -> level;
            case HALF -> (int) Math.ceil(level / 2.);
            case THIRD -> (int) Math.floor(level / 3.);
            default -> 0;
        };
    }

    private boolean isNotCaster(CasterType casterType) {
        return casterType == null || casterType == CasterType.NONE;
    }

    private void addCasterType(List<CasterType> casterTypes, CasterType casterType) {
        if (casterType != null && casterType != CasterType.NONE) {
            casterTypes.add(casterType);
        }
    }

    private CasterType resolveCasterType(List<CasterType> casterTypes) {
        if (casterTypes.isEmpty()) {
            return null;
        }
        return CasterType.MULTICLASS;
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
