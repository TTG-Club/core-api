package club.ttg.dnd5.domain.bastion.player.client;

import club.ttg.dnd5.config.properties.FindGameServiceProperties;
import club.ttg.dnd5.config.properties.InternalServiceProperties;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.security.InternalServiceTokenFilter;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Состав игры из find-game-api: кто мастер, кто игрок с одобренной заявкой.
 *
 * <p>Права на бастионы проверяются на каждом запросе, поэтому ответы кешируются на
 * короткое время ({@code find-game-service.membership-cache-ttl}): мастер одобрил заявку —
 * игрок увидит бастионы игры не позже чем через минуту.</p>
 *
 * <p>Недоступность каталога — отказ (503), а не «не участник»: иначе сбой соседнего
 * сервиса выглядел бы для мастера как потеря доступа к своим бастионам. Неудачи не
 * кешируются.</p>
 */
@Slf4j
@Component
public class GameMembershipClient {

    private final RestClient restClient;
    private final InternalServiceProperties internalProperties;
    private final Cache<MembershipKey, GameMembership> memberships;
    private final Cache<UUID, GameMembers> members;

    public GameMembershipClient(RestClient findGameServiceRestClient,
                                InternalServiceProperties internalProperties,
                                FindGameServiceProperties properties) {
        this.restClient = findGameServiceRestClient;
        this.internalProperties = internalProperties;
        this.memberships = Caffeine.newBuilder()
                .expireAfterWrite(properties.getMembershipCacheTtl())
                .maximumSize(10_000)
                .build();
        this.members = Caffeine.newBuilder()
                .expireAfterWrite(properties.getMembershipCacheTtl())
                .maximumSize(2_000)
                .build();
    }

    /**
     * Роль пользователя в игре.
     *
     * @throws EntityNotFoundException игры нет или она удалена
     * @throws ApiException 503, если каталог игр не ответил
     */
    public GameMembership membership(UUID gameId, UUID userId) {
        return memberships.get(new MembershipKey(gameId, userId), key -> fetch(gameId,
                () -> restClient.get()
                        .uri("/api/v1/internal/games/{gameId}/members/{userId}", gameId, userId)
                        .headers(this::addServiceHeaders)
                        .retrieve()
                        .body(GameMembership.class)));
    }

    /**
     * Мастер и игроки игры с одобренной заявкой.
     *
     * @throws EntityNotFoundException игры нет или она удалена
     * @throws ApiException 503, если каталог игр не ответил
     */
    public GameMembers members(UUID gameId) {
        return members.get(gameId, key -> fetch(gameId,
                () -> restClient.get()
                        .uri("/api/v1/internal/games/{gameId}/members", gameId)
                        .headers(this::addServiceHeaders)
                        .retrieve()
                        .body(GameMembers.class)));
    }

    private <T> T fetch(UUID gameId, Supplier<T> call) {
        T body;
        try {
            body = call.get();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new EntityNotFoundException("Игра %s не найдена".formatted(gameId));
        } catch (RestClientException ex) {
            log.warn("Не удалось получить состав игры {} из find-game-api", gameId, ex);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Каталог игр сейчас недоступен, попробуйте позже");
        }
        if (body == null) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Каталог игр вернул пустой ответ, попробуйте позже");
        }
        return body;
    }

    private void addServiceHeaders(HttpHeaders headers) {
        String secret = internalProperties.getServiceSecret();
        if (secret != null && !secret.isBlank()) {
            headers.set(InternalServiceTokenFilter.SERVICE_TOKEN_HEADER, secret);
        }
    }

    private record MembershipKey(UUID gameId, UUID userId) {
    }

    /**
     * Роль пользователя в игре.
     *
     * @param gameId   игра
     * @param title    название игры
     * @param masterId мастер игры
     * @param userId   о ком спрашивали
     * @param role     кем он приходится игре
     */
    public record GameMembership(UUID gameId, String title, UUID masterId, UUID userId, GameRole role) {
        /** Участник игры: мастер или игрок с одобренной заявкой. */
        public boolean isParticipant() {
            return role == GameRole.MASTER || role == GameRole.PLAYER;
        }
    }

    /**
     * Состав игры.
     *
     * @param gameId   игра
     * @param title    название игры
     * @param masterId мастер
     * @param players  игроки с одобренной заявкой
     */
    public record GameMembers(UUID gameId, String title, UUID masterId, List<Player> players) {
        /**
         * Игрок игры.
         *
         * @param userId        пользователь
         * @param characterName имя персонажа из заявки; может быть пустым
         */
        public record Player(UUID userId, String characterName) {
        }
    }
}
