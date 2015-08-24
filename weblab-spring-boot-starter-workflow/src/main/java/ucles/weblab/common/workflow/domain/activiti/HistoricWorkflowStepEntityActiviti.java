package ucles.weblab.common.workflow.domain.activiti;

import org.activiti.engine.history.HistoricActivityInstance;
import ucles.weblab.common.workflow.domain.HistoricWorkflowStepEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Entity class for retrieving a historic workflow step as an Activiti HistoricActivityInstance.
 *
 * @since 28/07/15
 */
public class HistoricWorkflowStepEntityActiviti implements HistoricWorkflowStepEntity {
    private final HistoricActivityInstance historicActivityInstance;
    
    public HistoricWorkflowStepEntityActiviti(HistoricActivityInstance historicActivityInstance) {
        this.historicActivityInstance = historicActivityInstance;
    }

    @Override
    public String getName() {
        return historicActivityInstance.getActivityName();
    }

    @Override
    public Optional<String> getActor() {
        return Optional.ofNullable(historicActivityInstance.getAssignee());
    }

    @Override
    public Instant getCompletedInstant() {
        return Optional.ofNullable(historicActivityInstance.getEndTime()).map(Date::toInstant).orElse(null);
    }

    @Override
    public Duration getDuration() {
        return Optional.ofNullable(historicActivityInstance.getDurationInMillis()).map(Duration::ofMillis).orElse(null);
    }
}
