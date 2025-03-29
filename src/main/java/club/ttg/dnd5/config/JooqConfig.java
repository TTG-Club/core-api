package club.ttg.dnd5.config;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class JooqConfig {
    @Bean
    public DSLContext dslContext(DataSource dataSource) throws SQLException {
        return DSL.using(dataSource.getConnection(), SQLDialect.POSTGRES);

    }
}
