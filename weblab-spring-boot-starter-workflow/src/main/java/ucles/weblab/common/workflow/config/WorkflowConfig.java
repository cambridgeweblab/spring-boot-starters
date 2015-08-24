package ucles.weblab.common.workflow.config;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.rest.editor.main.StencilsetRestResource;
import org.activiti.rest.editor.model.ModelEditorJsonRestResource;
import org.activiti.spring.boot.DataSourceProcessEngineAutoConfiguration;
import org.activiti.spring.boot.JpaProcessEngineAutoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessRepository;
import ucles.weblab.common.workflow.domain.EditableWorkflowProcessRepository;
import ucles.weblab.common.workflow.domain.HistoricWorkflowStepRepository;
import ucles.weblab.common.workflow.domain.WorkflowBuilders;
import ucles.weblab.common.workflow.domain.WorkflowService;
import ucles.weblab.common.workflow.domain.WorkflowTaskRepository;
import ucles.weblab.common.workflow.domain.activiti.DeployedWorkflowProcessRepositoryActiviti;
import ucles.weblab.common.workflow.domain.activiti.EditableWorkflowProcessRepositoryActiviti;
import ucles.weblab.common.workflow.domain.activiti.HistoricWorkflowStepRepositoryActiviti;
import ucles.weblab.common.workflow.domain.activiti.WorkflowServiceActiviti;
import ucles.weblab.common.workflow.domain.activiti.WorkflowTaskRepositoryActiviti;
import ucles.weblab.common.workflow.webapi.WorkflowController;
import ucles.weblab.common.workflow.webapi.converter.WorkflowConverters;

import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.util.UUID;

/**
 * Auto-configuration for the workflow domain.
 *
 * @since 16/07/15
 */
@Configuration
@AutoConfigureAfter({JacksonAutoConfiguration.class, JpaProcessEngineAutoConfiguration.class, DataSourceProcessEngineAutoConfiguration.class})
@ComponentScan(basePackageClasses = {StencilsetRestResource.class, ModelEditorJsonRestResource.class, WorkflowController.class})
@Import({WorkflowConverters.class, WorkflowBuilders.class})
public class WorkflowConfig {

    @Bean
    public WorkflowService workflowService(RepositoryService repositoryService, RuntimeService runtimeService,
                                           FormService formService, TaskService taskService) {
        return new WorkflowServiceActiviti(repositoryService, runtimeService, formService, taskService);
    }

    @Bean
    public DeployedWorkflowProcessRepository deployedProcessRepository(RepositoryService repositoryService) {
        return new DeployedWorkflowProcessRepositoryActiviti(repositoryService);
    }

    @Bean
    public EditableWorkflowProcessRepository processModelRepository(RepositoryService repositoryService) {
        return new EditableWorkflowProcessRepositoryActiviti(repositoryService);
    }

    @Bean
    public WorkflowTaskRepository workflowTaskRepository(TaskService taskService) {
        return new WorkflowTaskRepositoryActiviti(taskService);
    }

    @Bean
    public HistoricWorkflowStepRepository historicWorkflowStepRepository(HistoryService historyService) {
        return new HistoricWorkflowStepRepositoryActiviti(historyService);
    }

    /**
     * This allows Workflow EL expressions to convert String -> java.util.UUID easily.
     */
    @Bean
    public CommandLineRunner registerUuidPropertyEditor() {
        return (args) -> {
            PropertyEditorManager.registerEditor(UUID.class, UuidPropertyEditor.class);
        };
    }

    public static class UuidPropertyEditor extends PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(UUID.fromString(text));
        }
    }
}
