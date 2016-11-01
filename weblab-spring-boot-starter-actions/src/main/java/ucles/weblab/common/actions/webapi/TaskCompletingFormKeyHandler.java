package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.NullSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import ucles.weblab.common.webapi.resource.ActionableResourceSupport;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;

import java.util.Map;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Form key handler which generates an action to complete the workflow task.
 * Subclasses must implement the method to generate an appropriate schema for the task variables.
 */
public abstract class TaskCompletingFormKeyHandler implements FormKeyHandler {
    protected Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public ActionableResourceSupport.Action createAction(WorkflowTaskEntity task, String businessKey, Map<String, String> parameters) {
        ActionableResourceSupport.Action action = new ActionableResourceSupport.Action(
                linkTo(methodOn(ActionAwareWorkflowController.class).completeTask(businessKey, task.getId(), parameters, null))
                        .toUriComponentsBuilder()
                        .replaceQuery(null)
                        .build(true).toUri(),
                task.getName(), task.getDescription(), createSchema(task, parameters)
        );
        action.setEnctype(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        action.setMethod(HttpMethod.POST.toString());
        action.setTargetSchema(new NullSchema());
        action.setRel(task.getId());
        return action;
    }

    abstract JsonSchema createSchema(WorkflowTaskEntity task, Map<String, String> parameters);
}