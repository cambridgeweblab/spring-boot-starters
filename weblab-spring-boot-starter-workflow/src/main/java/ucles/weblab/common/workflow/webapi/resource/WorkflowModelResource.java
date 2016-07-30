package ucles.weblab.common.workflow.webapi.resource;

import org.springframework.hateoas.ResourceSupport;

import java.time.Instant;

/**
 * This class is ...
 *
 * @since 17/07/15
 */
// TODO: add @ActionCommand to deploy the model
public class WorkflowModelResource extends ResourceSupport {
    private String key;
    private String name;
    private Instant lastUpdateInstant;

    protected WorkflowModelResource() { // For Jackson
    }

    public WorkflowModelResource(String key, String name, Instant lastUpdateInstant) {
        this.key = key;
        this.name = name;
        this.lastUpdateInstant = lastUpdateInstant;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public Instant getLastUpdateInstant() {
        return lastUpdateInstant;
    }
}
