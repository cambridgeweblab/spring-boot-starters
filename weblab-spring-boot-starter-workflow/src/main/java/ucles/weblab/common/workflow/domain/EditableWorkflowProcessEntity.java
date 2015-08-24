package ucles.weblab.common.workflow.domain;

import java.time.Instant;

/**
 * Persistence-technology-neutral interface representing an editable workflow process model.
 * This model is not used for execution.
 *
 * @since 17/07/15
 */
public interface EditableWorkflowProcessEntity extends WorkflowProcess {
    String getId();
    Instant getLastUpdatedInstant();
}
