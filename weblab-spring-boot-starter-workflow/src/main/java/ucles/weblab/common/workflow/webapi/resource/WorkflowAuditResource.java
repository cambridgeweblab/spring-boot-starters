package ucles.weblab.common.workflow.webapi.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.ResourceSupport;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * View model, designed for JSON serialization, of a workflow audit recod.
 *
 * @since 28/07/15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowAuditResource extends ResourceSupport implements Comparable<WorkflowAuditResource> {
    private String actor;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant auditInstant;
    private String action;
    private Duration duration;

    protected WorkflowAuditResource() { // For Jackson
    }

    public WorkflowAuditResource(String actor, Instant auditInstant, String action, Duration duration) {
        this.actor = actor;
        this.auditInstant = auditInstant;
        this.action = action;
        this.duration = duration;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
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
