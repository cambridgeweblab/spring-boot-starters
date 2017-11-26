package ucles.weblab.common.workflow.webapi.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import org.springframework.hateoas.ResourceSupport;
import ucles.weblab.common.schema.webapi.JsonSchema;
import ucles.weblab.common.schema.webapi.JsonSchemaMetadata;
import ucles.weblab.common.schema.webapi.MoreFormats;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * View model, designed for JSON serialization, of a workflow audit recod.
 *
 * @since 28/07/15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowAuditResource extends ResourceSupport implements Comparable<WorkflowAuditResource> {
    @JsonSchemaMetadata(title = "Username", order = 30)
    private String actor;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSchema(format = JsonValueFormat.DATE_TIME_VALUE)
    @JsonSchemaMetadata(title = "Time", order = 10)
    private Instant auditInstant;
    @JsonSchemaMetadata(title = "Action", order = 20)
    private String action;
    @JsonSchema(format = MoreFormats.DURATION)
    @JsonSchemaMetadata(title = "Time taken", order = 40)
    private Duration duration;
    @JsonSchema(format = MoreFormats.TABLE)
    @JsonSchemaMetadata(title = "Properties", order = 50)
    private Map<String, String> properties;

    protected WorkflowAuditResource() { // For Jackson
    }

    public WorkflowAuditResource(String actor, Instant auditInstant, String action, Duration duration, Map<String, String> properties) {
        this.actor = actor;
        this.auditInstant = auditInstant;
        this.action = action;
        this.duration = duration;
        this.properties = properties;
    }

    public String getActor() {
        return actor;
    }

    public Instant getAuditInstant() {
        return auditInstant;
    }

    public String getAction() {
        return action;
    }

    public Duration getDuration() {
        return duration;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public int compareTo(WorkflowAuditResource that) {
        int comparison = auditInstant.compareTo(that.auditInstant);
        if (comparison == 0) {
            comparison = actor.compareTo(that.actor);
        }
        if (comparison == 0) {
            if (action == null) {
                comparison = that.action == null? 0 : 1;
            } else {
                comparison = that.action == null? -1 : action.compareTo(that.action);
            }
        }

        return comparison;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        WorkflowAuditResource that = (WorkflowAuditResource) o;
        return Objects.equals(actor, that.actor) &&
                Objects.equals(auditInstant, that.auditInstant) &&
                Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), actor, auditInstant, action);
    }

    @Override
    public String toString() {
        return "OrderAuditResource{" +
                "username='" + actor + '\'' +
                ", auditInstant=" + auditInstant +
                ", action='" + action + '\'' +
                ", duration=" + duration +
                '}';
    }
}
