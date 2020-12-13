package ucles.weblab.common.workflow.webapi.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ucles.weblab.common.webapi.ActionCommand;
import ucles.weblab.common.webapi.ActionParameter;
import ucles.weblab.common.webapi.resource.ActionableResourceSupport;
import ucles.weblab.common.workflow.webapi.WorkflowController;

import java.time.Instant;

/**
 * View model designed for JSON serialization.
 *
 * @since 17/07/15
 */
@ActionCommand(
        name = "create-model",
        title = "Create as Model",
        description = "Copy the deployed workflow process to an editable workflow model",
        condition = "#{!@processModelRepository.findOneByKey(key).present}",
        controller = WorkflowController.class,
        method = "createAndReturnModelForProcess",
        pathVariables = { @ActionParameter("#{processId}") }
)
@SuppressWarnings("PMD.DataClass")
public class WorkflowProcessDefResource extends ActionableResourceSupport {
    @JsonIgnore
    private String processId;
    private String key;
    private String name;
    private int version;
    private Instant deployInstant;

    protected WorkflowProcessDefResource() { // For Jackson
    }

    public WorkflowProcessDefResource(String id, String key, String name, int version, Instant deployInstant) {
        this.processId = id;
        this.key = key;
        this.name = name;
        this.version = version;
        this.deployInstant = deployInstant;
    }

    public String getProcessId() {
        return processId;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public int getVersion() {
        return version;
    }

    public Instant getDeployInstant() {
        return deployInstant;
    }
}
