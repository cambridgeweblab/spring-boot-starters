package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.module.jsonSchema.types.LinkDescriptionObject;
import org.springframework.hateoas.ResourceSupport;
import ucles.weblab.common.schema.webapi.JsonSchemaIgnore;

import java.net.URI;
import java.util.List;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Base class for DTOs to collect actions.
 *
 * @since 03/11/15
 */
public class ActionableResourceSupport extends ResourceSupport {
    @XmlTransient
    private List<Action> $actions;

    public List<Action> get$actions() {
        return $actions;
    }

    public void set$actions(List<Action> actions) {
        this.$actions = actions;
    }

    @JsonIgnoreProperties({"jsonSchema"}) // LinkDescriptionObject incorrectly annotates jsonSchema.
    @JsonSchemaIgnore
    public static class Action extends LinkDescriptionObject {
        private String description;

        public Action(URI href, String title, String description, com.fasterxml.jackson.module.jsonSchema.JsonSchema schema) {
            super();
            setHref(href.toString());
            setTitle(title);
            setSchema(schema);
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "Action{" +
                    ", href='" + getHref() + '\'' +
                    ", title='" + getTitle() + '\'' +
                    ", description='" + description + '\'' +
                    ", schema=" + getSchema() +
                    '}';
        }
    }
}
