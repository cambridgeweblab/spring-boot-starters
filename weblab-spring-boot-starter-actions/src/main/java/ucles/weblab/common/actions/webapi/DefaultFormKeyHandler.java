package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;
import ucles.weblab.common.workflow.domain.WorkflowTaskFormField;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Fallback form key handler which generates an action based on the Activiti workflow form field definitions on the task.
 */
public class DefaultFormKeyHandler extends TaskCompletingFormKeyHandler {
    private final FormFieldSchemaCreator formFieldSchemaCreator;

    public DefaultFormKeyHandler(FormFieldSchemaCreator formFieldSchemaCreator) {
        this.formFieldSchemaCreator = formFieldSchemaCreator;
    }

    /**
     * This form key handler is invoked last, so the order is set to {@link org.springframework.core.Ordered#LOWEST_PRECEDENCE}.
     * @return {@value org.springframework.core.Ordered#LOWEST_PRECEDENCE}
     */
    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    JsonSchema createSchema(WorkflowTaskEntity task, Map<String, String> parameters) {
        log.debug("No schema was derived from the form key, falling back to workflow-defined form");
        Stream<WorkflowTaskFormField> stream = (Stream<WorkflowTaskFormField>) task.getFormFields().stream();
        return formFieldSchemaCreator.createSchema(stream, parameters);
    }

    @Override
    public boolean canCreateActions(String formKey) {
        return true;
    }
}
