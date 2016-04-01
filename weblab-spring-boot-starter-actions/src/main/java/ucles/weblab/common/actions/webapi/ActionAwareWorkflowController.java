package ucles.weblab.common.actions.webapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ucles.weblab.common.webapi.exception.ResourceNotFoundException;
import ucles.weblab.common.workflow.domain.WorkflowService;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;
import ucles.weblab.common.workflow.domain.WorkflowTaskRepository;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Substitute for {@link ucles.weblab.common.workflow.webapi.WorkflowController} where actions are required.
 * TODO: Merge this back into WorkflowController (invert the dependency)
 *
 * @since 09/11/15
 */
@RestController
@RequestMapping("/api/action/workflow")
public class ActionAwareWorkflowController {
    private final WorkflowService workflowService;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final ActionDecorator actionDecorator;

    @Autowired
    public ActionAwareWorkflowController(WorkflowService workflowService, WorkflowTaskRepository workflowTaskRepository, ActionDecorator actionDecorator) {
        this.workflowService = workflowService;
        this.workflowTaskRepository = workflowTaskRepository;
        this.actionDecorator = actionDecorator;
    }

    @RequestMapping(value = "/instanceKey/{businessKey}/handlers/{eventName}/", method = POST, consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ActionableResourceSupport> handleEvent(@PathVariable String businessKey, @PathVariable String eventName, @RequestParam Map<String, String> allParameters, Principal principal) {
        final boolean delivered = workflowService.handleEvent(eventName, businessKey, allParameters);
        if (delivered) {
            return nextTask(businessKey, allParameters);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<ActionableResourceSupport> nextTask(@PathVariable String businessKey, Map<String,String> parameters) {
        ActionableResourceSupport resource = new ActionableResourceSupport();
        // Check if we are now on a user task with a form.
        final List<? extends WorkflowTaskEntity> tasks = workflowTaskRepository.findAllByProcessInstanceBusinessKey(businessKey);
        resource.set$actions(tasks.stream()
                .map(t -> actionDecorator.processWorkflowTaskAction(t, businessKey, parameters))
                .collect(toList()));
        return resource.get$actions().isEmpty()? new ResponseEntity<>(resource, HttpStatus.ACCEPTED) : ResponseEntity.ok(resource);
    }

    @RequestMapping(value = "/instanceKey/{businessKey}/tasks/{taskId}/", method = POST, consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ActionableResourceSupport> completeTask(@PathVariable String businessKey, @PathVariable String taskId, @RequestParam Map<String, String> allParameters, Principal principal) {
        final WorkflowTaskEntity task = workflowTaskRepository.findOneByProcessInstanceBusinessKeyAndId(businessKey, taskId)
                .orElseThrow(() -> new ResourceNotFoundException(taskId));
        
        workflowService.submitTaskFormData(task, allParameters, principal);
        return nextTask(businessKey, allParameters);
    }


}