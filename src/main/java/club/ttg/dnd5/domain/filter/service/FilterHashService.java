package club.ttg.dnd5.domain.filter.service;

import club.ttg.dnd5.domain.filter.model.FilterHashCategory;
import club.ttg.dnd5.domain.filter.model.FilterHashMapping;
import club.ttg.dnd5.domain.filter.repository.FilterHashMappingRepository;
import club.ttg.dnd5.dto.base.filters.FilterIdUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для управления хэш-маппингами фильтров.
 * Вызывается из lifecycle сущностей (save/update), а не из GET-запросов.
 */
@Service
@RequiredArgsConstructor
public class FilterHashService
{
    private final FilterHashMappingRepository repository;

    /**
     * Сохраняет хэш → значение, если ещё не существует.
     *
     * @param category категория фильтра
     * @param value    оригинальное строковое значение
     */
    @Transactional
    public void ensureHash(final FilterHashCategory category, final String value)
    {
        String hash = FilterIdUtils.shortHash(value);

        if (!repository.existsById(hash))
        {
            repository.save(new FilterHashMapping(hash, category, value));
        }
    }
}
