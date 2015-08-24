package ucles.weblab.common.workflow.domain.activiti;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StreamUtils;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessEntity;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

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

    private RepositoryService repositoryService;

    {
        configureBean(this);
    }

    public Object readResolve() {
        configureBean(this);
        return this;
    }

    public DeployedWorkflowProcessEntityActiviti(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    @Autowired
    void configureRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
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
