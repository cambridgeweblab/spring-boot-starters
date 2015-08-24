package ucles.weblab.common.workflow.webapi.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import ucles.weblab.common.webapi.HateoasConverterRegistrar;
import ucles.weblab.common.workflow.domain.EditableWorkflowProcessRepository;

import java.util.Map;

/**
 * Configuration of all the various converters required by the Web API.
 *
 * @since 17/07/15
 */
@Configuration
public class WorkflowConverters {
    @Bean
    public EditableWorkflowProcessResourceAssembler modelResourceAssembler() {
        return new EditableWorkflowProcessResourceAssembler();
    }

    @Bean
    public DeployedWorkflowProcessResourceAssembler processDefinitionResourceAssembler(EditableWorkflowProcessRepository editableWorkflowProcessRepository) {
        return new DeployedWorkflowProcessResourceAssembler(editableWorkflowProcessRepository);
    }

    @Bean
    public WorkflowAuditResourceAssembler workflowOrderAuditResourceAssembler() {
        return new WorkflowAuditResourceAssembler();
    }

    public enum AllParametersMapToEmptyStringConverter implements Converter<Map<String, String>, String> {
        INSTANCE;

        @Override
        public String convert(Map<String, String> source) {
            // TODO: ideally this wouldn't be an empty string, but a real query stirng
            return null;
        }
    }

    @Configuration
    @ConditionalOnClass(name = "org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor")
    static class MapConverterRegistration {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Bean
        CommandLineRunner registerWorkflowConvertersWithSpringHateoas() {
            return args -> {
                HateoasConverterRegistrar.registerConverters(AllParametersMapToEmptyStringConverter.INSTANCE);
                logger.info("Registered workflow converters with spring-hateoas.");
            };
        }
    }


}
