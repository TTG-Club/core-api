package club.ttg.dnd5.domain.common.model;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.rest.dto.CreatureRequest;
import club.ttg.dnd5.domain.beastiary.rest.mapper.CreatureMapperImpl;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.CharacterClassListItem;
import club.ttg.dnd5.domain.character_class.rest.mapper.ClassMapperImpl;
import club.ttg.dnd5.domain.common.rest.mapper.EquipmentMapping;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.source.rest.mapper.SourceMapperImpl;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Кадр обложки: формат JSON, прижатие значений и проход через мапперы разделов.
 */
@ExtendWith(MockitoExtension.class)
class ImageFrameTest {

    private static final ImageFrame FRAME = new ImageFrame(0.42, 0.3, 1.8);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BaseMapping baseMapping;

    @Mock
    private EquipmentMapping equipmentMapping;

    @InjectMocks
    private SourceMapperImpl sourceMapper;

    @InjectMocks
    private ClassMapperImpl classMapper;

    @InjectMocks
    private CreatureMapperImpl creatureMapper;

    @Test
    void readsFrontendContract() throws Exception {
        var frame = objectMapper.readValue("{\"x\":0.42,\"y\":0.3,\"zoom\":1.8}", ImageFrame.class);

        assertEquals(FRAME, frame);
        assertEquals("{\"x\":0.42,\"y\":0.3,\"zoom\":1.8}", objectMapper.writeValueAsString(frame));
    }

    /** Кадр вне пределов не отклоняется, а прижимается к ним. */
    @Test
    void clampsOutOfRangeValues() throws Exception {
        var frame = objectMapper.readValue("{\"x\":-1,\"y\":7,\"zoom\":10}", ImageFrame.class);

        assertEquals(new ImageFrame(0, 1, 4), frame);
        assertEquals(1, objectMapper.readValue("{\"x\":0.5,\"y\":0.5}", ImageFrame.class).zoom());
    }

    @Test
    void requestCarriesFrameNextToImage() throws Exception {
        var request = objectMapper.readValue(
                "{\"image\":\"/s3/a.webp\",\"imageFrame\":{\"x\":0.42,\"y\":0.3,\"zoom\":1.8}}",
                SourceRequest.class);

        assertEquals(FRAME, request.getImageFrame());
    }

    @Test
    void sourceSavesAndReturnsFrame() {
        var request = new SourceRequest();
        request.setImageFrame(FRAME);
        var source = sourceMapper.toEntity(request);

        assertEquals(FRAME, source.getImageFrame());
        assertEquals(FRAME, sourceMapper.toShort(source).getImageFrame());
        assertEquals(FRAME, sourceMapper.toDetail(source).getImageFrame());
        assertEquals(FRAME, sourceMapper.toRequest(source).getImageFrame());
    }

    /** {@code null} из запроса стирает сохранённый кадр. */
    @Test
    void nullInRequestClearsSavedFrame() {
        var source = new Source();
        source.setImageFrame(FRAME);
        sourceMapper.toEntity(new SourceRequest(), source);
        assertNull(source.getImageFrame());

        var creature = new Creature();
        creature.setImageFrame(FRAME);
        creatureMapper.updateEntity(new CreatureRequest(), null, creature);
        assertNull(creature.getImageFrame());
    }

    /** Подкласс без своей картинки показывает картинку класса — и её кадр. */
    @Test
    void subclassWithoutImageTakesParentFrame() {
        var parent = new CharacterClass();
        parent.setImageUrl("/s3/class.webp");
        parent.setImageFrame(FRAME);
        var subclass = new CharacterClass();
        subclass.setParent(parent);
        subclass.setImageFrame(new ImageFrame(0.9, 0.9, 3));

        assertEquals(FRAME, classMapper.toShort(subclass).getImageFrame());
        assertEquals(FRAME, classMapper.toImageFrame(subclass));
        // Форма правки получает собственный кадр записи, без подстановки
        assertEquals(new ImageFrame(0.9, 0.9, 3), classMapper.toRequest(subclass).getImageFrame());

        var parentItem = new CharacterClassListItem();
        parentItem.setImageUrl("/s3/class.webp");
        parentItem.setImageFrame(FRAME);
        var subclassItem = new CharacterClassListItem();
        subclassItem.setParent(parentItem);

        assertEquals(FRAME, classMapper.toShortListItem(subclassItem).getImageFrame());
    }
}
