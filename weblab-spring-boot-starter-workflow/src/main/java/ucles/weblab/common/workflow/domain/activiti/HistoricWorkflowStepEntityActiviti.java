package ucles.weblab.common.workflow.domain.activiti;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricFormProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import ucles.weblab.common.workflow.domain.HistoricWorkflowStepEntity;
import ucles.weblab.common.workflow.domain.HistoricWorkflowStepFormField;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static ucles.weblab.common.domain.ConfigurableEntitySupport.configureBean;

/**
 * Entity class for retrieving a historic workflow step as an Activiti HistoricActivityInstance.
 *
 * @since 28/07/15
 */
@Configurable
public class HistoricWorkflowStepEntityActiviti implements HistoricWorkflowStepEntity {
    private final HistoricActivityInstance historicActivityInstance;
    private final List<HistoricFormProperty> historicFormProperties;
    private Supplier<HistoricWorkflowStepFormField.Builder> historicWorkflowStepFormFieldBuilder;

    {
        configureBean(this);
    }

    public Object readResolve() {
        configureBean(this);
        return this;
    }

    public HistoricWorkflowStepEntityActiviti(HistoricActivityInstance historicActivityInstance, List<HistoricFormProperty> historicFormProperties) {
        this.historicActivityInstance = historicActivityInstance;
        this.historicFormProperties = historicFormProperties;
    }

    @Autowired
    void configureWorkflowBuilders(Supplier<HistoricWorkflowStepFormField.Builder> historicWorkflowStepFormFieldBuilder) {
        this.historicWorkflowStepFormFieldBuilder = historicWorkflowStepFormFieldBuilder;
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

    @Override
    public List<? extends HistoricWorkflowStepFormField> getFormFields() {
        return historicFormProperties != null? historicFormProperties.stream()
                .map(f -> historicWorkflowStepFormFieldBuilder.get()
                        .name(f.getPropertyId())
                        .value(f.getPropertyValue())
                        .get()
                ).collect(toList()) : null;
    }
}
