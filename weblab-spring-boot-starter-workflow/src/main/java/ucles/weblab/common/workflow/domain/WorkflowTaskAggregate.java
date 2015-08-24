package ucles.weblab.common.workflow.domain;

import ucles.weblab.common.domain.Buildable;

import java.util.List;

/**
 * Aggregate value object (i.e. unidentified) representation of a workflow user task with its child objects.
 *
 * @since 20/07/15
 */
public interface WorkflowTaskAggregate extends WorkflowTask, Buildable<WorkflowTaskAggregate> {
    List<? extends WorkflowTaskFormField> getFormFields();

    interface Builder extends Buildable.Builder<WorkflowTaskAggregate> {
        Builder name(String name);

        Builder description(String description);

        Builder formFields(List<? extends WorkflowTaskFormField> formFields);
    }
}
