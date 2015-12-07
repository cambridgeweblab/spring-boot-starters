package ucles.weblab.common.workflow.domain;

import ucles.weblab.common.domain.Buildable;

import java.util.Map;

/**
 * Value object (i.e. unidentified) representation of the context for a workflow user task.
 *
 * @since 07/12/15
 */
public interface WorkflowTaskContext extends Buildable<WorkflowTaskContext> {
    Map<String, Object> getVariables();

    interface Builder extends Buildable.Builder<WorkflowTaskContext> {
        Builder variables(Map<String, Object> variables);
    }
}
