package ucles.weblab.common.workflow.webapi.converter;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceAssembler;
import ucles.weblab.common.webapi.LinkRelation;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessEntity;
import ucles.weblab.common.workflow.domain.EditableWorkflowProcessRepository;
import ucles.weblab.common.workflow.webapi.WorkflowController;
import ucles.weblab.common.workflow.webapi.resource.WorkflowProcessDefResource;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static ucles.weblab.common.webapi.LinkRelation.DESCRIBED_BY;

/**
 * Converter from a domain model entity to a view model resource.
 *
 * @since 17/07/15
 */
public class DeployedWorkflowProcessResourceAssembler implements ResourceAssembler<DeployedWorkflowProcessEntity, WorkflowProcessDefResource> {
    private final EditableWorkflowProcessRepository editableWorkflowProcessRepository;

    public DeployedWorkflowProcessResourceAssembler(EditableWorkflowProcessRepository editableWorkflowProcessRepository) {
        this.editableWorkflowProcessRepository = editableWorkflowProcessRepository;
    }

    @Override
    public WorkflowProcessDefResource toResource(DeployedWorkflowProcessEntity entity) {
        WorkflowProcessDefResource resource = new WorkflowProcessDefResource(entity.getKey(), entity.getName(),
                entity.getVersion(), entity.deployedInstant());

        resource.add(editableWorkflowProcessRepository.findOneByKey(entity.getKey())
                .map(model -> linkTo(methodOn(WorkflowController.class).returnModelForProcess(model.getId())))
                .orElse(linkTo(methodOn(WorkflowController.class).createAndReturnModelForProcess(entity.getId())))
                .withRel(DESCRIBED_BY.rel()));
        resource.add(linkTo(methodOn(WorkflowController.class).updateModelForProcess(entity.getId(), resource)).withSelfRel());
        resource.add(new Link(linkTo(methodOn(WorkflowController.class).returnBpmn20ForProcessDefinition(entity.getId()))
                .toUriComponentsBuilder().toUriString() + ".xml", LinkRelation.CANONICAL.rel()));
        resource.add(new Link(linkTo(methodOn(WorkflowController.class).returnBpmn20ForProcessDefinition(entity.getId()))
                .toUriComponentsBuilder().toUriString() + ".png", LinkRelation.ALTERNATE.rel()));
        return resource;
    }
}