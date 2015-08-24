package ucles.weblab.common.workflow.domain;

import javax.xml.transform.Source;

/**
 * Value object (i.e. unidentified) representation of a workflow process definition.
 *
 * @since 17/07/15
 */
public interface WorkflowProcess {
    /** label used for display purposes */
    String getName();

    /** unique name for all versions this process definitions */
    String getKey();

    /** BPMN 2.0 XML definition of the process. */
    Source getBpmn20Xml();
}
