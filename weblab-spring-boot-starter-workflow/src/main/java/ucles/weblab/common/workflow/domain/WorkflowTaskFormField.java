package ucles.weblab.common.workflow.domain;

import ucles.weblab.common.domain.Buildable;

import java.io.Serializable;

/**
 * Value object (i.e. unidentified) representation of a workflow user task form field i.e. an input required for
 * completion of the task.
 *
 * @since 19/07/15
 */
public interface WorkflowTaskFormField extends Buildable<WorkflowTaskFormField> {
    String getName();
    String getLabel();
    String getDescription();
    FormFieldType getType();
    Serializable getDefaultValue();
    boolean isRequired();

    interface Builder extends Buildable.Builder<WorkflowTaskFormField> {
        Builder name(String name);
        Builder label(String label);
        Builder description(String description);
        Builder type(FormFieldType type);
        Builder defaultValue(Serializable defaultValue);
        Builder required(boolean required);
    }

    enum FormFieldType {
        BOOLEAN, LONG, DATE, STRING, ENUM
    }
}
