package club.ttg.dnd5.domain.common.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON-объект, который хранит ключи, незнакомые модели, и отдаёт их обратно как
 * есть.
 * <p>
 * Модель эффекта развивается на стороне системы D&amp;D быстрее бэкенда: без этого
 * поле, которого модель ещё не знает, Jackson молча выбрасывал бы при каждом
 * сохранении, и редактор сайта портил бы уже заполненную механику.
 * <p>
 * Незнакомые ключи пишутся после известных полей, в том порядке, в каком пришли.
 * Пустая карта ничего не добавляет в JSON. Методы названы не как бин-свойство
 * ({@code getX}/{@code setX}), чтобы ни Jackson, ни MapStruct, ни схема OpenAPI
 * не приняли карту за обычное поле.
 */
public abstract class UnknownFieldsHolder {
    private final Map<String, JsonNode> unknownFields = new LinkedHashMap<>();

    /** Незнакомые модели ключи в порядке прихода; только для чтения. */
    @JsonAnyGetter
    public Map<String, JsonNode> unknownFields() {
        return Collections.unmodifiableMap(unknownFields);
    }

    /** Запоминает ключ, которого нет среди полей модели. */
    @JsonAnySetter
    public void putUnknownField(String name, JsonNode value) {
        unknownFields.put(name, value);
    }
}
