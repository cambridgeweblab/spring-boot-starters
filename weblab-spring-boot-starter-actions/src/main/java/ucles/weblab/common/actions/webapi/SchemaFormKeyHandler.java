package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.util.Assert;
import ucles.weblab.common.i18n.service.LocalisationService;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles form keys beginning with 'schema:xxxx:yyyy' where 'xxxx' is a subtype and 'yyyy' is an identifier.
 * <dl>
 *     <dt>variable</dt>
 *     <dd>the schema is taken from a String workflow variable and used directly</dd>
 *     <dt>resource</dt>
 *     <dd>the schema is generated from a resource class (subclass of {@link org.springframework.hateoas.RepresentationModel}.</dd>
 * </dl>
 * Unknown subtypes will lead to a runtime exception.
 */
public class SchemaFormKeyHandler extends TaskCompletingFormKeyHandler {
    private static final Pattern SCHEMA_FORM_KEY = Pattern.compile("^schema:([a-z]+):(.*)$");
    private final ResourceSchemaCreator resourceSchemaCreator;

    public SchemaFormKeyHandler(LocalisationService localisationService, ResourceSchemaCreator resourceSchemaCreator) {
        super(localisationService);
        this.resourceSchemaCreator = resourceSchemaCreator;
    }

    @Override
    JsonSchema createSchema(WorkflowTaskEntity task, Map<String, String> parameters) {
        final String formKey = task.getFormKey();
        Matcher matcher = SCHEMA_FORM_KEY.matcher(formKey);
        if (matcher.matches()) {
            String subType = matcher.group(1);
            switch (subType) {
                case "variable": // Schema is a variable on the process
                    Map<String, Object> variables = task.getContext().getVariables();
                    if (variables != null && variables.containsKey(matcher.group(2))) {
                        final String schemaString = (String) variables.get(matcher.group(2));
                        try {
                            return new ObjectMapper().readValue(schemaString, JsonSchema.class);
                        } catch (IOException e) {
                            log.error("Workflow returned a schema which could not be parsed:\n" + schemaString, e);
                        }
                    } else {
                        log.error("Workflow defined schema form key '" + formKey + "' which did not match a variable in the context: " + variables);
                    }
                    break;
                case "resource": // Schema is taken directly from a resource
                    Class<?> resourceClass;
                    try {
                        resourceClass = Class.forName(matcher.group(2));
                        Assert.isAssignable(RepresentationModel.class, resourceClass);
                        return resourceSchemaCreator.create((Class<RepresentationModel>) resourceClass, URI.create("urn:none"),
                                Optional.empty(), Optional.empty());
                    } catch (ClassNotFoundException | IllegalArgumentException e) {
                        log.error("Workflow defined schema form key '" + formKey + "' which did not match a resource on the classpath");
                    }
                    break;
                default:
                    log.error("Workflow defined schema form key with unknown sub-type: " + subType);
            }
        }
        throw new IllegalArgumentException("Could not handle form key: " + formKey);
    }

    @Override
    public boolean canCreateActions(String formKey) {
        return formKey != null && SCHEMA_FORM_KEY.matcher(formKey).matches();
    }
}
