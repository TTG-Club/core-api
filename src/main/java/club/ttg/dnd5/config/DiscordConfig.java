package club.ttg.dnd5.config;

import club.ttg.dnd5.config.properties.DiscordProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

/**
 * Клиент к Discord-вебхуку: конечные таймауты. RestClient собирается здесь (а не в сервисе), чтобы
 * его можно было подменить mock-ом в тестах — как у {@link TelegramConfig}. Базовый адрес не зашивается:
 * весь URL вебхука (вместе с токеном) — секрет из окружения, поэтому публикатор бьёт по нему абсолютным
 * адресом, и бин не зависит от секрета (создаётся всегда, даже когда вебхук не задан).
 * <p>
 * Фабрика — {@link JdkClientHttpRequestFactory} (java.net.http), а не Simple/HttpURLConnection: правка
 * поста в Discord идёт методом {@code PATCH}, который {@code HttpURLConnection} не поддерживает.
 */
@Configuration
public class DiscordConfig {

    @Bean
    public RestClient discordRestClient(DiscordProperties properties) {
        return RestClient.builder()
                .requestFactory(requestFactory(properties))
                .build();
    }

    private JdkClientHttpRequestFactory requestFactory(DiscordProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.getReadTimeout());
        return factory;
    }
}
