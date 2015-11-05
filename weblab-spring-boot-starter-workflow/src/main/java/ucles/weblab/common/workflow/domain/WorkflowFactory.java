package ucles.weblab.common.workflow.domain;

/**
 * DDD factory interface, to create new entity objects.
 *
 * @since 04/11/15
 */
public interface WorkflowFactory {
    /**
     * Create a new workflow process, ready for editing and populate it with data from the value object.
     *
     * @param process value object containing all required data for the new workflow process
     * @return the newly-created entity, ready to persist
     */
    EditableWorkflowProcessEntity newEditableWorkflowProcess(WorkflowProcess process);
}
