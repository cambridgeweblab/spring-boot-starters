package ucles.weblab.common.config.xc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ucles.weblab.common.webapi.HateoasConverterRegistrar;
import ucles.weblab.common.xc.domain.CrossContextLink;
import ucles.weblab.common.xc.webapi.converter.CrossContextLinkToUrlConverter;
import ucles.weblab.common.xc.webapi.converter.UrlToCrossContextLinkConverter;

/**
 * Configuration of converters required for cross-context links outside of the persistence domain.
 * The converters transform between URLs (as {@link java.net.URI}) and {@link CrossContextLink}.
 * This configuration registers the converters with Spring HATEOAS if it's on the classpath.
 * @since 06/10/15
 */
@Configuration
@ConditionalOnClass(CrossContextLink.class)
public class CrossContextLinkConverters {
    @Bean
    public CrossContextLinkToUrlConverter crossContextLinkToUrlConverter() {
        return CrossContextLinkToUrlConverter.INSTANCE;
    }

    @Bean
    public UrlToCrossContextLinkConverter urlToCrossContextLinkConverter() {
        return UrlToCrossContextLinkConverter.INSTANCE;
    }

    @Configuration
    @ConditionalOnClass(name = "org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor")
    static class CrossContextLinkConverterRegistration {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Bean
        CommandLineRunner registerCrossContextLinkConvertersWithSpringHateoas() {
            return args -> {
                HateoasConverterRegistrar.registerConverters(CrossContextLinkToUrlConverter.INSTANCE, UrlToCrossContextLinkConverter.INSTANCE);
                logger.info("Registered CrossContextLink converters with spring-hateoas.");
            };
        }
    }
}
