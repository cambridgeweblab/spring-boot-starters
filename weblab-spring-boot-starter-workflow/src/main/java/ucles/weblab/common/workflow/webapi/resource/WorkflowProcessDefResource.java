package ucles.weblab.common.workflow.webapi.resource;

import org.springframework.hateoas.ResourceSupport;

import java.time.Instant;

/**
 * View model designed for JSON serialization.
 *
 * @since 17/07/15
 */
public class WorkflowProcessDefResource extends ResourceSupport {
    private String key;
    private String name;
    private int version;
    private Instant deployInstant;

    protected WorkflowProcessDefResource() { // For Jackson
    }

    public WorkflowProcessDefResource(String key, String name, int version, Instant deployInstant) {
        this.key = key;
        this.name = name;
        this.version = version;
        this.deployInstant = deployInstant;
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
