package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ucles.weblab.common.actions.config.ActionCommandAutoConfiguration;
import ucles.weblab.common.schema.webapi.EnumSchemaCreator;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.security.SecurityChecker;
import ucles.weblab.common.test.webapi.WebTestSupport;
import ucles.weblab.common.webapi.resource.ActionableResourceSupport;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessRepository;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;
import ucles.weblab.common.workflow.domain.WorkflowTaskRepository;
import ucles.weblab.common.xc.service.CrossContextConversionService;
import ucles.weblab.common.xc.service.CrossContextConversionServiceImpl;

import javax.validation.constraints.Pattern;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test to make sure that injected form key handler ordering in ActionDecorator works as expected.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
public class ActionDecorator_IT {
    @Configuration
    @Import(ActionCommandAutoConfiguration.class)
    public static class Config {
        @Bean
        CrossContextConversionService crossContextConversionService() {
            return new CrossContextConversionServiceImpl();
        }

        @Bean
        @ConditionalOnMissingBean(MethodSecurityExpressionHandler.class)
        MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
            return new DefaultMethodSecurityExpressionHandler();
        }

        @Bean
        SecurityChecker securityChecker(MethodSecurityExpressionHandler methodSecurityExpressionHandler) {
            return new SecurityChecker(methodSecurityExpressionHandler);
        }

        @Bean
        JsonSchemaFactory jsonSchemaFactory() {
            return new JsonSchemaFactory();
        }

        @Bean
        EnumSchemaCreator enumSchemaCreator() {
            return new EnumSchemaCreator();
        }

        @Bean
        ResourceSchemaCreator resourceSchemaCreator(SecurityChecker securityChecker, CrossContextConversionService crossContextConversionService, EnumSchemaCreator enumSchemaCreator, JsonSchemaFactory jsonSchemaFactory) {
            return new ResourceSchemaCreator(securityChecker, new ObjectMapper(), crossContextConversionService, enumSchemaCreator, jsonSchemaFactory);
        }

        @Bean
        DeployedWorkflowProcessRepository deployedWorkflowProcessRepository() {
            return mock(DeployedWorkflowProcessRepository.class);
        }

        @Bean
        WorkflowTaskRepository workflowTaskRepository() {
            return mock(WorkflowTaskRepository.class);
        }
    }

    @Autowired
    ActionDecorator actionDecorator;

    @Before
    public void setUp() throws Exception {
        WebTestSupport.setUpRequestContext();
    }

    @Test
    public void testGetResourceFirst() {
        WorkflowTaskEntity task = mock(WorkflowTaskEntity.class);
        when(task.getFormKey()).thenReturn("schema:resource:" + DummyResource.class.getName());
        when(task.getId()).thenReturn(UUID.randomUUID().toString());

        ActionableResourceSupport.Action action = actionDecorator.processWorkflowTaskAction(task, "blahdiblah", Collections.emptyMap());
        assertNotNull("Expect resource-based action", action.getSchema().asObjectSchema().getProperties().get("galacticSuperhighway"));
    }

    @Test
    public void testFallbackToDefault() {
        WorkflowTaskEntity task = mock(WorkflowTaskEntity.class);
        when(task.getFormKey()).thenReturn("unrecognised");
        when(task.getId()).thenReturn(UUID.randomUUID().toString());

        ActionableResourceSupport.Action action = actionDecorator.processWorkflowTaskAction(task, "blahdiblah", Collections.emptyMap());
        assertEquals("Expect empty schema actions", 0, action.getSchema().asObjectSchema().getProperties().size());
    }

    static class DummyResource extends ResourceSupport {
        @Pattern(regexp = "================")
        String galacticSuperhighway;

        public String getGalacticSuperhighway() {
            return galacticSuperhighway;
        }
    }

}
