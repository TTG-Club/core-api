package club.ttg.dnd5.domain.character_class.service;

import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.ClassFeature;
import club.ttg.dnd5.domain.character_class.model.ClassFeatureScaling;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.character_class.model.ClassTableItem;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MulticlassService {
    private final ClassRepository classRepository;
    private final MulticlassMapper multiclassMapper;
    private final ClassFeatureMapper classFeatureMapper;

    public CharacterClass findByUrl(String url) {
        return classRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Класс с url %s не существует", url)));
    }

    public MulticlassResponse getMulticlass(final MulticlassRequest request) {
        var multiclass = new CharacterClass();
        var mainClass = findByUrl(request.getUrl());
        List<ClassFeatureDto> features = new ArrayList<>();

        boolean spellcasting = false;
        int charachterLevel = request.getLevel();
        int spellcastLevel = calculateSpellCastingLevel(mainClass.getCasterType(), request.getLevel());

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
            var classFilterFeature = filterMulticlassScalingFeature(classFeature, request.getLevel(), 0);
            if (classFilterFeature.isHideInSubclasses()) {
                continue;
            }
            if (classFilterFeature.getName().equals("Использование заклинаний")) {
                classFilterFeature.setDescription(getSpellcastingMulticlass());
                spellcasting = true;
            }
            if (classFilterFeature.getLevel() <= request.getLevel()) {
                var feature = classFeatureMapper.toDto(classFilterFeature, false);
                feature.setAdditional(mainClass.getName());
                features.add(feature);
            }
        }
        var mainSubClass = findByUrl(request.getSubclass());

        List<MulticlassInfo> multiclassInfo = new ArrayList<>();
        multiclassInfo.add(MulticlassInfo.builder()
                .hitDice("1" + mainClass.getHitDice().getName() + " за каждый уровень")
                .name(mainClass.getName())
                .subclass(mainSubClass.getName())
                .level(request.getLevel())
                .build());

        if (mainClass.getCasterType() == CasterType.NONE) {
            spellcastLevel += calculateSpellCastingLevel(mainSubClass.getCasterType(), request.getLevel());
        }
        for (ClassFeature subclassFeature : mainSubClass.getFeatures()) {
            if (subclassFeature.getName().equals("Использование заклинаний")) {
                subclassFeature.setDescription(getSpellcastingMulticlass());
                spellcasting = true;
            }
            if (subclassFeature.getLevel() <= request.getLevel()) {
                var feature = classFeatureMapper.toDto(subclassFeature, true);
                feature.setAdditional(mainSubClass.getName());
                features.add(feature);
            }
        }
        var spellMulticlass = mainClass.getCasterType() != CasterType.NONE;
        var names = new ArrayList<String>(request.getClasses().size());
        for (var multiclassRequest :  request.getClasses()) {
            var multiClass = findByUrl(multiclassRequest.getUrl());
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
                if (multiclassFeature.getName().equals("Использование заклинаний")) {
                    if (spellcasting) {
                        continue;
                    }
                    multiclassFeature.setDescription(getSpellcastingMulticlass());
                }
                if (multiclassFeature.getLevel() <= level) {
                    ClassFeature classFeature = filterMulticlassScalingFeature(multiclassFeature,
                            level,
                            charachterLevel);
                    classFeature.setLevel(multiclassFeature.getLevel() + charachterLevel);
                    var feature = classFeatureMapper.toDto(classFeature, false);
                    feature.setAdditional(multiClass.getName());
                    features.add(feature);
                }
            }
            var multiSubclass = findByUrl(multiclassRequest.getSubclass());
            for (ClassFeature multiSubclassFeature : multiSubclass.getFeatures()) {
                if (multiSubclassFeature.getLevel() <= level) {
                    ClassFeature classFeature = filterMulticlassScalingFeature(multiSubclassFeature, level, charachterLevel);
                    classFeature.setLevel(multiSubclassFeature.getLevel() + charachterLevel);
                    var feature = classFeatureMapper.toDto(classFeature, true);
                    feature.setAdditional(multiSubclass.getName());
                    features.add(feature);
                }
            }
            multiclassInfo.add(MulticlassInfo.builder()
                    .hitDice("1" + multiClass.getHitDice().getName() + " за каждый уровень")
                    .name(multiClass.getName())
                    .subclass(multiSubclass.getName())
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
        multiclass.setArmorProficiency(mainClass.getArmorProficiency());
        multiclass.setPrimaryCharacteristics(mainClass.getPrimaryCharacteristics());

        multiclass.setTable(table);

        var multiclassResponse = multiclassMapper.toMulticlassResponse(multiclass);
        multiclassResponse.setCharacterLevel(request.getLevel()
                + request.getClasses().stream().mapToInt(MulticlassDto::getLevel).sum());
        multiclassResponse.setSpellcastingLevel(spellcastLevel);
        multiclassResponse.setFeatures(features);
        multiclassResponse.setMulticlass(multiclassInfo);
        return multiclassResponse;
    }

    private ClassFeature filterMulticlassScalingFeature(final ClassFeature classFeature,
                                                        final int level,
                                                        final int characterLevel) {
        List<ClassFeatureScaling> list = new ArrayList<>();
        for (ClassFeatureScaling classFeatureScaling : classFeature.getScaling()) {
            if (classFeatureScaling.getLevel() <= level) {
                classFeatureScaling.setLevel(classFeatureScaling.getLevel() + characterLevel);
                list.add(classFeatureScaling);
            }
        }
        classFeature.setScaling(list);
        return classFeature;
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
                  "Ваши возможности для накладывания заклинаний зависят частично от ваших комбинированных уровней во всех ваших классах, умеющих накладывать {@glossary заклинания|url:spell-phb}, и частично от ваших индивидуальных уровней в этих классах. Как только вы получаете умение {@glossary Использование заклинаний|url:spellcasting-phb} более чем из одного класса, используйте приведённые ниже правила. Если вы персонаж с мультиклассом, но у вас есть особенность {@glossary Использование заклинаний|url:spellcasting-phb} только из одного класса, следуйте правилам для этого класса.",
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
                  "Затем найдите этот суммарный уровень в колонке «Уровень» таблицы «Мультиклассовый заклинатель». Вы используете ячейки заклинаний этого уровня, чтобы накладывать заклинания соответствующего уровня из любого класса, у которого есть способность {@glossary Использование заклинаний|url:spellcasting-phb}.",
                  "Эта таблица может дать вам ячейки заклинаний более высокого уровня, чем заклинания, которые вы можете подготовить. Вы можете использовать такие ячейки только для наложения заклинаний более низкого уровня. Если заклинание имеет усиленный эффект при использовании ячейки более высокого уровня, вы можете применять этот эффект как обычно.",
                  "Например, если вы следопыт 4 уровня / чародей 3 уровня, вы считаетесь персонажем 5 уровня при определении ячеек заклинаний. У вас есть 4 ячейки заклинаний 1 уровня, 3 ячейки 2 уровня и 2 ячейки 3 уровня. Однако вы не можете подготовить заклинания 3 уровня и не можете подготовить заклинания 2 уровня следопыта. Вы всё равно можете использовать эти ячейки для накладывания подготовленных заклинаний и усиливать их эффекты.",
                  "{@b Магия договора.} Если у вас есть умение {@glossary Магия договора|url:pact-magic-phb} из класса колдуна и умение {@glossary Использование заклинаний|url:spellcasting-phb}, вы можете использовать ячейки заклинаний, полученные от Магии договора, для накладывания заклинаний других классов, и наоборот — использовать ячейки Использования заклинаний для сотворения заклинаний колдуна."
                ]
               """;
    }
}
