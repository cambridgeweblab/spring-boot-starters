package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
import ucles.weblab.common.schema.webapi.EnumSchemaCreator;
import ucles.weblab.common.schema.webapi.MoreFormats;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.schema.webapi.TypedReferenceSchema;
import ucles.weblab.common.workflow.domain.WorkflowTaskFormField;
import ucles.weblab.common.xc.service.CrossContextConversionService;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Generates a schema from a workflow form definition.
 */
public class FormFieldSchemaCreator {
    private final CrossContextConversionService crossContextConversionService;
    private final JsonSchemaFactory schemaFactory;
    private final EnumSchemaCreator enumSchemaCreator;

    public FormFieldSchemaCreator(CrossContextConversionService crossContextConversionService, JsonSchemaFactory schemaFactory, EnumSchemaCreator enumSchemaCreator) {
        this.crossContextConversionService = crossContextConversionService;
        this.schemaFactory = schemaFactory;
        this.enumSchemaCreator = enumSchemaCreator;
    }

    JsonSchema createSchema(Stream<WorkflowTaskFormField> fieldMap, Map<String,String> parameters) {
        final ObjectSchema objectSchema = schemaFactory.objectSchema();
        final AtomicInteger index = new AtomicInteger();
        fieldMap.forEach(formField -> {
            final JsonSchema fieldSchema;
            switch (formField.getType()) {
                case STRING: {
                    fieldSchema = schemaFactory.stringSchema();
                } break;
                case BOOLEAN: {
                    fieldSchema = schemaFactory.booleanSchema();
                } break;
                case DATE: {
                    fieldSchema = schemaFactory.stringSchema();
                    fieldSchema.asStringSchema().setFormat(JsonValueFormat.DATE);
                } break;
                case ENUM: {
                    Map<String, String> enumValues = formField.getEnumValues();
                    if (enumValues.isEmpty() && formField.getDefaultValue().toString().startsWith("urn:xc:")) {
                        // External enumRef
                        fieldSchema = schemaFactory.stringSchema();
                        String enumRef = formField.getDefaultValue().toString();
                        fieldSchema.setExtends(new com.fasterxml.jackson.module.jsonSchema.JsonSchema[]{
                                new TypedReferenceSchema(crossContextConversionService.asUrl(URI.create(enumRef)).toString(), fieldSchema.getType())
                        });
                    } else {
                        fieldSchema = enumSchemaCreator.createEnum(enumValues, schemaFactory::stringSchema);
                    }
                    fieldSchema.asValueTypeSchema().setFormat(JsonValueFormat.valueOf(MoreFormats.LIST));
                } break;
                case LONG: {
                    fieldSchema = schemaFactory.numberSchema();
                } break;
                default:
                    fieldSchema = schemaFactory.anySchema();
            }
            fieldSchema.asSimpleTypeSchema().setDefault(String.valueOf(formField.getDefaultValue()));
            fieldSchema.asSimpleTypeSchema().setTitle(formField.getName());
            fieldSchema.setDescription(formField.getDescription());
            fieldSchema.setId(String.format("order:%03d_%s", index.incrementAndGet(), formField.getName()));
            objectSchema.putProperty(formField.getName(), fieldSchema);
        });

        //put all the workflow parameters on the schema
        //ObjectSchema allParametersSchema = schemaFactory.objectSchema();
        parameters.keySet().stream().forEach((key) -> {
            StringSchema stringSchema = schemaFactory.stringSchema();

            String paramValue = parameters.get(key);


            stringSchema.asSimpleTypeSchema().setDefault(paramValue);
            stringSchema.asSimpleTypeSchema().setTitle(key);
            stringSchema.setDescription("Workflow variable");
            stringSchema.setReadonly(Boolean.TRUE);
            stringSchema.setId(key);
            objectSchema.putProperty(key, stringSchema);
        });

        objectSchema.set$schema(ResourceSchemaCreator.HTTP_JSON_SCHEMA_ORG_DRAFT_03_SCHEMA);
        return objectSchema;
    }

}
