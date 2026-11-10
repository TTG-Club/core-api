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
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OptimisticLock;
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
                @Index(name = "character_sheet_user_id_index", columnList = "user_id"),
                @Index(name = "character_sheet_share_token_index", columnList = "share_token", unique = true)
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

    /**
     * Секрет ссылки «поделиться листом»: {@code null} — доступа по ссылке нет. По токену лист
     * отдаётся кому угодно, но только на чтение. Токен, а не сам id, чтобы ссылка не раскрывала
     * идентификатор листа и отзывалась независимо от него.
     * <p>
     * Из версии исключён: включить или отозвать ссылку можно прямо из открытого листа, и
     * автосохранение того же листа не должно после этого получать конфликт — документ не менялся.
     */
    @OptimisticLock(excluded = true)
    @Column(name = "share_token")
    private UUID shareToken;

    /**
     * Оптимистическая блокировка: лист сохраняется целиком, и без версии правка из одной вкладки
     * (или хиты, записанные мастером боя) молча затиралась бы автосохранением из другой.
     * Клиент присылает версию, с которой начал правку, — устаревшая даёт 409.
     */
    @Version
    @Column(nullable = false)
    private long version;
}
