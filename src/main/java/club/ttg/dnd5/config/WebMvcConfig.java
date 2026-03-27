package club.ttg.dnd5.config;

import club.ttg.dnd5.domain.filter.rest.QueryRequestArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Конфигурация Spring MVC: регистрация кастомных аргумент-резолверов.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer
{
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers)
    {
        resolvers.add(new QueryRequestArgumentResolver());
    }
}
