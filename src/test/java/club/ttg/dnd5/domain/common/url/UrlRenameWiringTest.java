package club.ttg.dnd5.domain.common.url;

import club.ttg.dnd5.domain.source.service.SourceAcronymRenamer;
import club.ttg.dnd5.domain.vttg.service.VttgCompendiumRebuildService;
import club.ttg.dnd5.domain.vttg.service.VttgSourceRenameListener;
import club.ttg.dnd5.domain.vttg.service.VttgUrlAliasRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Компоненты смены акронима и адресов собираются Spring так же, как при старте
 * приложения. Полный контекст поднимает только интеграционный тест на Docker, поэтому
 * без этой проверки ошибка сборки бинов (например, неоднозначный конструктор) видна
 * лишь на CI и при выкатке.
 */
class UrlRenameWiringTest {

    @Test
    void componentsAreCreatedBySpring() {
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        when(entityManagerFactory.createEntityManager()).thenReturn(mock(EntityManager.class));
        when(entityManagerFactory.createEntityManager(any(java.util.Map.class))).thenReturn(mock(EntityManager.class));

        new ApplicationContextRunner()
                .withBean(PersistenceAnnotationBeanPostProcessor.class)
                .withBean(EntityManagerFactory.class, () -> entityManagerFactory)
                .withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class))
                .withBean(PlatformTransactionManager.class, () -> mock(PlatformTransactionManager.class))
                .withBean(VttgCompendiumRebuildService.class, () -> mock(VttgCompendiumRebuildService.class))
                .withBean(UrlTokenRewriter.class)
                .withBean(CatalogUrlRenamer.class)
                .withBean(SourceAcronymRenamer.class)
                .withBean(EntityUrlHistoryRegistry.class)
                .withBean(OldUrlRedirectFilter.class)
                .withBean(CatalogUrlGuard.Installer.class)
                .withBean(InternalUrlHistoryController.class)
                .withBean(VttgUrlAliasRegistry.class)
                .withBean(VttgSourceRenameListener.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(CatalogUrlRenamer.class);
                    assertThat(context).hasSingleBean(SourceAcronymRenamer.class);
                });
    }
}
