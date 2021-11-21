package ucles.weblab.common.workflow.webapi.converter;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import ucles.weblab.common.workflow.domain.HistoricWorkflowStepAggregate;
import ucles.weblab.common.workflow.domain.HistoricWorkflowStepFormField;
import ucles.weblab.common.workflow.webapi.resource.WorkflowAuditResource;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Converter from a domain model value to a view model resource.
 *
 * @since 28/07/15
 */
public class WorkflowAuditResourceAssembler implements RepresentationModelAssembler<HistoricWorkflowStepAggregate, WorkflowAuditResource> {
    @Override
    public WorkflowAuditResource toModel(HistoricWorkflowStepAggregate entity) {
        final List<? extends HistoricWorkflowStepFormField> formFields = entity.getFormFields();
        final Map<String, String> properties = formFields == null ? null : formFields.stream()
                .collect(toMap(HistoricWorkflowStepFormField::getName, HistoricWorkflowStepFormField::getValue));
        return new WorkflowAuditResource(entity.getActor().orElse(null), entity.getCompletedInstant(),
                entity.getName(), entity.getDuration(), properties);
    }
}
