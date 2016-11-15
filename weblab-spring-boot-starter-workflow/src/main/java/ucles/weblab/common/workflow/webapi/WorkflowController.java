package ucles.weblab.common.workflow.webapi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.webapi.AccessAudited;
import ucles.weblab.common.webapi.exception.ReferencedEntityNotFoundException;
import ucles.weblab.common.webapi.exception.ResourceNotFoundException;
import ucles.weblab.common.webapi.resource.ResourceListWrapper;
import ucles.weblab.common.workflow.domain.*;
import ucles.weblab.common.workflow.webapi.converter.DeployedWorkflowProcessResourceAssembler;
import ucles.weblab.common.workflow.webapi.converter.EditableWorkflowProcessResourceAssembler;
import ucles.weblab.common.workflow.webapi.converter.WorkflowAuditResourceAssembler;
import ucles.weblab.common.workflow.webapi.resource.WorkflowAuditResource;
import ucles.weblab.common.workflow.webapi.resource.WorkflowModelResource;
import ucles.weblab.common.workflow.webapi.resource.WorkflowProcessDefResource;

import javax.xml.transform.Source;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static ucles.weblab.common.schema.webapi.SchemaMediaTypes.APPLICATION_SCHEMA_JSON_UTF8_VALUE;
import static ucles.weblab.common.webapi.HateoasUtils.locationHeader;
import static ucles.weblab.common.webapi.LinkRelation.DESCRIBED_BY;
import static ucles.weblab.common.webapi.MoreMediaTypes.APPLICATION_JSON_UTF8_VALUE;
/**
 * Web API providing access to configured workflow processes.
 *
 * @since 16/07/15
 */
@RestController
@RequestMapping("/api/workflow")
@PreAuthorize("isAuthenticated()")
public class WorkflowController {
    private final DeployedWorkflowProcessRepository deployedWorkflowProcessRepository;
    private final EditableWorkflowProcessRepository editableWorkflowProcessRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final HistoricWorkflowStepRepository historicWorkflowStepRepository;
    private final WorkflowFactory workflowFactory;
    private final WorkflowService workflowService;
    private final DeployedWorkflowProcessResourceAssembler deployedWorkflowProcessResourceAssembler;
    private final EditableWorkflowProcessResourceAssembler editableWorkflowProcessResourceAssembler;
    private final WorkflowAuditResourceAssembler workflowAuditResourceAssembler;
    private final ResourceSchemaCreator schemaCreator;

    @Autowired
    public WorkflowController(DeployedWorkflowProcessRepository deployedWorkflowProcessRepository, EditableWorkflowProcessRepository editableWorkflowProcessRepository, WorkflowTaskRepository workflowTaskRepository, HistoricWorkflowStepRepository historicWorkflowStepRepository, WorkflowFactory workflowFactory, WorkflowService workflowService, DeployedWorkflowProcessResourceAssembler deployedWorkflowProcessResourceAssembler, EditableWorkflowProcessResourceAssembler editableWorkflowProcessResourceAssembler, WorkflowAuditResourceAssembler workflowAuditResourceAssembler, ResourceSchemaCreator schemaCreator) {
        this.deployedWorkflowProcessRepository = deployedWorkflowProcessRepository;
        this.editableWorkflowProcessRepository = editableWorkflowProcessRepository;
        this.workflowTaskRepository = workflowTaskRepository;
        this.historicWorkflowStepRepository = historicWorkflowStepRepository;
        this.workflowFactory = workflowFactory;
        this.workflowService = workflowService;
        this.deployedWorkflowProcessResourceAssembler = deployedWorkflowProcessResourceAssembler;
        this.editableWorkflowProcessResourceAssembler = editableWorkflowProcessResourceAssembler;
        this.workflowAuditResourceAssembler = workflowAuditResourceAssembler;
        this.schemaCreator = schemaCreator;
    }

    @RequestMapping(value = "/processes/", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResourceListWrapper<WorkflowProcessDefResource> listWorkflowProcessDefinitions() {
        final List<? extends DeployedWorkflowProcessEntity> entities = deployedWorkflowProcessRepository.findAllByCurrentVersionTrue();
        // The resource assembler checks if there is already a model with the same key as this process and
        // links to either #createAndReturnModelForProcess or #returnModelForProcess depending on that check.
        return ResourceListWrapper.wrap(entities.stream()
                .map(deployedWorkflowProcessResourceAssembler::toResource)
                .collect(toList()));
    }

    @RequestMapping(value = "/processes/{processId}/", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public WorkflowProcessDefResource getSingleWorkflowProcessDefinition(@PathVariable String processId) {
        final DeployedWorkflowProcessEntity entity = deployedWorkflowProcessRepository.findOneById(processId)
                .orElseThrow(() -> new ResourceNotFoundException(processId));
        // The resource assembler checks if there is already a model with the same key as this process and
        // links to either #createAndReturnModelForProcess or #returnModelForProcess depending on that check.
        return deployedWorkflowProcessResourceAssembler.toResource(entity);
    }

    @RequestMapping(value = "/processes/{processId}/", method = GET, produces = APPLICATION_XML_VALUE)
    public ResponseEntity<Source> returnBpmn20ForProcessDefinition(@PathVariable String processId) {
        final DeployedWorkflowProcessEntity entity = deployedWorkflowProcessRepository.findOneById(processId)
                .orElseThrow(() -> new ResourceNotFoundException(processId));
        return ResponseEntity.ok(entity.getBpmn20Xml());
    }

    @RequestMapping(value = "/processes/{processId}/", method = GET, produces = IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> returnDiagramForProcessDefinition(@PathVariable String processId) {
        final DeployedWorkflowProcessEntity entity = deployedWorkflowProcessRepository.findOneById(processId)
                .orElseThrow(() -> new ResourceNotFoundException(processId));
        return ResponseEntity.ok(entity.getPngDiagram());
    }

    @RequestMapping(value = "/processes/{processId}/model/", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ResourceSupport> createAndReturnModelForProcess(@PathVariable String processId) {
        final DeployedWorkflowProcessEntity entity = deployedWorkflowProcessRepository.findOneById(processId)
                .orElseThrow(() -> new ResourceNotFoundException(processId));

        final EditableWorkflowProcessEntity model = workflowService.convertProcessDefinitionToModel(entity);

        ResourceSupport resource = new ResourceSupport();
        resource.add(linkTo(methodOn(WorkflowController.class).returnModelForProcess(model.getId())).withSelfRel());
        return new ResponseEntity<>(resource, locationHeader(resource), HttpStatus.SEE_OTHER);
    }

    @RequestMapping(value = "/models", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResourceListWrapper<WorkflowModelResource> listModels() {
        return ResourceListWrapper.wrap(editableWorkflowProcessRepository.findAll().stream()
                .map(editableWorkflowProcessResourceAssembler::toResource)
                .collect(toList()));
    }

    @RequestMapping(value = "/models/", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<WorkflowModelResource> createNewModel(@RequestBody WorkflowModelResource resource) {
        WorkflowProcess data = new WorkflowProcess() {
            @Override
            public String getName() {
                return resource.getName();
            }

            @Override
            public String getKey() {
                return resource.getKey();
            }

            @Override
            public Source getBpmn20Xml() {
                return null;
            }
        };
        final EditableWorkflowProcessEntity entity = workflowFactory.newEditableWorkflowProcess(data);
        WorkflowModelResource response = editableWorkflowProcessResourceAssembler.toResource(editableWorkflowProcessRepository.save(entity));
        return new ResponseEntity<>(response, locationHeader(response), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/models/{modelId}", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public WorkflowModelResource returnModelForProcess(@PathVariable String modelId) {
        return editableWorkflowProcessResourceAssembler.toResource(editableWorkflowProcessRepository.findOneById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException(modelId)));
    }

    @RequestMapping(value = "/models/{modelId}", method = GET, produces = APPLICATION_XML_VALUE)
    public ResponseEntity<Source> returnBpmn20XmlForModel(@PathVariable String modelId) {
        final EditableWorkflowProcessEntity editableWorkflowProcessEntity = editableWorkflowProcessRepository.findOneById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException(modelId));
        return ResponseEntity.ok(editableWorkflowProcessEntity.getBpmn20Xml());
    }

    @RequestMapping(value = "/models/{modelId}", method = DELETE)
    public ResponseEntity<Void> deleteExistingModel(@PathVariable String modelId) {
        editableWorkflowProcessRepository.deleteById(modelId);
        return ResponseEntity.ok(null);
    }

    @RequestMapping(value = "/processes/{processId}/", method = PUT, consumes = APPLICATION_JSON_UTF8_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    @AccessAudited
    public WorkflowProcessDefResource updateModelForProcess(@PathVariable String processId, @RequestBody WorkflowProcessDefResource processDefinition) {
        final String modelLink = processDefinition.getLink(DESCRIBED_BY.rel()).getHref();
        final String modelId = modelLink.substring(modelLink.lastIndexOf('/') + 1);
        final EditableWorkflowProcessEntity model = editableWorkflowProcessRepository.findOneById(modelId)
                .orElseThrow(() -> new ReferencedEntityNotFoundException("Unable to find referenced model with ID", modelId));
        return deployedWorkflowProcessResourceAssembler.toResource(workflowService.deployModelAsProcessDefinition(model));
    }

    @RequestMapping(value = "/instanceKey/{businessKey}/handlers/{eventName}/", method = POST, consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> handleEvent(@PathVariable String businessKey, @PathVariable String eventName, @RequestParam Map<String, String> allParameters, Principal principal) {
        final boolean delivered = workflowService.handleEvent(eventName, businessKey, allParameters);
        return new ResponseEntity<>(delivered? HttpStatus.ACCEPTED : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/instanceKey/{businessKey}/tasks/{taskId}/", method = POST, consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> completeTask(@PathVariable String businessKey, @PathVariable String taskId, @RequestParam Map<String, String> allParameters, Principal principal) {
        final WorkflowTaskEntity task = workflowTaskRepository.findOneByProcessInstanceBusinessKeyAndId(businessKey, taskId)
                .orElseThrow(() -> new ResourceNotFoundException(taskId));

        workflowService.submitTaskFormData(task, allParameters, principal);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/instanceKey/{businessKey}/history/steps/", method = GET, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResourceListWrapper<WorkflowAuditResource> listWorkflowAudit(@PathVariable String businessKey) {
        final List<? extends HistoricWorkflowStepEntity> entities = historicWorkflowStepRepository.findAllByProcessInstanceBusinessKey(businessKey);
        ResourceListWrapper<WorkflowAuditResource> listWrapper = ResourceListWrapper.wrap(entities.stream().map(workflowAuditResourceAssembler::toResource).collect(toList()));
        listWrapper.add(linkTo(methodOn(WorkflowController.class)).withRel(DESCRIBED_BY.rel()));
        return listWrapper;
    }

    @RequestMapping(value = "/models$schema", method = RequestMethod.GET, produces = APPLICATION_SCHEMA_JSON_UTF8_VALUE)
    public ResponseEntity<JsonSchema> describeWorkflowModels() {
        final JsonSchema body = schemaCreator.create(WorkflowModelResource.class,
                methodOn(WorkflowController.class).describeWorkflowModels(),
                Optional.of(methodOn(WorkflowController.class).listModels()),
                Optional.of(methodOn(WorkflowController.class).createNewModel(null)));
        return ResponseEntity.ok(body);
    }

    @RequestMapping(value = "/processes$schema", method = RequestMethod.GET, produces = APPLICATION_SCHEMA_JSON_UTF8_VALUE)
    public ResponseEntity<JsonSchema> describeWorkflowProcesses() {
        final JsonSchema body = schemaCreator.create(WorkflowProcessDefResource.class,
                methodOn(WorkflowController.class).describeWorkflowProcesses(),
                Optional.of(methodOn(WorkflowController.class).listWorkflowProcessDefinitions()),
                Optional.empty());
        return ResponseEntity.ok(body);
    }

    @RequestMapping(value = "/instanceKey/{businessKey}/history/steps$schema", method = GET, produces = APPLICATION_SCHEMA_JSON_UTF8_VALUE)
    public ResponseEntity<JsonSchema> describeWorkflowAudit(@PathVariable String businessKey) {
        JsonSchema jsonSchema = schemaCreator.create(WorkflowAuditResource.class,
                methodOn(WorkflowController.class).describeWorkflowAudit(businessKey),
                Optional.of(methodOn(WorkflowController.class).listWorkflowAudit(businessKey)),
                Optional.empty());
        return ResponseEntity.ok(jsonSchema);
    }

}
