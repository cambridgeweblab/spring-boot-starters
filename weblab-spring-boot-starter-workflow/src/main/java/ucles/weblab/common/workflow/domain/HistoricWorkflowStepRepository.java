package ucles.weblab.common.workflow.domain;

import java.util.List;

/**
 * DDD repository interface - persistence-technology-neutral interface providing repository (i.e. CRUD) methods for
 * manipulating historic (completed) workflow steps. This is a read only repository.
 * <p>
 * Although this is technology neutral, it uses Spring Data naming conventions for methods. This allows the
 * interface to be extended with a Spring Data Repository interface for which an implementation is proxied in
 * at runtime.
 * </p>
 *
 * @since 28/07/15
 */
public interface HistoricWorkflowStepRepository {
    List<? extends HistoricWorkflowStepEntity> findAllByProcessInstanceBusinessKey(String businessKey);
}
