package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.util.UrlTokens;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Прежние адреса записей для выгрузки VTTG.
 *
 * <p>У пользователей VTTG записи компендиума названы по адресу страницы на сайте (id
 * заклинания, существа, вида, предыстории, предмета), и на эти id ссылаются листы
 * персонажей. Когда адрес записи на сайте меняется (смена акронима источника), выгрузка
 * продолжает отдавать <b>первоначальный</b> адрес — иначе у игроков осиротели бы
 * персонажи и ссылки между записями.</p>
 *
 * <p>Карта «текущий адрес → первоначальный» ведётся в {@code vttg_url_alias} и
 * подгружается {@link VttgUrlAliasRegistry}. Держится здесь статически, чтобы мапперы
 * (их создают и вручную, в тестах) не тащили лишнюю зависимость; без реестра карта
 * пуста и адреса не меняются.</p>
 */
public final class VttgUrlAliases {
    private static volatile Supplier<Map<String, String>> source = Map::of;

    private VttgUrlAliases() {
    }

    /** Подключает источник карты (реестр при старте приложения). */
    static void install(Supplier<Map<String, String>> aliases) {
        source = aliases == null ? Map::of : aliases;
    }

    /** Есть ли записи со сменённым адресом. */
    public static boolean active() {
        return !source.get().isEmpty();
    }

    /**
     * Первоначальный адрес записи для выгрузки.
     *
     * @param url текущий адрес записи на сайте
     * @return адрес, под которым запись впервые ушла в VTTG; сам {@code url}, если его не меняли
     */
    public static String original(String url) {
        if (url == null) {
            return null;
        }
        return source.get().getOrDefault(url, url);
    }

    /**
     * Заменяет в готовом payload текущие адреса записей первоначальными: в id и
     * {@code srcUrl}, в ссылках между записями, в ссылках на сайт внутри описаний.
     * Адрес ищется целым словом — {@code fireball-ph} внутри {@code greater-fireball-ph}
     * не трогается.
     *
     * @param payload payload записи
     * @return тот же узел (изменённый на месте) либо новый текстовый узел для корня-строки
     */
    public static JsonNode apply(JsonNode payload) {
        Map<String, String> aliases = source.get();
        if (payload == null || aliases.isEmpty()) {
            return payload;
        }
        Pattern pattern = UrlTokens.pattern(aliases.keySet());
        return rewrite(payload, aliases, pattern);
    }

    /** Строка с заменёнными адресами (для текста вне payload). */
    static String apply(String text, Map<String, String> aliases) {
        if (text == null || aliases.isEmpty()) {
            return text;
        }
        return UrlTokens.replace(text, aliases, UrlTokens.pattern(aliases.keySet()));
    }

    private static JsonNode rewrite(JsonNode node, Map<String, String> aliases, Pattern pattern) {
        if (node.isTextual()) {
            String replaced = UrlTokens.replace(node.asText(), aliases, pattern);
            return replaced.equals(node.asText()) ? node : TextNode.valueOf(replaced);
        }
        if (node instanceof ObjectNode object) {
            List<String> names = new ArrayList<>();
            object.fieldNames().forEachRemaining(names::add);
            for (String name : names) {
                JsonNode child = object.get(name);
                JsonNode rewritten = rewrite(child, aliases, pattern);
                String renamed = UrlTokens.replace(name, aliases, pattern);
                if (!renamed.equals(name)) {
                    object.remove(name);
                    object.set(renamed, rewritten);
                } else if (rewritten != child) {
                    object.set(name, rewritten);
                }
            }
        } else if (node instanceof ArrayNode array) {
            for (int i = 0; i < array.size(); i++) {
                JsonNode child = array.get(i);
                JsonNode rewritten = rewrite(child, aliases, pattern);
                if (rewritten != child) {
                    array.set(i, rewritten);
                }
            }
        }
        return node;
    }

    /** Для тестов: убрать подключённую карту. */
    static void reset() {
        source = Map::of;
    }
}
