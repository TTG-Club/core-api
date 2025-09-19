package club.ttg.dnd5.domain.charlist.model;

import club.ttg.dnd5.domain.common.dictionary.Size;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@Entity
public class CharList {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    /**
     * Имя персонажа
     */
    private String name;
    /**
     * Текущий опыт
     */
    private long experience;
    /**
     * Портрет
     */
    private String image;
    private byte armorClass;

    private Size size;
    /**
     * Хиты
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Hit hit;

    /**
     * Базовые характеристики
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Abilities abilities;
    /**
     * Навыки
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private CharSkills skills;
    /**
     * Классы
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Collection<CharClass> classes;

    @CreatedBy
    private String createdBy;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime lastModified;
}
