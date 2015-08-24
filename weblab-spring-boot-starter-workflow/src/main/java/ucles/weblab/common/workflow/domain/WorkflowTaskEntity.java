package ucles.weblab.common.workflow.domain;

/**
 * Persistence-technology-neutral interface representing a retrieved workflow user task.
 *
 * @since 20/07/15
 */
public interface WorkflowTaskEntity extends WorkflowTaskAggregate {
    String getId();
}
