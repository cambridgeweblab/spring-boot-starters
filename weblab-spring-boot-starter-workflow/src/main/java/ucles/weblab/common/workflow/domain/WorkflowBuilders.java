package ucles.weblab.common.workflow.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ucles.weblab.common.domain.BuilderProxyFactory;

import java.util.function.Supplier;

/**
 * Factory beans for domain object builders.
 *
 * @since 30/07/15
 */
@Configuration
public class WorkflowBuilders {
    protected final BuilderProxyFactory builderProxyFactory = new BuilderProxyFactory();

    @Bean
    public Supplier<WorkflowTaskFormField.Builder> workflowTaskFormFieldBuilder() {
        return () -> builderProxyFactory.builder(WorkflowTaskFormField.Builder.class, WorkflowTaskFormField.class);
    }

    @Bean
    public Supplier<WorkflowTaskAggregate.Builder> workflowTaskAggregateBuilder() {
        return () -> builderProxyFactory.builder(WorkflowTaskAggregate.Builder.class, WorkflowTaskAggregate.class);
    }

    @Bean
    public Supplier<WorkflowTaskContext.Builder> workflowTaskContextBuilder() {
        return () -> builderProxyFactory.builder(WorkflowTaskContext.Builder.class, WorkflowTaskContext.class);
    }

    @Bean
    public Supplier<HistoricWorkflowStepFormField.Builder> historicWorkflowStepFormFieldBuilder() {
        return () -> builderProxyFactory.builder(HistoricWorkflowStepFormField.Builder.class, HistoricWorkflowStepFormField.class);
    }
}
