package ucles.weblab.common.actions.webapi;

import java.util.Map;
import ucles.weblab.common.webapi.resource.ActionableResourceSupport;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;

/**
 * Interface to handle paypal forms from the workflow.
 *
 */
public interface PayPalFormKeyHandler {

    /**
     * Implementations can use the {@code WorkflowTaskEntity} to build an Action to hold all information
     * to send to paypal.
     *
     * @param workflowTaskEntity {@code WorkflowTaskEntity}
     * @param parameters
     * @return {@code ActionableResourceSupport.Action}
     */
    ActionableResourceSupport.Action createAction(WorkflowTaskEntity workflowTaskEntity, Map<String,String> parameters);
}
