package club.ttg.dnd5.config;

import club.ttg.dnd5.config.properties.TelegramProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Клиент к Telegram Bot API: базовый адрес и конечные таймауты. RestClient собирается
 * здесь (а не в сервисе), чтобы его можно было подменить mock-ом в тестах — как у
 * {@link SubscriberServiceConfig}. Токен в URL не зашивается: он подставляется в путь
 * на каждом вызове, поэтому бин не зависит от секрета и создаётся всегда.
 */
@Configuration
public class TelegramConfig {

    @Bean
    public RestClient telegramRestClient(TelegramProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getApiUrl())
                .requestFactory(requestFactory(properties))
                .build();
    }

    private ClientHttpRequestFactory requestFactory(TelegramProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        return factory;
    }
}
