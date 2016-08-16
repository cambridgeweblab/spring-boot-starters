package ucles.weblab.common.actions.webapi;

import java.util.Map;

import org.springframework.core.Ordered;
import ucles.weblab.common.webapi.resource.ActionableResourceSupport;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;

/**
 * Interface to handle workflow form keys to produce an action.
 */
public interface FormKeyHandler extends Ordered {
    int MID_PRECEDENCE = 0x3fffffff;

    /**
     * A descriptive name for this key handler, used for logging and identifying different handlers.
     * The default implementation of this method is to use {@link Class#getSimpleName()}
     * @return the name of this key handler
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * By extending {@link Ordered} we can control the order form key handlers are invoked. The default precedence
     * is {@link #MID_PRECEDENCE} which means we can prioritise or deprioritise form key handlers by using numbers
     * either side of that.
     *
     * @return the precendence, defaulting to {@value MID_PRECEDENCE}.
     * @see Ordered#LOWEST_PRECEDENCE
     * @see Ordered#HIGHEST_PRECEDENCE
     */
    @Override
    default int getOrder() {
        return MID_PRECEDENCE;
    }

    /**
     * Determines if this handler is capable of handling the form key given. The action decorator will call this to
     * find out which form key handler to use for a workflow task.
     *
     * @return true if {@link #createAction(WorkflowTaskEntity, String, Map)} should be called for this form key.
     */
    boolean canCreateActions(String formKey);

    /**
     * Implementations can use the {@code WorkflowTaskEntity} to build an Action to hold all information
     * to send to paypal.
     *  @param workflowTaskEntity {@code WorkflowTaskEntity}
     * @param businessKey
     * @param parameters  @return {@code ActionableResourceSupport.Action}
     */
    ActionableResourceSupport.Action createAction(WorkflowTaskEntity workflowTaskEntity, String businessKey, Map<String, String> parameters);
}
