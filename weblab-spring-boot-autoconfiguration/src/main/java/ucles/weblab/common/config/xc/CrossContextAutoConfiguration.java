package ucles.weblab.common.config.xc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.web.bind.annotation.RestController;
import ucles.weblab.common.xc.domain.jpa.CrossContextLinkJpaConverters;
import ucles.weblab.common.xc.service.ControllerIntrospectingCrossContextConverter;
import ucles.weblab.common.xc.service.CrossContextConversionService;
import ucles.weblab.common.xc.service.CrossContextConversionServiceImpl;
import ucles.weblab.common.xc.service.CrossContextResolverService;
import ucles.weblab.common.xc.service.CrossContextResolverServiceImpl;
import ucles.weblab.common.xc.service.HandlerMethodInvokingCrossContextResolver;

import javax.persistence.EntityManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ucles.weblab.common.xc.service.RestCrossContextConverter;
import ucles.weblab.common.xc.service.RestCrossContextResolver;
import ucles.weblab.common.xc.service.RestSettings;

/**
 * Auto-configuration for the cross-context services.
 *
 * @since 06/10/15
 */
@Configuration
@ConditionalOnClass({ CrossContextConversionService.class })
@Import(CrossContextLinkConverters.class)
@EnableConfigurationProperties(RestSettings.class)
public class CrossContextAutoConfiguration {

    @Bean
    CrossContextConversionService crossContextConversionService() {
        return new CrossContextConversionServiceImpl();
    }

    @Bean
    CrossContextResolverService crossContextResolverService() {
        return new CrossContextResolverServiceImpl();
    }

    /** Sets up conversion of CrossContextLink with a JPA 2.1 converter */
    @Configuration
    @ConditionalOnClass(EntityManager.class)
    @EntityScan(basePackageClasses = {CrossContextLinkJpaConverters.class})
    public static class CrossContextJpaConvertersConfig{
    }

    @Configuration
    @AutoConfigureAfter({DispatcherServletAutoConfiguration.class, WebMvcAutoConfiguration.class})
    @ConditionalOnWebApplication
    @ConditionalOnClass({RestController.class, ResourceAssembler.class, ObjectMapper.class})
    public static class CrossContextAutoConfigurationWeb {
        private final Logger logger = LoggerFactory.getLogger(getClass());
        @Autowired
        CrossContextConversionService crossContextConversionService;
        @Autowired
        CrossContextResolverService crossContextResolverService;

        @Bean
        ControllerIntrospectingCrossContextConverter controllerIntrospectingCrossContextConverter() {
            return new ControllerIntrospectingCrossContextConverter();
        }

        @Bean
        HandlerMethodInvokingCrossContextResolver handlerMethodInvokingCrossContextResolver(
                ControllerIntrospectingCrossContextConverter converter, ObjectMapper objectMapper) {
            return new HandlerMethodInvokingCrossContextResolver(converter, objectMapper);
        }

        @Bean
        RestCrossContextConverter restCrossContextConverter(RestSettings restSettings) {
            return new RestCrossContextConverter(restSettings);
        }

        @Bean
        RestCrossContextResolver restCrossContextResolver(RestCrossContextConverter converter,
                                                          ObjectMapper objectMapper) {
            return new RestCrossContextResolver(converter, objectMapper);
        }

        @Bean
        CommandLineRunner registerControllerIntrospectingCrossContextConvertWithConversionService(RestSettings restSettings) {
            return args -> {
                crossContextConversionService.addConverter(controllerIntrospectingCrossContextConverter());
                logger.info("Enabled controller introspection for @CrossContextMapping.");
                crossContextConversionService.addConverter(restCrossContextConverter(restSettings));
                logger.info("Enabled explicit mappings for @CrossContextMapping.");
            };
        }

        @Bean
        CommandLineRunner registerHandlerMethodInvokingCrossContextResolverWithResolverService(HandlerMethodInvokingCrossContextResolver handlerMethodInvokingCrossContextResolver,
                                                                                                RestCrossContextResolver restCrossContextResolver) {
            return args -> {
                crossContextResolverService.addResolver(handlerMethodInvokingCrossContextResolver);
                logger.info("Enabled direct-invocation cross-context link resolution.");
                crossContextResolverService.addResolver(restCrossContextResolver);
                logger.info("Enabled external RestTemplate cross-context link resolution.");
            };
        }
    }
}
