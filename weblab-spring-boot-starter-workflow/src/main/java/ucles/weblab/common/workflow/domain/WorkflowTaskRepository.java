package ucles.weblab.common.workflow.domain;

import java.util.List;
import java.util.Optional;

/**
 * DDD repository interface - persistence-technology-neutral interface providing repository (i.e. CRUD) methods for
 * manipulating workflow user tasks. This is a read only repository.
 * <p>
 * Although this is technology neutral, it uses Spring Data naming conventions for methods. This allows the
 * interface to be extended with a Spring Data Repository interface for which an implementation is proxied in
 * at runtime.
 * </p>
 *
 * @since 19/07/15
 */
public interface WorkflowTaskRepository {
    List<? extends WorkflowTaskEntity> findAllByProcessInstanceBusinessKey(String businessKey);

    Optional<? extends WorkflowTaskEntity> findOneByProcessInstanceBusinessKeyAndId(String businessKey, String taskId);
}
