package ucles.weblab.common.workflow.webapi.converter;

import org.springframework.hateoas.ResourceAssembler;
import ucles.weblab.common.workflow.domain.HistoricWorkflowStep;
import ucles.weblab.common.workflow.domain.HistoricWorkflowStepAggregate;
import ucles.weblab.common.workflow.domain.HistoricWorkflowStepFormField;
import ucles.weblab.common.workflow.webapi.resource.WorkflowAuditResource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Converter from a domain model value to a view model resource.
 *
 * @since 28/07/15
 */
public class WorkflowAuditResourceAssembler implements ResourceAssembler<HistoricWorkflowStepAggregate, WorkflowAuditResource> {
    @Override
    public WorkflowAuditResource toResource(HistoricWorkflowStepAggregate entity) {
        final List<? extends HistoricWorkflowStepFormField> formFields = entity.getFormFields();
        final Map<String, String> properties = formFields != null? formFields.stream()
                .collect(toMap(HistoricWorkflowStepFormField::getName, HistoricWorkflowStepFormField::getValue)) : null;
        return new WorkflowAuditResource(entity.getActor().orElse(null), entity.getCompletedInstant(),
                entity.getName(), entity.getDuration(), properties);
    }
}
