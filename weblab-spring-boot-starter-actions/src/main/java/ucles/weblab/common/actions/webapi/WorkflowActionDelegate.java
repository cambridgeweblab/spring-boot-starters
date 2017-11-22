package ucles.weblab.common.actions.webapi;

import ucles.weblab.common.webapi.ActionCommand;
import ucles.weblab.common.webapi.resource.ActionableResourceSupport;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface WorkflowActionDelegate {
    Stream<ActionableResourceSupport.Action> processExistingWorkflowTaskActions(ActionableResourceSupport resource, Optional<ActionCommand> actionCommand, URI businessKey, boolean addHistoryLink);
    Optional<ActionableResourceSupport.Action> processWorkflowAction(ActionableResourceSupport resource, ActionCommand actionCommand, final URI businessKey);

    ActionableResourceSupport.Action processWorkflowTaskAction(WorkflowTaskEntity task, String businessKey, Map<String, String> parameters);
}
