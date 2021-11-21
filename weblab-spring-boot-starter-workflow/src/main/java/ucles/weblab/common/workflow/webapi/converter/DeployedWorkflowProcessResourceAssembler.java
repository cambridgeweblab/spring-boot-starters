package ucles.weblab.common.workflow.webapi.converter;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import ucles.weblab.common.webapi.LinkRelation;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessEntity;
import ucles.weblab.common.workflow.webapi.WorkflowController;
import ucles.weblab.common.workflow.webapi.resource.WorkflowProcessDefResource;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


/**
 * Converter from a domain model entity to a view model resource.
 *
 * @since 17/07/15
 */
public class DeployedWorkflowProcessResourceAssembler implements RepresentationModelAssembler<DeployedWorkflowProcessEntity, WorkflowProcessDefResource> {

    @Override
    public WorkflowProcessDefResource toModel(DeployedWorkflowProcessEntity entity) {
        WorkflowProcessDefResource resource = new WorkflowProcessDefResource(entity.getId(), entity.getKey(), entity.getName(),
                entity.getVersion(), entity.deployedInstant());

        resource.add(linkTo(methodOn(WorkflowController.class).updateModelForProcess(entity.getId(), resource)).withSelfRel());
        resource.add(new Link(linkTo(methodOn(WorkflowController.class).returnBpmn20ForProcessDefinition(entity.getId()))
                // TODO: validate that toUriComponentsBuilder() is OK and doesn't need replacing with UriComponentsBuilder.fromUriString(...toString()) to avoid double-encoding.
                .toUriComponentsBuilder().toUriString() + ".xml", LinkRelation.CANONICAL.rel()));
        resource.add(new Link(linkTo(methodOn(WorkflowController.class).returnBpmn20ForProcessDefinition(entity.getId()))
                // TODO: validate that toUriComponentsBuilder() is OK and doesn't need replacing with UriComponentsBuilder.fromUriString(...toString()) to avoid double-encoding.
                .toUriComponentsBuilder().toUriString() + ".png", LinkRelation.ALTERNATE.rel()));
        return resource;
    }
}
