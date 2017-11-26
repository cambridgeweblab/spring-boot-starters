package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.module.jsonSchema.types.NullSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
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
import ucles.weblab.common.i18n.service.LocalisationService;
import ucles.weblab.common.schema.webapi.ResourceSchemaCreator;
import ucles.weblab.common.security.SecurityChecker;
import ucles.weblab.common.webapi.ActionCommand;
import ucles.weblab.common.webapi.ActionCommands;
import ucles.weblab.common.webapi.TitledLink;
import ucles.weblab.common.webapi.resource.ActionableResourceSupport;
import ucles.weblab.common.xc.service.CrossContextConversionService;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.security.Principal;
import java.util.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class ActionDecorator {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final SecurityChecker securityChecker;
    private final CrossContextConversionService crossContextConversionService;
    private final ResourceSchemaCreator resourceSchemaCreator;
    private final LocalisationService localisationService;
    // This delegate will be present iff workflow classes are available on the classpath.
    private final Optional<WorkflowActionDelegate> workflowActionDelegate;
    private final ExpressionEvaluator expressionEvaluator;

    public ActionDecorator(SecurityChecker securityChecker,
                           CrossContextConversionService crossContextConversionService,
                           ResourceSchemaCreator resourceSchemaCreator,
                           LocalisationService localisationService,
                           ExpressionEvaluator expressionEvaluator, Optional<WorkflowActionDelegate> workflowActionDelegate) {

        this.securityChecker = securityChecker;
        this.crossContextConversionService = crossContextConversionService;
        this.resourceSchemaCreator = resourceSchemaCreator;
        this.localisationService = localisationService;
        this.expressionEvaluator = expressionEvaluator;
        this.workflowActionDelegate = workflowActionDelegate;
    }

    void processResource(ActionableResourceSupport resource) {
        if (resource == null) {
            return;
        }

        // TODO: cache workflows which can be started for any given action command, since these will be the same across all resource instances.
        List<ActionableResourceSupport.Action> actions = new ArrayList<>();

        final ActionCommands actionCommands = AnnotationUtils.findAnnotation(resource.getClass(), ActionCommands.class);
        Optional<Object> businessKey = actionCommands == null || actionCommands.businessKey().isEmpty() ?
                Optional.empty() :
                Optional.ofNullable(expressionEvaluator.evaluateExpression(resource, actionCommands.businessKey()))
                        .filter(it -> !StringUtils.isEmpty(it));
        businessKey.ifPresent(key -> {
            workflowActionDelegate.ifPresent(delegate ->
                    delegate.processExistingWorkflowTaskActions(resource, Optional.empty(), URI.create(key.toString()), true).forEach(actions::add));
            if (!workflowActionDelegate.isPresent()) {
                log.warn("ActionCommands declares a business key but no workflow engine is available to process it.");
            }
        });

        final ActionCommand[] actionCommandList = findAnnotations(resource);
        for (ActionCommand actionCommand : actionCommandList) {
            if (actionCommand.authorization().isEmpty() || securityChecker.check(actionCommand.authorization())) {
                if (actionCommand.condition().isEmpty() || checkCondition(actionCommand, resource)) {
                    log.info("Processing action command '" + actionCommand.name() + "' on resource " + resource.toString());

                    if (actionCommand.message().isEmpty()) {
                        if (Void.class == actionCommand.controller()) {
                            log.error("Action command '" + actionCommand.name() + "' has no workflow message or controller method specified.");
                        } else {
                            // TODO: Also check authorization on the controller method itself.
                            processControllerAction(resource, actionCommand).ifPresent(actions::add);
                        }
                    } else {
                        final URI commandBusinessKey;
                        if (actionCommand.createNewKey()) {
                            commandBusinessKey = URI.create(UUID.randomUUID().toString());
                        } else {
                            commandBusinessKey = crossContextConversionService.asUrn(URI.create(resource.getId().getHref()));
                        }
                        workflowActionDelegate.ifPresent(delegate -> {
                            delegate.processWorkflowAction(resource, actionCommand, commandBusinessKey).ifPresent(actions::add);
                            delegate.processExistingWorkflowTaskActions(resource, Optional.of(actionCommand), commandBusinessKey, false).forEach(actions::add);
                        });
                        if (!workflowActionDelegate.isPresent()) {
                            log.error("Action command {} requires workflow but no workflow engine is available to process it.");
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Action command '" + actionCommand.name() + "' has unsatisfied condition on resource " + resource.toString());
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Action command '" + actionCommand.name() + "' forbidden on resource " + resource.toString());
                }
            }
        }

        if (!actions.isEmpty()) {
            actions.forEach((action) -> {
                //convert this to a spring link to set on the resource
                TitledLink tl = action.toTitledLink();
                resource.add(tl);
            });
        }
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
        // TODO: validate that toUriComponentsBuilder() is OK and doesn't need replacing with UriComponentsBuilder.fromUriString(...toString()) to avoid double-encoding.
        URI href = linkTo(actionCommand.controller(), method, arguments).toUriComponentsBuilder().replaceQuery(null).build(true).toUri();
        // This linkTo() method doesn't handle trailing slashes on URLs very well, so fix it.
        if (requestMapping.path()[0].endsWith("/")) {
            href = URI.create(href.toString() + "/");
        }
        final ActionableResourceSupport.Action action = new ActionableResourceSupport.Action(
                href,
                actionCommand.title(),
                actionCommand.description(),
                resourceType == null ? new NullSchema() : resourceSchemaCreator.create(resourceType, href.resolve("$schema"), Optional.empty(), Optional.empty())
        );
        action.setEnctype(requestMapping.consumes().length > 0 ? requestMapping.consumes()[0] : MediaType.APPLICATION_JSON_VALUE);
        action.setMethod((requestMapping.method().length > 0 ? requestMapping.method()[0] : HttpMethod.POST).toString());
        if (requestMapping.produces().length > 0) {
            action.setMediaType(requestMapping.produces()[0]);
        }
        action.setTargetSchema(new NullSchema());
        action.setRel(actionCommand.name());
        if (!actionCommand.titleKey().isEmpty()) {
            localisationService.ifMessagePresent(actionCommand.titleKey(), action::setTitle);
        }
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

    private Object[] evaluateControllerMethodArguments(final ActionableResourceSupport resource, ActionCommand actionCommand, Method method, Parameter... parameters) {
        final Object[] arguments = new Object[parameters.length];
        final Iterator<Object> pathVariableValues = Arrays.stream(actionCommand.pathVariables())
                .map(p -> expressionEvaluator.evaluateParameter(p, resource)).iterator();

        // TODO: Support controller methods which take form posts with @RequestParam too
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getAnnotation(PathVariable.class) == null) {
                if (parameter.getAnnotation(RequestBody.class) != null && ResourceSupport.class.isAssignableFrom(parameter.getType())) {
                    log.debug("Skipping @RequestBody parameter " + i + " ");
                } else if (parameter.getAnnotation(AuthenticationPrincipal.class) != null || parameter.getAnnotation(org.springframework.security.web.bind.annotation.AuthenticationPrincipal.class) != null || Principal.class.isAssignableFrom(parameter.getType()) || Authentication.class.isAssignableFrom(parameter.getType())) {
                    log.debug("Skipping security parameter " + i + " [" + parameter + "]");
                } else if (parameter.getAnnotation(RequestBody.class) == null || !ResourceSupport.class.isAssignableFrom(parameter.getType())) {
                    log.error("Controller method " + method.toString() + " parameter " + i + " [" + parameter + "] is not a @PathVariable or a @RequestBody ResourceSupport");
                    return null;
                }
            } else {
                try {
                    arguments[i] = pathVariableValues.next();
                } catch (NoSuchElementException e) {
                    log.error("No path variable specified for parameter " + i + " [" + parameter + "]");
                    return null;
                }
            }
        }
        return arguments;
    }

    private boolean checkCondition(ActionCommand actionCommand, ActionableResourceSupport resource) {
        return expressionEvaluator.evaluateExpression(resource, actionCommand.condition(), Boolean.class);
    }

    ActionCommand[] findAnnotations(ActionableResourceSupport resource) {
        ActionCommand[] anns;
        final ActionCommands actionCommands = AnnotationUtils.findAnnotation(resource.getClass(), ActionCommands.class);
        if (actionCommands == null) {
            final ActionCommand ann = AnnotationUtils.findAnnotation(resource.getClass(), ActionCommand.class);
            if (ann == null) {
                anns = new ActionCommand[0];
            } else {
                anns = new ActionCommand[]{ann};
            }
        } else {
            anns = actionCommands.value();
        }
        return anns;
    }
}
