package ucles.weblab.common.workflow.webapi.converter;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceAssembler;
import ucles.weblab.common.webapi.LinkRelation;
import ucles.weblab.common.workflow.domain.EditableWorkflowProcessEntity;
import ucles.weblab.common.workflow.webapi.WorkflowController;
import ucles.weblab.common.workflow.webapi.resource.WorkflowModelResource;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * This class is ...
 *
 * @since 17/07/15
 */
public class EditableWorkflowProcessResourceAssembler implements ResourceAssembler<EditableWorkflowProcessEntity, WorkflowModelResource> {
    @Override
    public WorkflowModelResource toResource(EditableWorkflowProcessEntity entity) {
        WorkflowModelResource resource = new WorkflowModelResource(entity.getKey(), entity.getName(),
                entity.getLastUpdatedInstant());

        resource.add(new Link("/modeler.html?modelId=" + entity.getId(), LinkRelation.EDIT_FORM.rel()));
        resource.add(new Link(linkTo(methodOn(WorkflowController.class).returnBpmn20XmlForModel(entity.getId()))
                .toUriComponentsBuilder().toUriString() + ".xml", LinkRelation.CANONICAL.rel()));
        return resource;
    }
}
