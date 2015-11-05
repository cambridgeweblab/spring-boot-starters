package ucles.weblab.common.workflow.domain.activiti;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import ucles.weblab.common.workflow.domain.EditableWorkflowProcessEntity;
import ucles.weblab.common.workflow.domain.WorkflowFactory;
import ucles.weblab.common.workflow.domain.WorkflowProcess;

import javax.xml.transform.Source;

import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_NAME;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_REVISION;

/**
 * Implementation of the factory using Activiti.
 *
 * @since 04/11/15
 */
public class WorkflowFactoryActiviti implements WorkflowFactory {
    private final RepositoryService repositoryService;

    public WorkflowFactoryActiviti(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Override
    public EditableWorkflowProcessEntity newEditableWorkflowProcess(WorkflowProcess process) {
        Model model = repositoryService.newModel();
        ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
        modelObjectNode.put(MODEL_NAME, process.getName());
        modelObjectNode.put(MODEL_REVISION, 1);
        model.setKey(process.getKey());
        model.setName(process.getName());
        model.setMetaInfo(modelObjectNode.toString());
        return new EditableWorkflowProcessEntityActiviti(model) {
            @Override
            public Source getBpmn20Xml() {
                return process.getBpmn20Xml();
            }
        };
    }
}
