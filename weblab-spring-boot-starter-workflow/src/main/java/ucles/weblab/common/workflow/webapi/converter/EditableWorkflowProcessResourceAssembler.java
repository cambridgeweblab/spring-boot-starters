package ucles.weblab.common.workflow.webapi.converter;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ucles.weblab.common.webapi.LinkRelation;
import ucles.weblab.common.webapi.StaticLinkBuilder;
import ucles.weblab.common.webapi.TitledLink;
import ucles.weblab.common.workflow.domain.EditableWorkflowProcessEntity;
import ucles.weblab.common.workflow.webapi.WorkflowController;
import ucles.weblab.common.workflow.webapi.resource.WorkflowModelResource;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * This class is ...
 *
 * @since 17/07/15
 */
public class EditableWorkflowProcessResourceAssembler implements RepresentationModelAssembler<EditableWorkflowProcessEntity, WorkflowModelResource> {
    @Override
    public WorkflowModelResource toModel(EditableWorkflowProcessEntity entity) {
        WorkflowModelResource resource = new WorkflowModelResource(entity.getKey(), entity.getName(),
                entity.getLastUpdatedInstant());

        resource.add(linkTo(methodOn(WorkflowController.class).returnModelForProcess(entity.getId())).withSelfRel());

        final ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
        final StaticLinkBuilder linkBuilder = new StaticLinkBuilder(builder);
        resource.add(linkBuilder.slash("modeler.html?modelId=" + entity.getId()).withRel(LinkRelation.EDIT_FORM.rel()));
        resource.add(new Link(linkTo(methodOn(WorkflowController.class).returnBpmn20XmlForModel(entity.getId()))
                // TODO: validate that toUriComponentsBuilder() is OK and doesn't need replacing with UriComponentsBuilder.fromUriString(...toString()) to avoid double-encoding.
                .toUriComponentsBuilder().toUriString() + ".xml", LinkRelation.CANONICAL.rel()));
        resource.add(new TitledLink(linkTo(methodOn(WorkflowController.class).deleteExistingModel(entity.getId())),
                "delete", null, HttpMethod.DELETE.name()));
        return resource;
    }
}
