package club.ttg.dnd5.config;

import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Любое поле {@code Set<...>} читается в {@link LinkedHashSet}, то есть в том
 * порядке, в каком пришло — из тела запроса или из jsonb.
 *
 * <p>По умолчанию Jackson отдаёт {@link java.util.HashSet}, а у перечислений
 * {@code hashCode} берётся от адреса объекта. Порядок получался произвольным и
 * менялся после каждого перезапуска приложения: спасброски, владение оружием и
 * основные характеристики классов показывались вразнобой, PUT сохранял их не в
 * том порядке, в каком их прислали, а побайтовая сверка тела карточки
 * срывалась на полях, которых правка не касалась.
 *
 * <p>Модуль нужен обоим мапперам — веб-слою ({@link FilterSubtypeAutoConfig})
 * и чтению jsonb ({@link HypersistenceFilterObjectMapperSupplier}): порядок
 * теряется в том из них, где модуля нет.
 */
public class OrderedSetsModule extends SimpleModule
{
    public OrderedSetsModule()
    {
        super("ordered-sets");

        addAbstractTypeMapping(Set.class, LinkedHashSet.class);
    }
}
