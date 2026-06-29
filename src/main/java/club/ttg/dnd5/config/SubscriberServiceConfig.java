package club.ttg.dnd5.config;

import club.ttg.dnd5.config.properties.SubscriberServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Клиент к subscriber-service: базовый адрес и конечные таймауты, чтобы быстро
 * отваливаться в fail-closed при недоступности сервиса подписок. RestClient собирается
 * здесь (а не в самом сервисе), чтобы его можно было подменить mock-ом в тестах.
 */
@Configuration
public class SubscriberServiceConfig {

    /**
     * Собирает {@link RestClient} к subscriber-service с конечными таймаутами.
     * Падает на старте, если {@code subscriber-service.base-url} не задан, — лучше
     * не подняться, чем молча ходить «в никуда».
     */
    @Bean
    public RestClient subscriberServiceRestClient(SubscriberServiceProperties properties) {
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            throw new IllegalStateException("subscriber-service.base-url is not set");
        }

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory(properties))
                .build();
    }

    private ClientHttpRequestFactory requestFactory(SubscriberServiceProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        return factory;
    }
}
