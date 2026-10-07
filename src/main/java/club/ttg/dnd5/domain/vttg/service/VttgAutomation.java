package club.ttg.dnd5.domain.vttg.service;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Объём автоматизации в выгрузке VTTG. Сами записи отдаются всем целиком; от подписки зависит
 * только, едут ли с записями вне SRD их активные эффекты (см. {@link VttgAutomationStripper}).
 *
 * <p>Значение уходит клиенту в ответах {@code /changes} и {@code /changes/status}: клиент хранит
 * его рядом с курсором и при расхождении пересобирает паки полной выгрузкой — смена подписки
 * меняет записи, но не сдвигает их {@code updatedAt}, и инкрементальное окно их не отдаст.</p>
 */
public enum VttgAutomation {
    /** Эффекты у всех записей: действующая подписка или админ. */
    FULL("full"),
    /** Эффекты только у записей SRD; у остальных вырезаны. */
    SRD("srd");

    private final String value;

    VttgAutomation(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
