package club.ttg.dnd5.config;

import club.ttg.dnd5.config.properties.VkProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

/**
 * Клиент к VK API: конечные таймауты. RestClient собирается здесь (а не в сервисе), чтобы его можно было
 * подменить mock-ом в тестах — как у {@link DiscordConfig} и {@link TelegramConfig}. Базовый адрес не
 * зашивается: методы VK ({@code wall.post}/{@code wall.edit}/…) и адрес загрузки фото (выдаётся VK на лету)
 * лежат на разных хостах, поэтому публикатор бьёт по абсолютным адресам. Токен — не в URL, а в теле запроса,
 * поэтому бин не зависит от секрета и создаётся всегда.
 * <p>
 * Фабрика — {@link JdkClientHttpRequestFactory} (java.net.http): вызовы идут методом {@code POST}
 * (form-urlencoded для методов и multipart для загрузки обложки), длинный текст поста не влезает в URL.
 */
@Configuration
public class VkConfig {

    @Bean
    public RestClient vkRestClient(VkProperties properties) {
        return RestClient.builder()
                .requestFactory(requestFactory(properties))
                .build();
    }

    private JdkClientHttpRequestFactory requestFactory(VkProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.getReadTimeout());
        return factory;
    }
}
