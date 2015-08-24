package ucles.weblab.common.workflow.domain;

import java.util.Optional;

/**
 * DDD repository interface - persistence-technology-neutral interface providing repository (i.e. CRUD) methods for
 * manipulating editable workflow processes.
 * <p>
 * Although this is technology neutral, it uses Spring Data naming conventions for methods. This allows the
 * interface to be extended with a Spring Data Repository interface for which an implementation is proxied in
 * at runtime.
 * </p>
 *
 * @since 17/07/15
 */
public interface EditableWorkflowProcessRepository {
    Optional<? extends EditableWorkflowProcessEntity> findOneByKey(String key);

    Optional<? extends EditableWorkflowProcessEntity> findOneById(String id);

    void deleteById(String id);
}
