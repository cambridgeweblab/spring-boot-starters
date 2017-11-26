package ucles.weblab.common.workflow.domain.activiti;

import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.FormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StreamUtils;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessEntity;
import ucles.weblab.common.workflow.domain.WorkflowTaskFormField;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import static java.util.stream.Collectors.toList;
import static ucles.weblab.common.domain.ConfigurableEntitySupport.configureBean;

/**
 * Entity class for persisting an deployed workflow process as an Activiti ProcessDefinition.
 *
 * @since 17/07/15
 */
@Configurable
public class DeployedWorkflowProcessEntityActiviti implements DeployedWorkflowProcessEntity {
    private final ProcessDefinition processDefinition;
    private final AtomicReference<Deployment> deploymentReference = new AtomicReference<>();

    private Supplier<WorkflowTaskFormField.Builder> workflowTaskFormFieldBuilder;
    private RepositoryService repositoryService;
    private FormService formService;

    public DeployedWorkflowProcessEntityActiviti(ProcessDefinition processDefinition) {
        configureBean(this);
        this.processDefinition = processDefinition;
    }

    public Object readResolve() {
        configureBean(this);
        return this;
    }

    @Autowired
    void configureFormService(FormService formService) {
        this.formService = formService;
    }

    @Autowired
    void configureRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Autowired
    void configureWorkflowTaskFormFieldBuilder(Supplier<WorkflowTaskFormField.Builder> workflowTaskFormFieldBuilder) {
        this.workflowTaskFormFieldBuilder = workflowTaskFormFieldBuilder;
    }

    @Override
    public String getId() {
        return processDefinition.getId();
    }

    @Override
    public String getDescription() {
        return processDefinition.getDescription();
    }

    @Override
    public int getVersion() {
        return processDefinition.getVersion();
    }

    @Override
    public byte[] getPngDiagram() {
        final InputStream processDiagram = repositoryService.getProcessDiagram(processDefinition.getId());
        try {
            return StreamUtils.copyToByteArray(processDiagram);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return processDefinition.getName();
    }

    @Override
    public String getKey() {
        return processDefinition.getKey();
    }

    @Override
    public Source getBpmn20Xml() {
        return new StreamSource(repositoryService.getProcessModel(processDefinition.getId()));
    }

    @Override
    public List<? extends WorkflowTaskFormField> getStartFormFields() {
        // See also formProperty.getType().getInformation("datePattern") and formProperty.getType().getInformation("values")
        Function<FormType, WorkflowTaskFormField.FormFieldType> typeMapper = t ->
                WorkflowTaskFormField.FormFieldType.valueOf(t.getName().toUpperCase());

        return formService.getStartFormData(processDefinition.getId()).getFormProperties().stream()
                .filter(FormProperty::isWritable)
                .map(f -> workflowTaskFormFieldBuilder.get()
                        .name(f.getId())
                        .label(f.getName())
                        .description("")
                        .required(f.isRequired())
                        .defaultValue(f.getValue())
                        .type(typeMapper.apply(f.getType()))
                        .get())
                .collect(toList());
    }

    @Override
    public Instant deployedInstant() {
        return getDeployment().getDeploymentTime().toInstant();
    }

    private Deployment getDeployment() {
        if (deploymentReference.get() == null) {
            deploymentReference.compareAndSet(null, repositoryService.createDeploymentQuery().deploymentId(processDefinition.getDeploymentId()).singleResult());
        }
        return deploymentReference.get();
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }
}
