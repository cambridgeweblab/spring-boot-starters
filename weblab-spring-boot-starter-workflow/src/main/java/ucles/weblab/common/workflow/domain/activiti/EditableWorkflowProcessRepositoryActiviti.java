package ucles.weblab.common.workflow.domain.activiti;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.explorer.Messages;
import org.activiti.explorer.util.XmlUtil;
import org.springframework.util.Assert;
import ucles.weblab.common.workflow.domain.EditableWorkflowProcessEntity;
import ucles.weblab.common.workflow.domain.EditableWorkflowProcessRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static java.util.stream.Collectors.toList;

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
    public List<? extends EditableWorkflowProcessEntity> findAll() {
        return repositoryService.createModelQuery().orderByModelKey().asc().list().stream()
                .map(EditableWorkflowProcessEntityActiviti::new)
                .collect(toList());
    }

    @Override
    public void deleteById(String id) {
        repositoryService.deleteModel(id);
    }

    @Override
    public EditableWorkflowProcessEntity save(EditableWorkflowProcessEntity entity) {
        final Model model = ((EditableWorkflowProcessEntityActiviti) entity).getModel();
        Assert.isNull(model.getId(), "Only new models can be saved via the repository");
        repositoryService.saveModel(model);

        final BpmnModel bpmnModel;
        if (entity.getBpmn20Xml() != null) {
            try {
                XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
                XMLStreamReader xtr = xif.createXMLStreamReader(entity.getBpmn20Xml());
                bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
                if (bpmnModel.getMainProcess() == null || bpmnModel.getMainProcess().getId() == null) {
                    throw new IllegalStateException(Messages.MODEL_IMPORT_FAILED);
                } else if (bpmnModel.getLocationMap().isEmpty()) {
                    throw new IllegalStateException(Messages.MODEL_IMPORT_INVALID_BPMNDI);
                }
            } catch (XMLStreamException e) {
                throw new IllegalStateException(Messages.MODEL_IMPORT_FAILED, e);
            }
        } else {
            bpmnModel = templateBpmnModel(entity);
        }

        BpmnJsonConverter converter = new BpmnJsonConverter();
        ObjectNode modelNode = converter.convertToJson(bpmnModel);
        repositoryService.addModelEditorSource(model.getId(), modelNode.toString().getBytes(StandardCharsets.UTF_8));

        return new EditableWorkflowProcessEntityActiviti(model);
    }

    private BpmnModel templateBpmnModel(EditableWorkflowProcessEntity entity) {
        BpmnModel bpmnModel;
        bpmnModel = new BpmnModel();
        final Process process = new Process();
        process.setId(entity.getKey());
        process.setName(entity.getName());
        process.setExecutable(true);
        final StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        startEvent.setName(entity.getName());
        process.addFlowElement(startEvent);
        bpmnModel.addProcess(process);
        final GraphicInfo graphicInfo = new GraphicInfo();
        graphicInfo.setHeight(30);
        graphicInfo.setWidth(30);
        graphicInfo.setX(30);
        graphicInfo.setY(30);
        bpmnModel.addGraphicInfo(startEvent.getId(), graphicInfo);
        return bpmnModel;
    }
}
