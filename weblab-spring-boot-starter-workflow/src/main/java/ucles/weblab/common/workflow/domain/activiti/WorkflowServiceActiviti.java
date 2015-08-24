package ucles.weblab.common.workflow.domain.activiti;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.explorer.Messages;
import org.activiti.explorer.util.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessEntity;
import ucles.weblab.common.workflow.domain.EditableWorkflowProcessEntity;
import ucles.weblab.common.workflow.domain.WorkflowService;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_DESCRIPTION;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_NAME;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_REVISION;

/**
 * Implementation of the service layer using Activiti.
 *
 * @since 17/07/15
 */
public class WorkflowServiceActiviti implements WorkflowService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final FormService formService;
    private final TaskService taskService;

    public WorkflowServiceActiviti(RepositoryService repositoryService, RuntimeService runtimeService, FormService formService, TaskService taskService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
        this.formService = formService;
        this.taskService = taskService;
    }

    // based on org.activiti.editor.ui.ConvertProcessDefinitionPopupWindow
    @Override
    public EditableWorkflowProcessEntity convertProcessDefinitionToModel(DeployedWorkflowProcessEntity deployedWorkflowProcessEntity) {
        try {
            ProcessDefinition processDefinition = ((DeployedWorkflowProcessEntityActiviti) deployedWorkflowProcessEntity).getProcessDefinition();

            InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), processDefinition.getResourceName());
            XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
            InputStreamReader in = new InputStreamReader(bpmnStream, StandardCharsets.UTF_8);
            XMLStreamReader xtr = xif.createXMLStreamReader(in);
            BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

            if (bpmnModel.getMainProcess() == null || bpmnModel.getMainProcess().getId() == null) {
                throw new IllegalStateException(Messages.MODEL_IMPORT_FAILED);
            } else {
                if (bpmnModel.getLocationMap().isEmpty()) {
                    throw new IllegalStateException(Messages.MODEL_IMPORT_INVALID_BPMNDI);
                } else {

                    BpmnJsonConverter converter = new BpmnJsonConverter();
                    ObjectNode modelNode = converter.convertToJson(bpmnModel);
                    Model modelData = repositoryService.newModel();

                    ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
                    modelObjectNode.put(MODEL_NAME, deployedWorkflowProcessEntity.getName());
                    modelObjectNode.put(MODEL_REVISION, 1);
                    modelObjectNode.put(MODEL_DESCRIPTION, deployedWorkflowProcessEntity.getDescription());
                    modelData.setMetaInfo(modelObjectNode.toString());
                    modelData.setName(deployedWorkflowProcessEntity.getName());
                    modelData.setKey(deployedWorkflowProcessEntity.getKey());

                    repositoryService.saveModel(modelData);

                    repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes(StandardCharsets.UTF_8));

                    return new EditableWorkflowProcessEntityActiviti(modelData);
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DeployedWorkflowProcessEntity deployModelAsProcessDefinition(EditableWorkflowProcessEntity modelData) {
        String bpmnString = ((EditableWorkflowProcessEntityActiviti) modelData).getBpmn20XmlString();

        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, bpmnString)
                .deploy();

        final ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
        return new DeployedWorkflowProcessEntityActiviti(processDefinition);
    }

    @Override
    public boolean handleEvent(String eventName, String businessKey) {
        boolean messageAccepted = false;
        final List<Execution> executions = runtimeService.createExecutionQuery().processInstanceBusinessKey(businessKey).list();
        if (!executions.isEmpty()) {
            for (Execution execution : executions) {
                try {
                    runtimeService.messageEventReceivedAsync(eventName, execution.getId());
                    messageAccepted = true;
                } catch (ActivitiException e) {
                    log.debug("Attempted to send message " + eventName + " to execution " + execution.getId() + " to no avail.");
                }
            }
            log.warn("Event " + eventName + " with key " + businessKey + " did nothing - the running process was not interested.");
        } else {
            try {
                runtimeService.startProcessInstanceByMessage(eventName, businessKey);
                messageAccepted = true;
            } catch (ActivitiException e) {
                log.warn("Event " + eventName + " with key " + businessKey + " did nothing - no process starts with that message.");
            }
        }

        return messageAccepted;
    }

    @Override
    public void submitTaskFormData(WorkflowTaskEntity task, Map<String, String> data, Principal principal) {
        taskService.claim(task.getId(), principal.getName());
        formService.submitTaskFormData(task.getId(), data);
    }
}