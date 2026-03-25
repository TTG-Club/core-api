package club.ttg.dnd5.domain.filter.rest;

import club.ttg.dnd5.dto.base.filters.AbstractSearchRequest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

/**
 * Утилита для декодирования Base64url-encoded JSON из query-параметра {@code f}
 * в типизированный {@link AbstractSearchRequest}.
 */
@Slf4j
@UtilityClass
public class SearchRequestResolver
{
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Декодирует фильтры из Base64url-encoded JSON и собирает итоговый DTO.
     *
     * @param encodedFilter Base64url-encoded JSON с фильтрами (может быть {@code null})
     * @param search        текстовая строка поиска (может быть {@code null})
     * @param page          номер страницы (может быть {@code null} — используется дефолт из DTO)
     * @param size          размер страницы (может быть {@code null} — используется дефолт из DTO)
     * @param clazz         тип целевого DTO
     * @return заполненный DTO; при ошибке декодирования — пустой DTO с текстом и пагинацией
     */
    public <T extends AbstractSearchRequest> T resolve(final String encodedFilter,
                                                        final String search,
                                                        final Integer page,
                                                        final Integer size,
                                                        final Class<T> clazz)
    {
        T request = decode(encodedFilter, clazz);

        if (search != null)
        {
            request.setText(search);
        }

        if (page != null)
        {
            request.setPage(page);
        }

        if (size != null)
        {
            request.setSize(size);
        }

        return request;
    }

    private <T extends AbstractSearchRequest> T decode(final String encodedFilter,
                                                        final Class<T> clazz)
    {
        if (encodedFilter != null && !encodedFilter.isBlank())
        {
            try
            {
                byte[] decoded = Base64.getUrlDecoder().decode(encodedFilter);
                return MAPPER.readValue(decoded, clazz);
            }
            catch (Exception e)
            {
                log.warn("Не удалось декодировать параметр f: {}", e.getMessage());
            }
        }

        try
        {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (ReflectiveOperationException e)
        {
            throw new IllegalStateException("Не удалось создать экземпляр " + clazz.getName(), e);
        }
    }
}
