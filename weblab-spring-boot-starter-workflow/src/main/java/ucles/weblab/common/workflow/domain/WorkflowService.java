package ucles.weblab.common.workflow.domain;

import java.security.Principal;
import java.util.Map;

/**
 * DDD service interface, for methods which do not naturally form part of a repository.
 *
 * @since 17/07/15
 */
public interface WorkflowService {
    /**
     * Creates a model from a deployed process. The model can then be edited and eventually deployed back as an update
     * to the process. The process key is preserved throughout ensuring the deployment is then a new version of the same
     * process rather than a wholly new process.
     * <p>
     *     The created model will be already persisted.
     *
     * @param processDefinition the process definition to generate a model for
     * @return the generated model
     */
    EditableWorkflowProcessEntity convertProcessDefinitionToModel(DeployedWorkflowProcessEntity processDefinition);

    /**
     * Deploys a model as a process. If a process with the same key already exists then a new version of it will be created.
     * <p>
     * The created process will be already persisted.
     * @param model the model to deploy a process for
     * @return the deployed process
     */
    DeployedWorkflowProcessEntity deployModelAsProcessDefinition(EditableWorkflowProcessEntity model);

    /**
     * Fire an event into the workflow system. This may trigger the start of a new process instance or affect a running
     * instance.
     * <p>
     * First of all we attempt to dispatch the event to any process instance with the business key specified.
     * If that fails, if the message instead causes a new instance to be created, the new instance will be given the
     * business key specified.
     *
     * @param eventName the event name which should match a name on a BPMN 2.0.
     * @param businessKey business key of the process instance
     * @param parameters
     * @return true if the message was delivered to a (new or existing) process instance
     */
    boolean handleEvent(String eventName, String businessKey, Map<String,String> parameters);

    /**
     * Completes a task with user data that was entered as properties in a form. The task is also claimed for a user.
     * @param task the task to complete
     * @param data the form data to post
     * @param principal the user for whom to claim the task prior to completion
     */
    void submitTaskFormData(WorkflowTaskEntity task, Map<String, String> data, Principal principal);
}
