package ucles.weblab.common.workflow.domain.activiti;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RepositoryService;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessEntity;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessRepository;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Implementation of the repository interface which uses Activiti.
 *
 * @since 17/07/15
 */
public class DeployedWorkflowProcessRepositoryActiviti implements DeployedWorkflowProcessRepository {
    private final RepositoryService repositoryService;

    public DeployedWorkflowProcessRepositoryActiviti(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Override
    public List<? extends DeployedWorkflowProcessEntity> findAllByCurrentVersionTrue() {
        return repositoryService.createProcessDefinitionQuery().latestVersion().list().stream()
                .map(DeployedWorkflowProcessEntityActiviti::new)
                .collect(toList());
    }

    @Override
    public List<? extends DeployedWorkflowProcessEntity> findAllByStartMessage(String startMessage) {
        return repositoryService.createProcessDefinitionQuery().latestVersion().messageEventSubscriptionName(startMessage).list().stream()
                .map(DeployedWorkflowProcessEntityActiviti::new)
                .collect(toList());
    }

    @Override
    public Optional<? extends DeployedWorkflowProcessEntity> findOneById(String id) {
        try {
            return Optional.of(new DeployedWorkflowProcessEntityActiviti(repositoryService.getProcessDefinition(id)));
        } catch (ActivitiObjectNotFoundException e) {
            return Optional.empty();
        }
    }
}
