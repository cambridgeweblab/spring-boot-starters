package ucles.weblab.common.actions.webapi;

import com.fasterxml.jackson.module.jsonSchema.types.NullSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;
import ucles.weblab.common.i18n.service.LocalisationService;
import ucles.weblab.common.webapi.ActionCommand;
import ucles.weblab.common.webapi.ActionParameterNameValue;
import ucles.weblab.common.webapi.LinkRelation;
import ucles.weblab.common.webapi.TitledLink;
import ucles.weblab.common.webapi.resource.ActionableResourceSupport;
import ucles.weblab.common.workflow.domain.*;
import ucles.weblab.common.workflow.webapi.WorkflowController;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class WorkflowActionDelegateImpl implements WorkflowActionDelegate {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final DeployedWorkflowProcessRepository deployedWorkflowProcessRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final FormFieldSchemaCreator formFieldSchemaCreator;
    private final LocalisationService localisationService;
    private final ExpressionEvaluator expressionEvaluator;
    private final Collection<FormKeyHandler> formKeyHandlers;

    public WorkflowActionDelegateImpl(
            DeployedWorkflowProcessRepository deployedWorkflowProcessRepository,
            WorkflowTaskRepository workflowTaskRepository,
            FormFieldSchemaCreator formFieldSchemaCreator,
            LocalisationService localisationService,
            ExpressionEvaluator expressionEvaluator, Optional<List<FormKeyHandler>> formKeyHandlers) {
        this.deployedWorkflowProcessRepository = deployedWorkflowProcessRepository;
        this.workflowTaskRepository = workflowTaskRepository;
        this.formFieldSchemaCreator = formFieldSchemaCreator;
        this.localisationService = localisationService;
        this.expressionEvaluator = expressionEvaluator;
        if (log.isInfoEnabled()) {
            formKeyHandlers.ifPresent(hs -> {
                for (FormKeyHandler formKeyHandler : hs) {
                    log.info("Registering workflow form key handler: " + formKeyHandler.getName());
                }
            });
        }
        this.formKeyHandlers = formKeyHandlers.orElse(Collections.emptyList());
    }

    public Stream<ActionableResourceSupport.Action> processExistingWorkflowTaskActions(ActionableResourceSupport resource, Optional<ActionCommand> actionCommand, URI businessKey, boolean addHistoryLink) {
        final List<? extends WorkflowTaskEntity> tasks = workflowTaskRepository.findAllByProcessInstanceBusinessKey(businessKey.toString());
        // Also check for any existing workflow tasks for this resource
        // TODO: Demoware - this does not restrict by audience, so everyone sees all tasks
        Map<String, String> parameters = actionCommand.map(cmd -> evaluateWorkflowVariables(resource, cmd.workFlowVariables()))
                .orElse(Collections.emptyMap());

        // Add a history link to the workflow audit trail.
        if (addHistoryLink) {
            resource.add(new TitledLink(linkTo(methodOn(WorkflowController.class).listWorkflowAudit(businessKey.toString())),
                    LinkRelation.ARCHIVES.rel(), "History", HttpMethod.GET.name()));
        }

        return tasks.stream()
                .map(t -> processWorkflowTaskAction(t, businessKey.toString(), parameters));
    }

    private Map<String, String> evaluateWorkflowVariables(ActionableResourceSupport resource, ActionParameterNameValue[] workFlowVariables) {
        //evaluate the values using spring
        Map<String, String> parameters = new HashMap<String, String>();
        for (ActionParameterNameValue v : workFlowVariables) {
            Object value = expressionEvaluator.evaluateExpression(resource, v.value());
            parameters.put(v.name(), value.toString());
        }

        return parameters;
    }

    public Optional<ActionableResourceSupport.Action> processWorkflowAction(ActionableResourceSupport resource, ActionCommand actionCommand, final URI businessKey) {

        final List<? extends DeployedWorkflowProcessEntity> targetProcesses = deployedWorkflowProcessRepository.findAllByStartMessage(actionCommand.message());

        if (!targetProcesses.isEmpty()) {
            // TODO: demoware - this check is too broad, we want multiple users to be able to create workflows, and we don't mind multiple workflows of the same type either.
            // This checks for ANY process for this resource with a user task, and refuses to create a new one if there is already one.
            List<? extends WorkflowTaskEntity> existingTasks = workflowTaskRepository.findAllByProcessInstanceBusinessKey(businessKey.toString());
            if (existingTasks.isEmpty()) {
                final Stream<WorkflowTaskFormField> fields = targetProcesses.stream()
                        .flatMap(p -> p.getStartFormFields().stream());
                Map<String, String> parameters = evaluateWorkflowVariables(resource, actionCommand.workFlowVariables());

                final ControllerLinkBuilder actionLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ActionAwareWorkflowController.class).handleEvent(businessKey.toString(), actionCommand.message(), parameters, null));
                final ActionableResourceSupport.Action action = new ActionableResourceSupport.Action(
                        UriComponentsBuilder.fromUriString(actionLink.toString())
                                .replaceQuery(null).build(true).toUri(), // strip parameters
                        actionCommand.title().isEmpty() ? actionCommand.message() : actionCommand.title(),
                        actionCommand.description(),
                        formFieldSchemaCreator.createSchema(fields, parameters)
                );
                action.setEnctype(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
                action.setMethod(HttpMethod.POST.toString());
                action.setTargetSchema(new NullSchema());
                action.setRel(actionCommand.name());
                if (!actionCommand.titleKey().isEmpty())
                    localisationService.ifMessagePresent(actionCommand.titleKey(), action::setTitle);
                return Optional.of(action);
            } else {
                log.debug("Found a workflow action command but there was already a process with the business key");
            }
        } else {
            log.debug("Found a workflow action command but no start target was found");
        }
        return Optional.empty();
    }

    @Override
    public ActionableResourceSupport.Action processWorkflowTaskAction(WorkflowTaskEntity task, String businessKey, Map<String, String> parameters) {
        final String formKey = task.getFormKey();

        FormKeyHandler formKeyHandler = formKeyHandlers.stream()
                .filter(handler -> handler.canCreateActions(formKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(formKey + " form key is found but no suitable FormKeyHandler available in "
                        + formKeyHandlers.stream().map(FormKeyHandler::getName).collect(Collectors.toList())));

        return formKeyHandler.createAction(task, businessKey, parameters);
    }
}