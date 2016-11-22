package ucles.weblab.common.workflow.domain.activiti;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.springframework.util.StringUtils;
import ucles.weblab.common.workflow.domain.HistoricWorkflowStepRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Implementation of the repository interface which uses Activiti.
 *
 * @since 28/07/15
 */
public class HistoricWorkflowStepRepositoryActiviti implements HistoricWorkflowStepRepository {
    private final HistoryService historyService;

    public HistoricWorkflowStepRepositoryActiviti(HistoryService historyService) {
        this.historyService = historyService;
    }

    @Override
    public List<HistoricWorkflowStepEntityActiviti> findAllByProcessInstanceBusinessKey(String businessKey) {
        final HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
        if (historicProcessInstance == null) {
            return Collections.emptyList();
        }

        // Collect the submitted form data which will be used to enhance the task activities below
        final Map<String, List<HistoricFormProperty>> propertiesByActivity = historyService.createHistoricDetailQuery()
                .processInstanceId(historicProcessInstance.getId())
                .formProperties().list().stream()
                .map(historicDetail -> (HistoricFormProperty) historicDetail)
                .collect(groupingBy(HistoricFormProperty::getActivityInstanceId));

        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(historicProcessInstance.getId())
                .finished()
                .orderByHistoricActivityInstanceEndTime().asc()
                .orderByHistoricActivityInstanceStartTime().asc().list().stream()
                .filter(i -> !StringUtils.isEmpty(i.getActivityName()))
                .map(activityInstance -> new HistoricWorkflowStepEntityActiviti(activityInstance,
                        propertiesByActivity.get(activityInstance.getId())))
                .collect(Collectors.toList());
    }
}
