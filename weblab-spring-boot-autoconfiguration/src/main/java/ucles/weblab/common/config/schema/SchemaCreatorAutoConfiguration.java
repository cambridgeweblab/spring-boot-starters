package ucles.weblab.common.config.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import ucles.weblab.common.i18n.service.LocalisationService;
import ucles.weblab.common.schema.webapi.ControllerMethodSchemaCreator;
import ucles.weblab.common.schema.webapi.EnumSchemaCreator;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.security.SecurityChecker;
import ucles.weblab.common.xc.service.CrossContextConversionService;

/**
 * Configure the {@link ResourceSchemaCreator} ready to be injected into controllers.
 *
 * @since 07/10/15
 */
@Configuration
@ConditionalOnBean({ ObjectMapper.class, CrossContextConversionService.class })
@ConditionalOnClass({ SecurityContextHolder.class, JsonSchemaFactory.class, SecurityChecker.class, MethodSecurityExpressionHandler.class, ResourceSchemaCreator.class })
@AutoConfigureAfter(SecurityAutoConfiguration.class)
public class SchemaCreatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MethodSecurityExpressionHandler.class)
    MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        return new DefaultMethodSecurityExpressionHandler();
    }

    @Bean
    SecurityChecker securityChecker(MethodSecurityExpressionHandler handler) {
        return new SecurityChecker(handler);
    }

    @Bean
    ResourceSchemaCreator schemaCreator(SecurityChecker securityChecker,
                                        ObjectMapper objectMapper,
                                        CrossContextConversionService crossContextConversionService,
                                        EnumSchemaCreator enumSchemaCreator,
                                        JsonSchemaFactory schemaFactory,
                                        LocalisationService localisationService) {
        return new ResourceSchemaCreator(securityChecker, objectMapper, crossContextConversionService,
                enumSchemaCreator, schemaFactory, localisationService);
    }

    @Bean
    EnumSchemaCreator enumSchemaCreator(final JsonSchemaFactory schemaFactory) {
        return new EnumSchemaCreator();
    }

    @Bean
    ControllerMethodSchemaCreator controllerMethodSchemaCreator(ObjectMapper objectMapper,
                                                                CrossContextConversionService crossContextConversionService,
                                                                EnumSchemaCreator enumSchemaCreator,
                                                                LocalisationService localisationService) {
        return new ControllerMethodSchemaCreator(objectMapper, crossContextConversionService, enumSchemaCreator, localisationService);
    }

    @Bean
    JsonSchemaFactory jsonSchemaFactory() {
        return new JsonSchemaFactory();
    }
}
