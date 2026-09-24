package club.ttg.dnd5.config;

import club.ttg.dnd5.config.properties.FindGameServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Клиент к find-game-api — так же, как {@link SubscriberServiceConfig}: базовый адрес,
 * конечные таймауты и отдельный бин, чтобы подменять его в тестах.
 */
@Configuration
public class FindGameServiceConfig {

    /**
     * Собирает {@link RestClient} к find-game-api. Падает на старте без
     * {@code find-game-service.base-url}: без каталога игр бастионы игроков не работают.
     */
    @Bean
    public RestClient findGameServiceRestClient(FindGameServiceProperties properties) {
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            throw new IllegalStateException("find-game-service.base-url is not set");
        }

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }
}
