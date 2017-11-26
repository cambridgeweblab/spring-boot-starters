package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ucles.weblab.common.webapi.exception.ResourceNotFoundException;
import ucles.weblab.common.webapi.resource.ActionableResourceSupport;
import ucles.weblab.common.workflow.domain.WorkflowService;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;
import ucles.weblab.common.workflow.domain.WorkflowTaskRepository;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import ucles.weblab.common.webapi.TitledLink;

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
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final WorkflowService workflowService;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowActionDelegate workflowActionDelegate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ActionAwareWorkflowController(WorkflowService workflowService, WorkflowTaskRepository workflowTaskRepository, WorkflowActionDelegate workflowActionDelegate) {
        this.workflowService = workflowService;
        this.workflowTaskRepository = workflowTaskRepository;
        this.workflowActionDelegate = workflowActionDelegate;
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
        
        //get the actions and set them in the resource
        tasks.stream()
             .map(t -> workflowActionDelegate.processWorkflowTaskAction(t, businessKey, parameters))
             .forEach((action) -> {
                    if (action != null) {
                        //convert this to a spring link to set on the resource
                        TitledLink tl = action.toTitledLink();
                        resource.add(tl);
                    }
              });

        try {
            log.debug("resource = " +  objectMapper.writeValueAsString(resource));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
                                     
        return resource.getLinks().isEmpty() ? new ResponseEntity<>(resource, HttpStatus.ACCEPTED) : ResponseEntity.ok(resource);
    }

    @RequestMapping(value = "/instanceKey/{businessKey}/tasks/{taskId}/", method = POST, consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ActionableResourceSupport> completeTask(@PathVariable String businessKey, @PathVariable String taskId, @RequestParam Map<String, String> allParameters, Principal principal) {
        final WorkflowTaskEntity task = workflowTaskRepository.findOneByProcessInstanceBusinessKeyAndId(businessKey, taskId)
                .orElseThrow(() -> new ResourceNotFoundException(taskId));
        
        workflowService.submitTaskFormData(task, allParameters, principal);
        return nextTask(businessKey, allParameters);
    }


}
