package ucles.weblab.common.actions.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.web.bind.annotation.RestController;
import ucles.weblab.common.actions.webapi.PayPalFormKeyHandler;
import ucles.weblab.common.webapi.ActionCommand;
import ucles.weblab.common.webapi.ActionCommands;
import ucles.weblab.common.security.SecurityChecker;
import ucles.weblab.common.actions.webapi.ActionAwareWorkflowController;
import ucles.weblab.common.actions.webapi.ActionDecorator;
import ucles.weblab.common.actions.webapi.ActionDecoratorAspect;
import ucles.weblab.common.schema.webapi.EnumSchemaCreator;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessRepository;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;
import ucles.weblab.common.workflow.domain.WorkflowTaskRepository;
import ucles.weblab.common.xc.service.CrossContextConversionService;

import java.util.Optional;

/**
 * Configure automatic processing of {@link ActionCommands @ActionCommands} and
 * {@link ActionCommand @ActionCommand} annotations.
 *
 * @since 03/11/15
 */
@Configuration
@ConditionalOnWebApplication
//@ConditionalOnBean(DeployedWorkflowProcessRepository.class) // TODO: make actions available without workflow...
public class ActionCommandAutoConfiguration {


    @ConditionalOnBean(PayPalFormKeyHandler.class)
    @Bean
    ActionDecorator actionDecorator(SecurityChecker securityChecker,
                                    DeployedWorkflowProcessRepository deployedWorkflowProcessRepository,
                                    CrossContextConversionService crossContextConversionService,
                                    ResourceSchemaCreator schemaCreator,
                                    WorkflowTaskRepository workflowTaskRepository,
                                    JsonSchemaFactory schemaFactory,
                                    EnumSchemaCreator enumSchemaCreator,
                                    PayPalFormKeyHandler payPalFormKeyHandler) {

        return new ActionDecorator(securityChecker,
                deployedWorkflowProcessRepository,
                workflowTaskRepository,
                crossContextConversionService,
                schemaCreator,
                enumSchemaCreator,
                schemaFactory,
                Optional.of(payPalFormKeyHandler));
    }

    @ConditionalOnMissingBean(PayPalFormKeyHandler.class)
    @Bean
    ActionDecorator actionDecorator(SecurityChecker securityChecker,
                                    DeployedWorkflowProcessRepository deployedWorkflowProcessRepository,
                                    CrossContextConversionService crossContextConversionService,
                                    ResourceSchemaCreator schemaCreator,
                                    WorkflowTaskRepository workflowTaskRepository,
                                    JsonSchemaFactory schemaFactory,
                                    EnumSchemaCreator enumSchemaCreator) {

        return new ActionDecorator(securityChecker,
                deployedWorkflowProcessRepository,
                workflowTaskRepository,
                crossContextConversionService,
                schemaCreator,
                enumSchemaCreator,
                schemaFactory,
                Optional.empty());
    }

    @Bean
    ActionDecoratorAspect actionDecoratorAspect(ActionDecorator actionDecorator) {
        return new ActionDecoratorAspect(actionDecorator);
    }

    @Configuration
    @AutoConfigureAfter({DispatcherServletAutoConfiguration.class, WebMvcAutoConfiguration.class})
    @ConditionalOnWebApplication
    @ConditionalOnClass({RestController.class, ResourceAssembler.class, ObjectMapper.class, WorkflowTaskEntity.class})
    @ComponentScan(basePackageClasses = {ActionAwareWorkflowController.class })
    public static class ActionCommandAutoConfigurationWeb {
    }
}
