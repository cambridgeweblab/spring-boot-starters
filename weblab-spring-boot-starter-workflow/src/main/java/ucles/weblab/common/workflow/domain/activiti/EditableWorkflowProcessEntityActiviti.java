package ucles.weblab.common.workflow.domain.activiti;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import ucles.weblab.common.workflow.domain.EditableWorkflowProcessEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static ucles.weblab.common.domain.ConfigurableEntitySupport.configureBean;

/**
 * Entity class for persisting an editable workflow process as an Activiti Model.
 *
 * @since 17/07/15
 */
@Configurable
public class EditableWorkflowProcessEntityActiviti implements EditableWorkflowProcessEntity {
    private final Model model;
    private RepositoryService repositoryService;
    private ObjectMapper objectMapper;

    {
        configureBean(this);
    }

    public Object readResolve() {
        configureBean(this);
        return this;
    }

    public EditableWorkflowProcessEntityActiviti(Model model) {
        this.model = model;
    }

    @Autowired
    void configureRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Autowired
    void configureObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper=objectMapper;
    }

    @Override
    public String getId() {
        return model.getId();
    }

    @Override
    public String getName() {
        return model.getName();
    }

    @Override
    public String getKey() {
        return model.getKey();
    }

    @Override
    public Source getBpmn20Xml() {
        try {
            final ObjectNode modelNode = (ObjectNode) objectMapper.readTree(repositoryService.getModelEditorSource(getId()));
            final BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            final byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
            return new StreamSource(new ByteArrayInputStream(bpmnBytes));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read model editor source", e);
        }
    }

    public String getBpmn20XmlString() {
        try {
            final ObjectNode modelNode = (ObjectNode) objectMapper.readTree(repositoryService.getModelEditorSource(getId()));
            final BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            final byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model, UTF_8.name());
            return new String(bpmnBytes, UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read model editor source", e);
        }
    }

    @Override
    public Instant getLastUpdatedInstant() {
        return model.getLastUpdateTime().toInstant();
    }

    public Model getModel() {
        return model;
    }
}
