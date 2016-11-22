package ucles.weblab.common.workflow.domain;

import java.util.List;

/**
 * Aggregate value object (i.e. unidentified) representation of a historic (completed) workflow step with its child objects.
 *
 * @since 22/11/2016
 */
public interface HistoricWorkflowStepAggregate extends HistoricWorkflowStep {
    List<? extends HistoricWorkflowStepFormField> getFormFields();
}
