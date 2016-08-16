package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.hateoas.ResourceSupport;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.test.webapi.WebTestSupport;
import ucles.weblab.common.webapi.HateoasConverterRegistrar;
import ucles.weblab.common.webapi.resource.ActionableResourceSupport;
import ucles.weblab.common.workflow.domain.WorkflowTaskContext;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;
import ucles.weblab.common.workflow.webapi.converter.WorkflowConverters;

import javax.validation.constraints.Pattern;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * @since 23/12/15
 */
@RunWith(MockitoJUnitRunner.class)
public class SchemaFormKeyHandlerTest {
    JsonSchemaFactory schemaProvider = new JsonSchemaFactory();

    @Mock
    ResourceSchemaCreator resourceSchemaCreator;

    @InjectMocks
    SchemaFormKeyHandler schemaFormKeyHandler;

    @BeforeClass
    public static void registerConverter() {
        HateoasConverterRegistrar.registerConverters(WorkflowConverters.AllParametersMapToEmptyStringConverter.INSTANCE);
    }

    @Before
    public void setUp() throws Exception {
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
    public void whenFormKeyStartsWithSchema_thenHandlerIndicatesSupport() {
        assertTrue("Expect support", schemaFormKeyHandler.canCreateActions("schema:kimchi:spicy"));
    }

    @Test
    public void whenFormKeyIsNull_thenHandlerIndicatesNoSupport() {
        assertFalse("Expect no support", schemaFormKeyHandler.canCreateActions(null));
    }

    @Test
    public void whenFormKeyDoesNotStartWithSchema_thenHandlerIndicatesNoSupport() {
        assertFalse("Expect no support", schemaFormKeyHandler.canCreateActions("magaluf:lager:lager:lager:shouting"));
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
        ActionableResourceSupport.Action action = schemaFormKeyHandler.createAction(task, "blahdiblah", Collections.emptyMap());
        assertEquals("Expect schema to be included with action", schema, action.getSchema());
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenClassIsNotAResource_whenWorkflowSpecifiesResourceSchema_thenErrorThrown() {
        Class<?> resource = DummyPojo.class;
        WorkflowTaskEntity task = mock(WorkflowTaskEntity.class);
        when(task.getFormKey()).thenReturn("schema:resource:" + resource.getName());
        when(task.getId()).thenReturn(UUID.randomUUID().toString());
        ObjectSchema schema = schemaProvider.objectSchema();
        schema.setId("urn:" + task.getId());

        ActionableResourceSupport.Action action = schemaFormKeyHandler.createAction(task, "blahdiblah", Collections.emptyMap());
        assertNotEquals("Expect schema to not be included with action", schema, action.getSchema());
        verifyZeroInteractions(resourceSchemaCreator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenClassDoesNotExist_whenWorkflowSpecifiesResourceSchema_thenErrorThrown() {
        WorkflowTaskEntity task = mock(WorkflowTaskEntity.class);
        when(task.getFormKey()).thenReturn("schema:resource:foo.bar.Wibble");
        when(task.getId()).thenReturn(UUID.randomUUID().toString());
        ObjectSchema schema = schemaProvider.objectSchema();
        schema.setId("urn:" + task.getId());

        ActionableResourceSupport.Action action = schemaFormKeyHandler.createAction(task, "blahdiblah", Collections.emptyMap());
        assertNotEquals("Expect schema to not be included with action", schema, action.getSchema());
        verifyZeroInteractions(resourceSchemaCreator);
    }

    @Test
    public void givenVariableIsValidJson_whenWorkflowSpecifiesVariableSchema_thenActionIncludesSchema() throws JsonProcessingException {
        WorkflowTaskEntity task = mock(WorkflowTaskEntity.class);
        WorkflowTaskContext taskContext = mock(WorkflowTaskContext.class);
        when(task.getFormKey()).thenReturn("schema:variable:boxer");
        when(task.getId()).thenReturn(UUID.randomUUID().toString());
        when(task.getContext()).thenReturn(taskContext);
        ObjectSchema schema = schemaProvider.objectSchema();
        schema.setId("urn:" + task.getId());
        String schemaString = new ObjectMapper().writeValueAsString(schema);
        when(taskContext.getVariables()).thenReturn(Collections.singletonMap("boxer", schemaString));

        ActionableResourceSupport.Action action = schemaFormKeyHandler.createAction(task, "blahdiblah", Collections.emptyMap());
        assertEquals("Expect schema to be included with action", schema.getId(), action.getSchema().getId());
    }
}
