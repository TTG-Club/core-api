package club.ttg.dnd5.domain.vttg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Вырезает автоматизацию из записей вне SRD для пользователей без подписки.
 *
 * <p>Автоматизацией считаются только активные эффекты (вкладка «Эффекты» мастерской) — поле
 * {@code activeEffects} на любом уровне записи: у неё самой, у умений, подклассов, вариантов
 * выбора, вложенных заклинаний существа. Урон, спасброски, дары и прочая механика остаются.</p>
 *
 * <p>Запись SRD ({@code isSRD} не {@code false} — так же её раскладывает по пакам VTTG) не
 * трогается никогда. Запись, у которой эффекты действительно были вырезаны, получает метку
 * {@code automation: false}: по ней потребитель показывает, что автоматизация доступна по
 * подписке. Запись без эффектов метку не получает — у неё вырезать нечего.</p>
 *
 * <p>Payload в {@code vttg_export} хранится полным и общим на всех, поэтому исходное дерево не
 * меняется: правится копия.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgAutomationStripper {
    /** Поле записи с активными эффектами. */
    static final String EFFECTS_FIELD = "activeEffects";
    /** Метка записи, у которой эффекты вырезаны. */
    static final String MARKER_FIELD = "automation";
    private static final String SRD_FIELD = "isSRD";

    private final ObjectMapper objectMapper;

    /**
     * @param data payload записи: дерево Jackson, DTO маппера либо карта (разделитель черт)
     * @return тот же payload, если вырезать нечего; иначе копия без эффектов с меткой
     */
    public Object strip(Object data) {
        // Разделители черт — служебные карты без эффектов и без признака SRD.
        if (data == null || data instanceof Map<?, ?>) {
            return data;
        }
        JsonNode node = data instanceof JsonNode tree ? tree : objectMapper.valueToTree(data);
        JsonNode srd = node.get(SRD_FIELD);
        if (srd == null || !srd.isBoolean() || srd.asBoolean() || !hasEffects(node)) {
            return data;
        }
        ObjectNode copy = node.deepCopy();
        removeEffects(copy);
        copy.put(MARKER_FIELD, false);
        return copy;
    }

    private static boolean hasEffects(JsonNode node) {
        if (node.has(EFFECTS_FIELD)) {
            return true;
        }
        for (JsonNode child : node) {
            if (child.isContainerNode() && hasEffects(child)) {
                return true;
            }
        }
        return false;
    }

    private static void removeEffects(JsonNode node) {
        if (node instanceof ObjectNode object) {
            object.remove(EFFECTS_FIELD);
        }
        for (JsonNode child : node) {
            if (child.isContainerNode()) {
                removeEffects(child);
            }
        }
    }
}
