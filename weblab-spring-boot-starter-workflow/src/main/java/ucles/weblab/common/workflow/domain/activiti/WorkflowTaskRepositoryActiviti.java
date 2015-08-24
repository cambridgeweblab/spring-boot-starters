package ucles.weblab.common.workflow.domain.activiti;

import org.activiti.engine.TaskService;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;
import ucles.weblab.common.workflow.domain.WorkflowTaskRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the repository interface which uses Activiti.
 *
 * @since 20/07/15
 */
public class WorkflowTaskRepositoryActiviti implements WorkflowTaskRepository {
    private final TaskService taskService;

    public WorkflowTaskRepositoryActiviti(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public List<WorkflowTaskEntityActiviti> findAllByProcessInstanceBusinessKey(String businessKey) {
        return taskService.createTaskQuery().processInstanceBusinessKey(businessKey).active().list().stream()
                .map(WorkflowTaskEntityActiviti::new)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<? extends WorkflowTaskEntity> findOneByProcessInstanceBusinessKeyAndId(String businessKey, String taskId) {
        return Optional.ofNullable(taskService.createTaskQuery().active().taskId(taskId).processInstanceBusinessKey(businessKey).singleResult())
                .map(WorkflowTaskEntityActiviti::new);
    }
}
