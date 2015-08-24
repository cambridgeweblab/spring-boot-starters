package ucles.weblab.common.workflow.domain.activiti;

import org.activiti.engine.RepositoryService;
import ucles.weblab.common.workflow.domain.EditableWorkflowProcessEntity;
import ucles.weblab.common.workflow.domain.EditableWorkflowProcessRepository;

import java.util.Optional;

/**
 * Implementation of the repository interface which uses Activiti.
 *
 * @since 17/07/15
 */
public class EditableWorkflowProcessRepositoryActiviti implements EditableWorkflowProcessRepository {
    private final RepositoryService repositoryService;

    public EditableWorkflowProcessRepositoryActiviti(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Override
    public Optional<? extends EditableWorkflowProcessEntity> findOneByKey(String key) {
        return Optional.ofNullable(repositoryService.createModelQuery().modelKey(key).singleResult())
                .map(EditableWorkflowProcessEntityActiviti::new);
    }

    @Override
    public Optional<? extends EditableWorkflowProcessEntity> findOneById(String id) {
        return Optional.ofNullable(repositoryService.getModel(id)).map(EditableWorkflowProcessEntityActiviti::new);
    }

    @Override
    public void deleteById(String id) {
        repositoryService.deleteModel(id);
    }
}
