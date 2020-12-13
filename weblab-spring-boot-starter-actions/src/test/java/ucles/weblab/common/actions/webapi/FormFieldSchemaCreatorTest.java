package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import ucles.weblab.common.domain.BuilderProxyFactory;
import ucles.weblab.common.schema.webapi.EnumSchemaCreator;
import ucles.weblab.common.workflow.domain.WorkflowTaskFormField;
import ucles.weblab.common.xc.service.CrossContextConversionService;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @since 17/11/2016
 */
public class FormFieldSchemaCreatorTest {
    @Mock
    CrossContextConversionService crossContextConversionService;
    @Mock
    EnumSchemaCreator enumSchemaCreator;
    JsonSchemaFactory jsonSchemaFactory = new JsonSchemaFactory();
    FormFieldSchemaCreator formFieldSchemaCreator;

    @Before
    public void setUp() {
        formFieldSchemaCreator = new FormFieldSchemaCreator(crossContextConversionService, jsonSchemaFactory, enumSchemaCreator);
    }

    @Test
    public void testOptionalProperty() {
        final WorkflowTaskFormField field = formFieldBuilder()
                .label("Optional property")
                .description("Property description")
                .name("optionalProperty")
                .type(WorkflowTaskFormField.FormFieldType.STRING)
                .required(false)
                .get();
        final JsonSchema schema = formFieldSchemaCreator.createSchema(Stream.of(field), Collections.emptyMap());
        assertTrue("Expect object schema", schema.isObjectSchema());
        assertThat("Expect single property", schema.asObjectSchema().getProperties().keySet(), contains(field.getName()));
        final JsonSchema propertySchema = schema.asObjectSchema().getProperties().get(field.getName());
        assertTrue("Expect string schema", propertySchema.isStringSchema());
        assertFalse("Expect optional", propertySchema.asStringSchema().getRequired());
    }

    @Test
    public void testRequiredProperty() {
        final WorkflowTaskFormField field = formFieldBuilder()
                .label("Required property")
                .description("Property description")
                .name("requiredProperty")
                .type(WorkflowTaskFormField.FormFieldType.STRING)
                .required(true)
                .get();
        final JsonSchema schema = formFieldSchemaCreator.createSchema(Stream.of(field), Collections.emptyMap());
        assertTrue("Expect object schema", schema.isObjectSchema());
        assertThat("Expect single property", schema.asObjectSchema().getProperties().keySet(), contains(field.getName()));
        final JsonSchema propertySchema = schema.asObjectSchema().getProperties().get(field.getName());
        assertTrue("Expect string schema", propertySchema.isStringSchema());
        assertTrue("Expect required", propertySchema.asStringSchema().getRequired());
    }

    private WorkflowTaskFormField.Builder formFieldBuilder() {
        return new BuilderProxyFactory().builder(WorkflowTaskFormField.Builder.class, WorkflowTaskFormField.class);
    }
}
