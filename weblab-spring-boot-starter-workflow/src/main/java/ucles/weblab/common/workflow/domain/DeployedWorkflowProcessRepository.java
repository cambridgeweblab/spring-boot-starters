package ucles.weblab.common.workflow.domain;

import java.util.List;
import java.util.Optional;

/**
 * DDD repository interface - persistence-technology-neutral interface providing repository (i.e. CRUD) methods for
 * manipulating deployed workflow processes.
 * <p>
 * Although this is technology neutral, it uses Spring Data naming conventions for methods. This allows the
 * interface to be extended with a Spring Data Repository interface for which an implementation is proxied in
 * at runtime.
 * </p>
 *
 * @since 17/07/15
 */
public interface DeployedWorkflowProcessRepository {
    List<? extends DeployedWorkflowProcessEntity> findAllByCurrentVersionTrue();

    List<? extends DeployedWorkflowProcessEntity> findAllByStartMessage(String startMessage);

    Optional<? extends DeployedWorkflowProcessEntity> findOneById(String id);
}
