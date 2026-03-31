package club.ttg.dnd5.config;

import club.ttg.dnd5.domain.filter.rest.QueryRequestArgumentResolver;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
public class WebMvcConfig
{
    @Bean
    public BeanPostProcessor queryRequestArgumentResolverPostProcessor()
    {
        return new BeanPostProcessor()
        {
            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException
            {
                if (bean instanceof RequestMappingHandlerAdapter adapter)
                {
                    List<HandlerMethodArgumentResolver> resolvers =
                            new ArrayList<>(Objects.requireNonNull(adapter.getArgumentResolvers()));

                    resolvers.addFirst(new QueryRequestArgumentResolver());

                    adapter.setArgumentResolvers(resolvers);
                }

                return bean;
            }
        };
    }
}