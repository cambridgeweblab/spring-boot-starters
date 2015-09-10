package ucles.weblab.common.config.audit;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ucles.weblab.common.audit.aspect.AccessAuditAspect;
import ucles.weblab.common.audit.domain.AccessAudit;
import ucles.weblab.common.audit.domain.AccessAuditRepository;
import ucles.weblab.common.audit.domain.AuditBuilders;
import ucles.weblab.common.audit.domain.AuditFactory;
import ucles.weblab.common.audit.domain.jpa.AccessAuditEntityJpa;
import ucles.weblab.common.audit.domain.jpa.AccessAuditRepositoryJpa;
import ucles.weblab.common.audit.domain.jpa.AuditFactoryJpa;

import java.util.function.Supplier;
import javax.sql.DataSource;

/**
 * Configure access auditing for this application. Controller methods annotated with
 * {@link ucles.weblab.common.webapi.AccessAudited @AccessAudited} have their access logged to a database.
 *
 * @since 27/07/15
 */
@Configuration
@AutoConfigureAfter({DataSourceAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
@ConditionalOnBean(DataSource.class)
@ConditionalOnClass({JpaRepository.class, AuditFactory.class})
@ConditionalOnProperty(prefix = "spring.data.jpa.repositories", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({AuditBuilders.class})
@EnableJpaRepositories(basePackageClasses = {AccessAuditRepositoryJpa.class})
@EntityScan(basePackageClasses = { AccessAuditEntityJpa.class } )
@EnableAspectJAutoProxy
public class AuditAutoConfiguration {

    @Bean
    public AuditFactory auditFactory() {
        return new AuditFactoryJpa();
    }

    @Bean
    public AccessAuditAspect accessAuditAspect(AccessAuditRepository accessAuditRepository, AuditFactory auditFactory,
                                               Supplier<AccessAudit.Builder> accessAuditBuilder) {
        return new AccessAuditAspect(accessAuditRepository, auditFactory, accessAuditBuilder);
    }
}
