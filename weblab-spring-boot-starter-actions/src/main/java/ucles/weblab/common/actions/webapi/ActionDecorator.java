package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.NullSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethodSelector;
import ucles.weblab.common.security.SecurityChecker;
import ucles.weblab.common.schema.webapi.EnumSchemaCreator;
import ucles.weblab.common.schema.webapi.MoreFormats;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.schema.webapi.TypedReferenceSchema;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessEntity;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessRepository;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;
import ucles.weblab.common.workflow.domain.WorkflowTaskFormField;
import ucles.weblab.common.workflow.domain.WorkflowTaskRepository;
import ucles.weblab.common.xc.service.CrossContextConversionService;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class ActionDecorator {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final SecurityChecker securityChecker;
    private final DeployedWorkflowProcessRepository deployedWorkflowProcessRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final CrossContextConversionService crossContextConversionService;
    private final ResourceSchemaCreator resourceSchemaCreator;
    private final EnumSchemaCreator enumSchemaCreator;
    private final JsonSchemaFactory schemaFactory;

    public ActionDecorator(SecurityChecker securityChecker, DeployedWorkflowProcessRepository deployedWorkflowProcessRepository, WorkflowTaskRepository workflowTaskRepository, CrossContextConversionService crossContextConversionService, ResourceSchemaCreator resourceSchemaCreator, EnumSchemaCreator enumSchemaCreator, final JsonSchemaFactory schemaFactory) {
        this.securityChecker = securityChecker;
        this.deployedWorkflowProcessRepository = deployedWorkflowProcessRepository;
        this.workflowTaskRepository = workflowTaskRepository;
        this.crossContextConversionService = crossContextConversionService;
        this.resourceSchemaCreator = resourceSchemaCreator;
        this.enumSchemaCreator = enumSchemaCreator;
        this.schemaFactory = schemaFactory;
    }

    void processResource(ActionableResourceSupport resource) {
        if (resource == null) {
            return;
        }
        
        final ActionCommand[] actionCommands = findAnnotations(resource);
        if (actionCommands.length == 0) {
            return;
        }

        // TODO: cache workflows which can be started for any given action command, since these will be the same across all resource instances.
        List<ActionableResourceSupport.Action> actions = new ArrayList<>();
                
        for (ActionCommand actionCommand : actionCommands) {
            if (actionCommand.authorization().isEmpty() || securityChecker.check(actionCommand.authorization())) {
                final URI businessKey;
                if (actionCommand.createNewKey()) {
                    businessKey = URI.create(UUID.randomUUID().toString());
                } else {
                    businessKey = crossContextConversionService.asUrn(URI.create(resource.getId().getHref()));
                }
                
                if (actionCommand.condition().isEmpty() || checkCondition(actionCommand, resource)) {
                    log.info("Processing action command '" + actionCommand.name() + "' on resource " + resource.toString());
                     
                    if (!actionCommand.message().isEmpty()) {
                        processWorkflowAction(resource, actionCommand, businessKey).ifPresent(actions::add);

                        final List<? extends WorkflowTaskEntity> tasks = workflowTaskRepository.findAllByProcessInstanceBusinessKey(businessKey.toString());
                        // Also check for any existing workflow tasks for this resource
                        // TODO: Demoware - this does not restrict by audience, so everyone sees all tasks
                        Map<String, String> parameters = evaluateWorkflowVariables(resource, actionCommand.workFlowVariables());
                        
                        tasks.stream()
                             .map(t -> processWorkflowTaskAction(t, businessKey.toString(), parameters))
                             .forEach(actions::add);

                    } else if (Void.class != actionCommand.controller()) {
                        // TODO: Also check authorization on the controller method itself.
                        processControllerAction(resource, actionCommand).ifPresent(actions::add);
                    } else {
                        log.error("Action command '" + actionCommand.name() + "' has no workflow message or controller method specified.");
                    }                    
                } else {
                    if (log.isDebugEnabled()) log.debug("Action command '" + actionCommand.name() + "' has unsatisfied condition on resource " + resource.toString());
                }
            } else {
                if (log.isDebugEnabled()) log.debug("Action command '" + actionCommand.name() + "' forbidden on resource " + resource.toString());
            }
        }
        
               
        if (!actions.isEmpty()) {
            if (resource.get$actions() != null) { // Preserve existing actions on a resource
                actions.addAll(resource.get$actions());
            }
            resource.set$actions(actions);
        }
    }

    private Map<String,String> evaluateWorkflowVariables(ActionableResourceSupport resource, ActionParameterNameValue[] workFlowVariables) {
        //evaluate the values using spring                                                 
        Map<String, String> parameters = new HashMap<>();
        for (ActionParameterNameValue v : workFlowVariables) {
            Expression expression = new SpelExpressionParser().parseExpression(v.value(), new TemplateParserContext());
            StandardEvaluationContext evalContext = new StandardEvaluationContext(resource);
            Object value = expression.getValue(evalContext);                            
            parameters.put(v.name(), value.toString());
        }
        
        return parameters;
    }
    
    private Optional<ActionableResourceSupport.Action> processControllerAction(ActionableResourceSupport resource, ActionCommand actionCommand) {
        // Find the controller method and its parameters
        final Method method = findControllerMethod(actionCommand);        
        if (method == null) {
            return Optional.empty();
        }        
        final Parameter[] parameters = method.getParameters();
        final Object[] arguments = evaluateControllerMethodArguments(resource, actionCommand, method, parameters);
        Class<ResourceSupport> resourceType = null;
        if (arguments == null) {
            return Optional.empty();
        } else {
            for (Parameter parameter : parameters) {
                if (ResourceSupport.class.isAssignableFrom(parameter.getType())) {
                    resourceType = (Class<ResourceSupport>) parameter.getType();
                    break;
                }
            }
        }

        final RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        // This linkTo() method doesn't handle trailing slashes on URLs very well
        URI href = linkTo(actionCommand.controller(), method, arguments).toUriComponentsBuilder().replaceQuery(null).build(true).toUri();
        final ActionableResourceSupport.Action action = new ActionableResourceSupport.Action(
                href,
                actionCommand.title(),
                actionCommand.description(),
                resourceType != null? resourceSchemaCreator.create(resourceType, href.resolve("$schema"), Optional.empty(), Optional.empty()) : new NullSchema()
        );
        action.setEnctype(requestMapping.consumes().length > 0 ? requestMapping.consumes()[0] : MediaType.APPLICATION_JSON_VALUE);
        action.setMethod((requestMapping.method().length > 0 ? requestMapping.method()[0] : HttpMethod.POST).toString());
        action.setTargetSchema(new NullSchema());
        action.setRel(actionCommand.name());
        return Optional.of(action);
    }

    private Method findControllerMethod(ActionCommand actionCommand) {
        final Set<Method> methods = HandlerMethodSelector.selectMethods(actionCommand.controller(),
                method -> actionCommand.method().equals(method.getName()));
        if (methods.size() > 1) {
            log.error("More than one controller method matches ");
            return null;
        }
        return methods.iterator().next();
    }

    private Object[] evaluateControllerMethodArguments(final ActionableResourceSupport resource, ActionCommand actionCommand, Method method, Parameter[] parameters) {
        final Object[] arguments = new Object[parameters.length];
        final Iterator<Object> pathVariableValues = Arrays.stream(actionCommand.pathVariables())
                .map(p -> evaluateParameter(p, resource)).iterator();

        // TODO: Support controller methods which take form posts with @RequestParam too
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getAnnotation(PathVariable.class) != null) {
                try {
                    arguments[i] = pathVariableValues.next();
                } catch (NoSuchElementException e) {
                    log.error("No path variable specified for parameter " + i + " [" + parameter + "]");
                    return null;
                }
            } else if (parameter.getAnnotation(RequestBody.class) != null && ResourceSupport.class.isAssignableFrom(parameter.getType())) {
                log.debug("Skipping @RequestBody parameter " + i + " ");
            } else if (parameter.getAnnotation(AuthenticationPrincipal.class) != null || Principal.class.isAssignableFrom(parameter.getType()) || Authentication.class.isAssignableFrom(parameter.getType())) {
                log.debug("Skipping security parameter " + i + " [" + parameter + "]");
            } else if (parameter.getAnnotation(RequestBody.class) == null || !ResourceSupport.class.isAssignableFrom(parameter.getType())) {
                log.error("Controller method " + method.toString() + " parameter " + i + " [" + parameter + "] is not a @PathVariable or a @RequestBody ResourceSupport");
                return null;
            }
        }
        return arguments;
    }

    private Optional<ActionableResourceSupport.Action> processWorkflowAction(ActionableResourceSupport resource, ActionCommand actionCommand, final URI businessKey) {
        
        final List<? extends DeployedWorkflowProcessEntity> targetProcesses = deployedWorkflowProcessRepository.findAllByStartMessage(actionCommand.message());

        if (!targetProcesses.isEmpty()) {
            // TODO: demoware - this check is too broad, we want multiple users to be able to create workflows, and we don't mind multiple workflows of the same type either.
            // This checks for ANY process for this resource with a user task, and refuses to create a new one if there is already one.
            List<? extends WorkflowTaskEntity> existingTasks = workflowTaskRepository.findAllByProcessInstanceBusinessKey(businessKey.toString());
            if (existingTasks.isEmpty()) {
                final Stream<WorkflowTaskFormField> fields = targetProcesses.stream()
                        .flatMap(p -> p.getStartFormFields().stream());
                Map<String, String> parameters = evaluateWorkflowVariables(resource, actionCommand.workFlowVariables());
                                 
                final ActionableResourceSupport.Action action = new ActionableResourceSupport.Action(
                        linkTo(methodOn(ActionAwareWorkflowController.class).handleEvent(businessKey.toString(), actionCommand.message(), parameters, null))
                                .toUriComponentsBuilder().replaceQuery(null).build(true).toUri(), // strip parameters
                        actionCommand.title().isEmpty()? actionCommand.message() : actionCommand.title(),
                        actionCommand.description(),
                        generateSchema(fields, parameters)
                );
                action.setEnctype(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
                action.setMethod(HttpMethod.POST.toString());
                action.setTargetSchema(new NullSchema());
                action.setRel(actionCommand.name());
                return Optional.of(action);
            } else {
                log.debug("Found a workflow action command but there was already a process with the business key");
            }
        } else {
            log.debug("Found a workflow action command but no start target was found");
        }
        return Optional.empty();
    }


    protected ActionableResourceSupport.Action processWorkflowTaskAction(WorkflowTaskEntity task, String businessKey, Map<String,String> parameters) {
        final String formKey = task.getFormKey();
        final Pattern schemaFormKey = Pattern.compile("^schema:([a-z]+):(.*)$");
        JsonSchema schema = null;
        if (formKey != null) {
            Matcher matcher = schemaFormKey.matcher(formKey);
            if (matcher.matches()) {
                String subType = matcher.group(1);
                switch (subType) {
                    case "variable": // Schema is a variable on the process
                        Map<String, Object> variables = task.getContext().getVariables();
                        if (variables != null && variables.containsKey(matcher.group(2))) {
                            final String schemaString = (String) variables.get(matcher.group(2));
                            try {
                                schema = new ObjectMapper().readValue(schemaString, JsonSchema.class);
                            } catch (IOException e) {
                                log.error("Workflow returned a schema which could not be parsed:\n" + schemaString, e);
                            }
                        } else {
                            log.warn("Workflow defined schema form key '" + formKey + "' which did not match a variable in the context: " + variables);
                        }
                        break;
                    case "resource": // Schema is taken directly from a resource
                        Class<?> resourceClass = null;
                        try {
                            resourceClass = Class.forName(matcher.group(2));
                            Assert.isAssignable(ResourceSupport.class, resourceClass);
                            schema = resourceSchemaCreator.create((Class<ResourceSupport>) resourceClass, URI.create("urn:none"),
                                    Optional.empty(), Optional.empty());
                        } catch (ClassNotFoundException | IllegalArgumentException e) {
                            log.warn("Workflow defined schema form key '" + formKey + "' which did not match a resource on the classpath");
                        }
                        break;
                    default:
                        log.warn("Workflow defined schema form key with unknown sub-type: " + formKey);

                }
            } else {
                log.warn("Workflow defined unknown form key: " + formKey);
            }
        }
        if (schema == null) {
            log.debug("No schema was derived from the form key, falling back to workflow-defined form");
            Stream<WorkflowTaskFormField> stream = (Stream<WorkflowTaskFormField>) task.getFormFields().stream();
            schema = generateSchema(stream, parameters);
        }
        ActionableResourceSupport.Action action = new ActionableResourceSupport.Action(linkTo(methodOn(ActionAwareWorkflowController.class).completeTask(businessKey, task.getId(), parameters, null))
                .toUriComponentsBuilder().replaceQuery(null).build(true).toUri(),
                task.getName(), task.getDescription(), schema
        );
        action.setEnctype(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        action.setMethod(HttpMethod.POST.toString());
        action.setTargetSchema(new NullSchema());
        action.setRel(task.getId());
        return action;
    }

    private JsonSchema generateSchema(Stream<WorkflowTaskFormField> fieldMap, Map<String,String> parameters) {
        final ObjectSchema objectSchema = schemaFactory.objectSchema();
        final AtomicInteger index = new AtomicInteger();
        fieldMap.forEach(formField -> {
            final JsonSchema fieldSchema;
            switch (formField.getType()) {
                case STRING: {
                    fieldSchema = schemaFactory.stringSchema();
                } break;
                case BOOLEAN: {
                    fieldSchema = schemaFactory.booleanSchema();
                } break;
                case DATE: {
                    fieldSchema = schemaFactory.stringSchema();
                    fieldSchema.asStringSchema().setFormat(JsonValueFormat.DATE);
                } break;
                case ENUM: {
                    Map<String, String> enumValues = formField.getEnumValues();
                    if (enumValues.isEmpty() && formField.getDefaultValue().toString().startsWith("urn:xc:")) {
                        // External enumRef
                        fieldSchema = schemaFactory.stringSchema();
                        String enumRef = formField.getDefaultValue().toString();
                        fieldSchema.setExtends(new com.fasterxml.jackson.module.jsonSchema.JsonSchema[]{
                                new TypedReferenceSchema(crossContextConversionService.asUrl(URI.create(enumRef)).toString(), fieldSchema.getType())
                        });
                    } else {
                        fieldSchema = enumSchemaCreator.createEnum(enumValues, schemaFactory::stringSchema);
                    }
                    fieldSchema.asValueSchemaSchema().setFormat(JsonValueFormat.valueOf(MoreFormats.LIST));
                } break;
                case LONG: {
                    fieldSchema = schemaFactory.numberSchema();
                } break;
                default:
                    fieldSchema = schemaFactory.anySchema();
            }
            fieldSchema.asSimpleTypeSchema().setDefault(String.valueOf(formField.getDefaultValue()));
            fieldSchema.asSimpleTypeSchema().setTitle(formField.getName());
            fieldSchema.setDescription(formField.getDescription());
            fieldSchema.setId(String.format("order:%03d_%s", index.incrementAndGet(), formField.getName()));
            objectSchema.putProperty(formField.getName(), fieldSchema);
        });
        
        //put all the workflow parameters on the schema  
        //ObjectSchema allParametersSchema = schemaFactory.objectSchema();
        parameters.keySet().stream().forEach((key) -> {
            StringSchema stringSchema = schemaFactory.stringSchema();

            String paramValue = parameters.get(key);


            stringSchema.asSimpleTypeSchema().setDefault(paramValue);
            stringSchema.asSimpleTypeSchema().setTitle(key);
            stringSchema.setDescription("Workflow variable");
            stringSchema.setReadonly(Boolean.TRUE);
            stringSchema.setId(key);
            objectSchema.putProperty(key, stringSchema);
        });
       
        objectSchema.set$schema(ResourceSchemaCreator.HTTP_JSON_SCHEMA_ORG_DRAFT_03_SCHEMA);
        return objectSchema;
    }

    private boolean checkCondition(ActionCommand actionCommand, ActionableResourceSupport resource) {
        final Expression expression = new SpelExpressionParser().parseExpression(actionCommand.condition(), new TemplateParserContext());

        StandardEvaluationContext evalContext = new StandardEvaluationContext(resource);
        return expression.getValue(evalContext, Boolean.class);
    }

    private Object evaluateParameter(ActionParameter parameter, ActionableResourceSupport resource) {
        final Expression expression = new SpelExpressionParser().parseExpression(parameter.value(), new TemplateParserContext());

        StandardEvaluationContext evalContext = new StandardEvaluationContext(resource);
        return expression.getValue(evalContext);
    }

    ActionCommand[] findAnnotations(ActionableResourceSupport resource) {
        ActionCommand[] anns;
        final ActionCommands actionCommands = AnnotationUtils.findAnnotation(resource.getClass(), ActionCommands.class);
        if (actionCommands != null) {
            anns = actionCommands.value();
        } else {
            final ActionCommand ann = AnnotationUtils.findAnnotation(resource.getClass(), ActionCommand.class);
            if (ann != null) {
                anns = new ActionCommand[]{ann};
            } else {
                anns = new ActionCommand[0];
            }
        }
        return anns;
    }
}