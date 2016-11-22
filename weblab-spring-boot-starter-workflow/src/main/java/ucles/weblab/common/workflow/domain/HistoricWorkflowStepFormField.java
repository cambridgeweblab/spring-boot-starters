package ucles.weblab.common.workflow.domain;

import ucles.weblab.common.domain.Buildable;

import java.io.Serializable;
import java.util.Map;

/**
 * Value object (i.e. unidentified) representation of a historic (completed) workflow step form field i.e. an input made at
 * completion of the task.
 *
 * @since 19/07/15
 */
public interface HistoricWorkflowStepFormField extends Buildable<HistoricWorkflowStepFormField> {
    String getName();
    String getValue();

    interface Builder extends Buildable.Builder<HistoricWorkflowStepFormField> {
        Builder name(String name);
        Builder value(String value);
    }
}
