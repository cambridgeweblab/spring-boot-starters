package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.hateoas.ResourceSupport;
import ucles.weblab.common.security.SecurityChecker;
import ucles.weblab.common.test.webapi.WebTestSupport;
import ucles.weblab.common.webapi.HateoasConverterRegistrar;
import ucles.weblab.common.schema.webapi.EnumSchemaCreator;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessRepository;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;
import ucles.weblab.common.workflow.domain.WorkflowTaskRepository;
import ucles.weblab.common.workflow.webapi.converter.WorkflowConverters;
import ucles.weblab.common.xc.service.CrossContextConversionService;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import javax.validation.constraints.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @since 23/12/15
 */
@RunWith(MockitoJUnitRunner.class)
public class ActionDecoratorTest {
    JsonSchemaFactory schemaProvider = new JsonSchemaFactory();

    @Mock
    SecurityChecker securityChecker;

    @Mock
    DeployedWorkflowProcessRepository deployedWorkflowProcessRepository;

    @Mock
    WorkflowTaskRepository workflowTaskRepository;

    @Mock
    ResourceSchemaCreator resourceSchemaCreator;

    @Mock
    CrossContextConversionService crossContextConversionService;

    @Mock
    EnumSchemaCreator enumSchemaCreator;

    ActionDecorator actionDecorator;

    @BeforeClass
    public static void registerConverter() {
        HateoasConverterRegistrar.registerConverters(WorkflowConverters.AllParametersMapToEmptyStringConverter.INSTANCE);
    }

    @Before
    public void setUp() throws Exception {
        actionDecorator = new ActionDecorator(securityChecker, deployedWorkflowProcessRepository, workflowTaskRepository,
                crossContextConversionService, resourceSchemaCreator, enumSchemaCreator, schemaProvider);
        WebTestSupport.setUpRequestContext();
    }

    static class DummyResource extends ResourceSupport {
        @Pattern(regexp = "================")
        String galacticSuperhighway;

        public String getGalacticSuperhighway() {
            return galacticSuperhighway;
        }
    }

    static class DummyPojo {
        @Pattern(regexp = "================")
        String galacticSuperhighway;

        public String getGalacticSuperhighway() {
            return galacticSuperhighway;
        }
    }

    @Test
    public void givenResourceExists_whenWorkflowSpecifiesResourceSchema_thenActionIncludesSchema() {
        Class<?> resource = DummyResource.class;
        WorkflowTaskEntity task = mock(WorkflowTaskEntity.class);
        when(task.getFormKey()).thenReturn("schema:resource:" + resource.getName());
        when(task.getId()).thenReturn(UUID.randomUUID().toString());
        ObjectSchema schema = schemaProvider.objectSchema();
        schema.setId("urn:" + task.getId());
        when(resourceSchemaCreator.create(same((Class) resource), any(URI.class), any(Optional.class), any(Optional.class)))
                .thenReturn(schema);

        ActionableResourceSupport.Action action = actionDecorator.processWorkflowTaskAction(task, "blahdiblah", Collections.emptyMap());
        assertEquals("Expect schema to be included with action", schema, action.getSchema());
    }

    @Test
    public void givenClassIsNotAResource_whenWorkflowSpecifiesResourceSchema_thenActionDoesntIncludeSchema() {
        Class<?> resource = DummyPojo.class;
        WorkflowTaskEntity task = mock(WorkflowTaskEntity.class);
        when(task.getFormKey()).thenReturn("schema:resource:" + resource.getName());
        when(task.getId()).thenReturn(UUID.randomUUID().toString());
        ObjectSchema schema = schemaProvider.objectSchema();
        schema.setId("urn:" + task.getId());

        ActionableResourceSupport.Action action = actionDecorator.processWorkflowTaskAction(task, "blahdiblah", Collections.emptyMap());
        assertNotEquals("Expect schema to not be included with action", schema, action.getSchema());
        verifyZeroInteractions(resourceSchemaCreator);
    }

    @Test
    public void givenClassDoesNotExist_whenWorkflowSpecifiesResourceSchema_thenActionDoesntIncludeSchema() {
        WorkflowTaskEntity task = mock(WorkflowTaskEntity.class);
        when(task.getFormKey()).thenReturn("schema:resource:foo.bar.Wibble");
        when(task.getId()).thenReturn(UUID.randomUUID().toString());
        ObjectSchema schema = schemaProvider.objectSchema();
        schema.setId("urn:" + task.getId());

        ActionableResourceSupport.Action action = actionDecorator.processWorkflowTaskAction(task, "blahdiblah", Collections.emptyMap());
        assertNotEquals("Expect schema to not be included with action", schema, action.getSchema());
        verifyZeroInteractions(resourceSchemaCreator);
    }
}
