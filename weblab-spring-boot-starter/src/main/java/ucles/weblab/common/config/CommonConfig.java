package ucles.weblab.common.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import ucles.weblab.common.domain.BuilderProxyFactory;
import ucles.weblab.common.domain.ConfigurableEntitySupport;
import ucles.weblab.common.domain.jpa.MoreJsr310JpaConverters;
import ucles.weblab.common.webapi.ControllerExceptionHandler;
import ucles.weblab.common.webapi.converter.MoreGenericConverters;
import ucles.weblab.common.webapi.converter.MoreJsr310Converters;
import ucles.weblab.common.webapi.multipart.jersey.JerseyMultipartResolver;

import javax.persistence.EntityManager;

/**
 * Auto-configuration for the common classes.
 *
 * @since 25/06/15
 */
@Configuration
@AutoConfigureBefore(DispatcherServletAutoConfiguration.class)
@ComponentScan(basePackageClasses = ControllerExceptionHandler.class)
@Import({ConfigurableEntitySupport.class, MoreGenericConverters.class, MoreJsr310Converters.class})
public class CommonConfig {

    @Bean
    BuilderProxyFactory builderProxyFactory() {
        return new BuilderProxyFactory();
    }

    /**
     * Use JerseyMultipartResolver instead of the default StandardServletMultipartResolver or CommonsMultipartResolver
     * as it can handle base64 Content-Transfer-Encoding.
     */
    @Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
    @ConditionalOnMissingBean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
    MultipartResolver multipartResolver() {
        return new JerseyMultipartResolver();
    }

    @Configuration
    @ConditionalOnClass(EntityManager.class)
    @EntityScan(basePackageClasses = {MoreJsr310JpaConverters.class})
    public static class CommonJpaConvertersConfig {}

}