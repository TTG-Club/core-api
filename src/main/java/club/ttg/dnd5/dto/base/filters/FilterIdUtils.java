package club.ttg.dnd5.dto.base.filters;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Утилита генерации коротких идентификаторов из строковых значений.
 * Используется для traits, tags и других длинных строк в URL-параметрах фильтрации.
 * <p>
 * SHA-256 → первые 8 hex-символов (аналогично short commit hash в GitHub).
 */
@UtilityClass
public class FilterIdUtils
{
    private static final int SHORT_HASH_LENGTH = 8;

    /**
     * Вычисляет короткий хэш (8 hex-символов) от строки.
     *
     * @param value исходная строка
     * @return первые 8 символов SHA-256 hex-дайджеста
     */
    public String shortHash(final String value)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, SHORT_HASH_LENGTH);
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("SHA-256 algorithm not available", exception);
        }
    }
}
