package ucles.weblab.common.workflow.domain;

import java.time.Instant;
import java.util.List;

/**
 * Persistence-technology-neutral interface representing a persistable deployed process definition.
 *
 * @since 17/07/15
 */
public interface DeployedWorkflowProcessEntity extends WorkflowProcess {
    String getId();

    /** description of this process **/
    String getDescription();

    /** version of this process definition */
    int getVersion();

    /** Diagram view of the process, in PNG format. */
    byte[] getPngDiagram();

    /** Definition of the form fields required to start this process. */
    List<? extends WorkflowTaskFormField> getStartFormFields();

    /** When the workflow process was deployed. */
    Instant deployedInstant();
}
