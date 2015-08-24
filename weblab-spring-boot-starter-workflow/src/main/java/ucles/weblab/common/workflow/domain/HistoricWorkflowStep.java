package ucles.weblab.common.workflow.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Value object (i.e. unidentified) representation of a historic (completed) workflow step (not just user tasks).
 *
 * @since 28/07/15
 */
public interface HistoricWorkflowStep {
    String getName();

    Optional<String> getActor();

    Instant getCompletedInstant();

    Duration getDuration();
}
