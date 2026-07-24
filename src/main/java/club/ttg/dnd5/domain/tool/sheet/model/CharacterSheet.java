package club.ttg.dnd5.domain.tool.sheet.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.UUID;

/**
 * Лист персонажа (инструмент): сохранённый лист пользователя одним JSON-документом.
 * Структуру документа сервер не разбирает — форматом владеет фронтенд.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "character_sheet",
        indexes = {
                @Index(name = "character_sheet_user_id_index", columnList = "user_id")
        })
public class CharacterSheet extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Владелец — uuid пользователя из JWT (subject). Лист доступен только владельцу.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Название листа. Дублируется клиентом из JSON-документа, чтобы список (включая историю
     * удалённых, где документ не отдаётся) не требовал разбора jsonb на сервере.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Весь лист персонажа как есть (фронтовый формат Character). Сохраняется при мягком
     * удалении — восстановление возвращает лист без потерь.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode data;

    /**
     * Мягкое удаление: лист скрыт из активных, остаётся в истории и может быть восстановлен,
     * пока не вытеснен из неё более свежими удалениями.
     */
    @Column(nullable = false)
    private boolean deleted;
}
