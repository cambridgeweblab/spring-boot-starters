package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.module.jsonSchema.types.NullSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethodSelector;
import ucles.weblab.common.security.SecurityChecker;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.webapi.ActionCommand;
import ucles.weblab.common.webapi.ActionCommands;
import ucles.weblab.common.webapi.ActionParameter;
import ucles.weblab.common.webapi.ActionParameterNameValue;
import ucles.weblab.common.webapi.LinkRelation;
import ucles.weblab.common.webapi.resource.ActionableResourceSupport;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessEntity;
import ucles.weblab.common.workflow.domain.DeployedWorkflowProcessRepository;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;
import ucles.weblab.common.workflow.domain.WorkflowTaskFormField;
import ucles.weblab.common.workflow.domain.WorkflowTaskRepository;
import ucles.weblab.common.workflow.webapi.WorkflowController;
import ucles.weblab.common.xc.service.CrossContextConversionService;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.stream.Stream;
import ucles.weblab.common.webapi.TitledLink;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class ActionDecorator implements BeanFactoryAware {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final SecurityChecker securityChecker;
    private final DeployedWorkflowProcessRepository deployedWorkflowProcessRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final CrossContextConversionService crossContextConversionService;
    private final ResourceSchemaCreator resourceSchemaCreator;
    private final FormFieldSchemaCreator formFieldSchemaCreator;

    private BeanFactory beanFactory;
    private Collection<FormKeyHandler> formKeyHandlers;

    public ActionDecorator(SecurityChecker securityChecker,
                           DeployedWorkflowProcessRepository deployedWorkflowProcessRepository,
                           WorkflowTaskRepository workflowTaskRepository,
                           CrossContextConversionService crossContextConversionService,
                           ResourceSchemaCreator resourceSchemaCreator,
                           FormFieldSchemaCreator formFieldSchemaCreator,
                           Optional<List<FormKeyHandler>> formKeyHandlers) {

        this.securityChecker = securityChecker;
        this.deployedWorkflowProcessRepository = deployedWorkflowProcessRepository;
        this.workflowTaskRepository = workflowTaskRepository;
        this.crossContextConversionService = crossContextConversionService;
        this.resourceSchemaCreator = resourceSchemaCreator;
        this.formFieldSchemaCreator = formFieldSchemaCreator;
        if (log.isInfoEnabled()) {
            formKeyHandlers.ifPresent(hs -> {
                for (FormKeyHandler formKeyHandler : hs) {
                    log.info("Registering workflow form key handler: " + formKeyHandler.getName());
                }
            });
        }
        this.formKeyHandlers = formKeyHandlers.orElse(Collections.emptyList());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    void processResource(ActionableResourceSupport resource) {
        if (resource == null) {
            return;
        }

        // TODO: cache workflows which can be started for any given action command, since these will be the same across all resource instances.
        List<ActionableResourceSupport.Action> actions = new ArrayList<>();

        final ActionCommands actionCommands = AnnotationUtils.findAnnotation(resource.getClass(), ActionCommands.class);
        if (actionCommands != null && !actionCommands.businessKey().isEmpty()) {
            final Object businessKey = evaluateExpression(resource, actionCommands.businessKey());
            if (!StringUtils.isEmpty(businessKey)) {
                processExistingWorkflowTaskActions(resource, Optional.empty(), URI.create(businessKey.toString())).forEach(actions::add);
                // Add a history link to the workflow audit trail.
                resource.add(new TitledLink(linkTo(methodOn(WorkflowController.class).listWorkflowAudit(businessKey.toString())),
                        LinkRelation.ARCHIVES.rel(), "History", HttpMethod.GET.name()));
            }
        }

        final ActionCommand[] actionCommandList = findAnnotations(resource);
        for (ActionCommand actionCommand : actionCommandList) {
            if (actionCommand.authorization().isEmpty() || securityChecker.check(actionCommand.authorization())) {
                if (actionCommand.condition().isEmpty() || checkCondition(actionCommand, resource)) {
                    log.info("Processing action command '" + actionCommand.name() + "' on resource " + resource.toString());

                    if (!actionCommand.message().isEmpty()) {
                        final URI businessKey;
                        if (actionCommand.createNewKey()) {
                            businessKey = URI.create(UUID.randomUUID().toString());
                        } else {
                            businessKey = crossContextConversionService.asUrn(URI.create(resource.getId().getHref()));
                        }

                        processWorkflowAction(resource, actionCommand, businessKey).ifPresent(actions::add);
                        processExistingWorkflowTaskActions(resource, Optional.of(actionCommand), businessKey).forEach(actions::add);

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
            actions.stream()
                .forEach((action) -> {
                    //convert this to a spring link to set on the resource
                    TitledLink tl = ActionableResourceSupport.convert(action);
                    resource.add(tl);
            });
        }
    }

    protected Stream<ActionableResourceSupport.Action> processExistingWorkflowTaskActions(ActionableResourceSupport resource, Optional<ActionCommand> actionCommand, URI businessKey) {
        final List<? extends WorkflowTaskEntity> tasks = workflowTaskRepository.findAllByProcessInstanceBusinessKey(businessKey.toString());
        // Also check for any existing workflow tasks for this resource
        // TODO: Demoware - this does not restrict by audience, so everyone sees all tasks
        Map<String, String> parameters = actionCommand.map(cmd -> evaluateWorkflowVariables(resource, cmd.workFlowVariables()))
                .orElse(emptyMap());

        return tasks.stream()
                .map(t -> processWorkflowTaskAction(t, businessKey.toString(), parameters));
    }

    private Map<String,String> evaluateWorkflowVariables(ActionableResourceSupport resource, ActionParameterNameValue[] workFlowVariables) {
        //evaluate the values using spring
        Map<String, String> parameters = new HashMap<>();
        for (ActionParameterNameValue v : workFlowVariables) {
            Object value = evaluateExpression(resource, v.value());
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
        URI href = linkTo(actionCommand.controller(), method, arguments).toUriComponentsBuilder().replaceQuery(null).build(true).toUri();
        // This linkTo() method doesn't handle trailing slashes on URLs very well, so fix it.
        if (requestMapping.path()[0].endsWith("/")) {
            href = URI.create(href.toString() + "/");
        }
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
            } else if (parameter.getAnnotation(AuthenticationPrincipal.class) != null || parameter.getAnnotation(org.springframework.security.web.bind.annotation.AuthenticationPrincipal.class) != null || Principal.class.isAssignableFrom(parameter.getType()) || Authentication.class.isAssignableFrom(parameter.getType())) {
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
                        formFieldSchemaCreator.createSchema(fields, parameters)
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

        FormKeyHandler formKeyHandler = formKeyHandlers.stream()
                .filter(handler -> handler.canCreateActions(formKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(formKey + " form key is found but no suitable FormKeyHandler available in "
                        + formKeyHandlers.stream().map(FormKeyHandler::getName).collect(toList())));

        return formKeyHandler.createAction(task, businessKey, parameters);
    }

    private boolean checkCondition(ActionCommand actionCommand, ActionableResourceSupport resource) {
        final Expression expression = new SpelExpressionParser().parseExpression(actionCommand.condition(), new TemplateParserContext());

        StandardEvaluationContext evalContext = new StandardEvaluationContext(resource);
        evalContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        return expression.getValue(evalContext, Boolean.class);
    }

    private Object evaluateParameter(ActionParameter parameter, ActionableResourceSupport resource) {
        final String value = parameter.value();
        return evaluateExpression(resource, value);
    }

    private Object evaluateExpression(ActionableResourceSupport resource, String expr) {
        final Expression expression = new SpelExpressionParser().parseExpression(expr, new TemplateParserContext());

        StandardEvaluationContext evalContext = new StandardEvaluationContext(resource);
        evalContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
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