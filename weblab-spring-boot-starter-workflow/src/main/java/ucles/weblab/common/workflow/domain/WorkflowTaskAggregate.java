package ucles.weblab.common.workflow.domain;

import ucles.weblab.common.domain.Buildable;

import java.util.List;

/**
 * Aggregate value object (i.e. unidentified) representation of a workflow user task with its child objects.
 *
 * @since 20/07/15
 */
public interface WorkflowTaskAggregate extends WorkflowTask, Buildable<WorkflowTaskAggregate> {
    String getFormKey();

    /** unique name for all versions of this task definition within its owning process */
    String getTaskDefinitionKey();

    /** unique name for all versions of this process definitions */
    String getProcessKey();

    List<? extends WorkflowTaskFormField> getFormFields();

    WorkflowTaskContext getContext();

    interface Builder extends Buildable.Builder<WorkflowTaskAggregate> {
        Builder name(String name);

        Builder description(String description);

        Builder formKey(String formKey);

        Builder formFields(List<? extends WorkflowTaskFormField> formFields);

        Builder context(WorkflowTaskContext context);
    }
}
