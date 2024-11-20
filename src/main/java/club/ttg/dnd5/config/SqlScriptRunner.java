package club.ttg.dnd5.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SqlScriptRunner {

//    @Bean
//    public DataSourceInitializer dataSourceInitializer(@Qualifier("dataSource") final DataSource dataSource) {
//        try {
//            ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
//            resourceDatabasePopulator.addScript(new ClassPathResource("/scripts/setup.sql"));
//            DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
//            dataSourceInitializer.setDataSource(dataSource);
//            dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
//            return dataSourceInitializer;
//        } catch (Exception sqlException) {
//            sqlException.printStackTrace();
//            return null;
//        }
//    }
}