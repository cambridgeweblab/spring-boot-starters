package ucles.weblab.common.actions.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.web.bind.annotation.RestController;
import ucles.weblab.common.actions.webapi.ActionAwareWorkflowController;
import ucles.weblab.common.actions.webapi.ActionDecorator;
import ucles.weblab.common.actions.webapi.ActionDecoratorAspect;
import ucles.weblab.common.actions.webapi.DefaultFormKeyHandler;
import ucles.weblab.common.actions.webapi.ExpressionEvaluator;
import ucles.weblab.common.actions.webapi.FormFieldSchemaCreator;
import ucles.weblab.common.actions.webapi.FormKeyHandler;
import ucles.weblab.common.actions.webapi.SchemaFormKeyHandler;
import ucles.weblab.common.actions.webapi.WorkflowActionDelegate;
import ucles.weblab.common.actions.webapi.WorkflowActionDelegateImpl;
import ucles.weblab.common.i18n.service.LocalisationService;
import ucles.weblab.common.schema.webapi.EnumSchemaCreator;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.security.SecurityChecker;
import ucles.weblab.common.webapi.ActionCommand;
import ucles.weblab.common.webapi.ActionCommands;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessRepository;
import ucles.weblab.common.workflow.domain.WorkflowService;
import ucles.weblab.common.workflow.domain.WorkflowTaskRepository;
import ucles.weblab.common.xc.service.CrossContextConversionService;

import java.util.List;
import java.util.Optional;

/**
 * Configure automatic processing of {@link ActionCommands @ActionCommands} and
 * {@link ActionCommand @ActionCommand} annotations.
 *
 * @since 03/11/15
 */
@Configuration
public class ActionCommandAutoConfiguration {

    @Configuration
    @AutoConfigureAfter(ActionCommandAutoConfigurationWorkflow.class)
    static class ActionCommandAutoConfigurationBase {
        @Bean
        ActionDecorator actionDecorator(SecurityChecker securityChecker,
                                        CrossContextConversionService crossContextConversionService,
                                        ResourceSchemaCreator resourceSchemaCreator,
                                        LocalisationService localisationService,
                                        ExpressionEvaluator expressionEvaluator,
                                        Optional<WorkflowActionDelegate> workflowActionDelegate) {

            return new ActionDecorator(securityChecker, crossContextConversionService, resourceSchemaCreator,
                    localisationService, expressionEvaluator, workflowActionDelegate);
        }

        @Bean
        ActionDecoratorAspect actionDecoratorAspect(ActionDecorator actionDecorator) {
            return new ActionDecoratorAspect(actionDecorator);
        }

        @Bean
        ExpressionEvaluator actionableResourceExpressionEvaluator() {
            return new ExpressionEvaluator();
        }
    }

    @Configuration
    @ConditionalOnClass({DeployedWorkflowProcessRepository.class, WorkflowTaskRepository.class, WorkflowService.class})
    static class ActionCommandAutoConfigurationWorkflow {

        @Bean
        WorkflowActionDelegate workflowActionDelegate(DeployedWorkflowProcessRepository deployedWorkflowProcessRepository,
                                                      WorkflowTaskRepository workflowTaskRepository,
                                                      FormFieldSchemaCreator formFieldSchemaCreator,
                                                      LocalisationService localisationService,
                                                      ExpressionEvaluator expressionEvaluator,
                                                      Optional<List<FormKeyHandler>> formKeyHandlers) {
            return new WorkflowActionDelegateImpl(deployedWorkflowProcessRepository, workflowTaskRepository,
                    formFieldSchemaCreator, localisationService, expressionEvaluator, formKeyHandlers);
        }

        @Bean
        FormKeyHandler defaultFormKeyHandler(LocalisationService localisationService, FormFieldSchemaCreator formFieldSchemaCreator) {
            return new DefaultFormKeyHandler(localisationService, formFieldSchemaCreator);
        }

        @Bean
        FormKeyHandler schemaFormKeyHandler(LocalisationService localisationService, ResourceSchemaCreator resourceSchemaCreator) {
            return new SchemaFormKeyHandler(localisationService, resourceSchemaCreator);
        }

        @Bean
        FormFieldSchemaCreator formFieldSchemaCreator(CrossContextConversionService crossContextConversionService,
                                                      JsonSchemaFactory schemaFactory,
                                                      EnumSchemaCreator enumSchemaCreator) {
            return new FormFieldSchemaCreator(crossContextConversionService, schemaFactory, enumSchemaCreator);
        }

        @ConditionalOnWebApplication
        @ConditionalOnClass({RestController.class, RepresentationModelAssembler.class, ObjectMapper.class})
        @Bean
        ActionAwareWorkflowController actionAwareWorkflowController(WorkflowService workflowService,
                                                                    WorkflowTaskRepository workflowTaskRepository,
                                                                    WorkflowActionDelegate workflowActionDelegate) {
            return new ActionAwareWorkflowController(workflowService, workflowTaskRepository, workflowActionDelegate);
        }
    }

}
