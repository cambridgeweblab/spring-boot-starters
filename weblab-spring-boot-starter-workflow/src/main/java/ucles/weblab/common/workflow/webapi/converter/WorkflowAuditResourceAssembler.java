package ucles.weblab.common.workflow.webapi.converter;

import org.springframework.hateoas.ResourceAssembler;
import ucles.weblab.common.workflow.domain.HistoricWorkflowStep;
import ucles.weblab.common.workflow.webapi.resource.WorkflowAuditResource;

/**
 * Converter from a domain model value to a view model resource.
 *
 * @since 28/07/15
 */
public class WorkflowAuditResourceAssembler implements ResourceAssembler<HistoricWorkflowStep, WorkflowAuditResource> {
    @Override
    public WorkflowAuditResource toResource(HistoricWorkflowStep entity) {
        return new WorkflowAuditResource(entity.getActor().orElse(null), entity.getCompletedInstant(),
                entity.getName(), entity.getDuration());
    }
}
