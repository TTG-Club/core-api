package club.ttg.dnd5.domain.telegram.service;

import club.ttg.dnd5.config.properties.TelegramProperties;
import club.ttg.dnd5.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Выпуск одноразовых ссылок-приглашений в секретный чат разработки через бота
 * ({@code createChatInviteLink}, {@code member_limit=1}: после первого входа Telegram
 * гасит ссылку сам). Бот должен быть администратором чата с правом приглашать.
 * <p>
 * Чат задаётся отдельным свойством {@code telegram.dev-chat-id} — это другой чат,
 * нежели новостной канал автопубликации ({@code telegram.chat-id}).
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TelegramInviteService {
    /** Лимит Telegram на длину подписи ссылки-приглашения. */
    private static final int INVITE_NAME_MAX_LENGTH = 32;

    private final RestClient telegramRestClient;
    private final TelegramProperties properties;

    /**
     * Выпускает одноразовую ссылку-приглашение в чат разработки.
     *
     * @param name подпись ссылки в списке приглашений чата (обрезается до 32 символов)
     * @return ссылка вида {@code https://t.me/+...}
     * @throws ApiException 503 — интеграция не настроена или Telegram временно недоступен;
     *                      502 — Telegram отклонил запрос (нет прав, неверный чат и т.п.)
     */
    public String createDevChatInvite(String name) {
        if (!StringUtils.hasText(properties.getBotToken()) || !StringUtils.hasText(properties.getDevChatId())) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Интеграция с Telegram не настроена: не заданы токен бота или чат разработки");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chat_id", properties.getDevChatId());
        payload.put("name", truncateName(name));
        payload.put("member_limit", 1);

        JsonNode response;
        try {
            response = telegramRestClient.post()
                    // Токен — часть пути (…/bot<token>/method). В шаблон не оборачиваем,
                    // чтобы ':' в токене не был percent-энкоднут.
                    .uri("/bot" + properties.getBotToken() + "/createChatInviteLink")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException ex) {
            log.error("Telegram createChatInviteLink вернул {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            boolean retriable = ex.getStatusCode().value() == 429 || ex.getStatusCode().is5xxServerError();
            if (retriable) {
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Telegram временно недоступен, попробуйте позже");
            }
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Telegram отклонил создание ссылки-приглашения");
        } catch (RestClientException ex) {
            // Логируем причину (IOException), а НЕ ex.getMessage(): RestClient вшивает в него полный URL
            // запроса, а в пути — токен бота (…/bot<token>/…). Причина URL не содержит.
            Throwable cause = ex.getCause();
            log.error("Не удалось вызвать Telegram createChatInviteLink: {}",
                    cause != null ? cause.getMessage() : ex.getClass().getSimpleName());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Telegram временно недоступен, попробуйте позже");
        }

        String inviteLink = response == null ? "" : response.path("result").path("invite_link").asText("");
        if (!StringUtils.hasText(inviteLink)) {
            log.error("Telegram createChatInviteLink ответил без invite_link: {}", response);
            throw new ApiException(HttpStatus.BAD_GATEWAY,
                    "Telegram вернул ответ без ссылки-приглашения");
        }
        return inviteLink;
    }

    private static String truncateName(String name) {
        String trimmed = name.trim();
        return trimmed.length() <= INVITE_NAME_MAX_LENGTH
                ? trimmed
                : trimmed.substring(0, INVITE_NAME_MAX_LENGTH);
    }
}
