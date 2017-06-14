package ucles.weblab.common.workflow.domain.activiti;

import org.activiti.engine.FormService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.FormType;
import org.activiti.engine.impl.form.EnumFormType;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import ucles.weblab.common.workflow.domain.WorkflowTaskContext;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;
import ucles.weblab.common.workflow.domain.WorkflowTaskFormField;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static ucles.weblab.common.domain.ConfigurableEntitySupport.configureBean;

/**
 * Entity class for retrieving a workflow user task as an Activiti Task.
 *
 * @since 20/07/15
 */
@Configurable
public class WorkflowTaskEntityActiviti implements WorkflowTaskEntity {
    private final Task task;
    private FormService formService;
    private Supplier<WorkflowTaskFormField.Builder> workflowTaskFormFieldBuilder;
    private Supplier<WorkflowTaskContext.Builder> workflowTaskContextBuilder;

    {
        configureBean(this);
    }

    public Object readResolve() {
        configureBean(this);
        return this;
    }

    public WorkflowTaskEntityActiviti(Task task) {
        this.task = task;
    }

    @Autowired
    void configureFormService(FormService formService) {
        this.formService = formService;
    }

    @Autowired
    void configureWorkflowBuilders(Supplier<WorkflowTaskFormField.Builder> workflowTaskFormFieldBuilder, Supplier<WorkflowTaskContext.Builder> workflowTaskContextBuilder) {
        this.workflowTaskFormFieldBuilder = workflowTaskFormFieldBuilder;
        this.workflowTaskContextBuilder = workflowTaskContextBuilder;
    }

    @Override
    public String getId() {
        return task.getId();
    }

    @Override
    public String getName() {
        return task.getName();
    }

    @Override
    public String getDescription() {
        return task.getDescription();
    }

    @Override
    public String getFormKey() {
        return task.getFormKey();
    }

    @Override
    public String getTaskDefinitionKey() {
        return task.getTaskDefinitionKey();
    }

    @Override
    public String getProcessKey() {
        return task.getProcessDefinitionId().substring(0, task.getProcessDefinitionId().indexOf(':'));
    }

    @Override
    public List<? extends WorkflowTaskFormField> getFormFields() {
        // See also formProperty.getType().getInformation("datePattern") and formProperty.getType().getInformation("values")
        Function<FormType, WorkflowTaskFormField.FormFieldType> typeMapper = t ->
                WorkflowTaskFormField.FormFieldType.valueOf(t.getName().toUpperCase());

        return formService.getTaskFormData(task.getId()).getFormProperties().stream()
                .filter(FormProperty::isWritable)
                .map(f -> {
                    WorkflowTaskFormField.Builder builder = workflowTaskFormFieldBuilder.get()
                            .name(f.getId())
                            .label(f.getName())
                            .description("")
                            .required(f.isRequired())
                            .defaultValue(f.getValue())
                            .type(typeMapper.apply(f.getType()));
                    if (f.getType() instanceof EnumFormType) {
                        builder.enumValues((Map<String, String>) f.getType().getInformation("values"));
                    }
                    return builder.get();
                })
                .collect(toList());
    }

    @Override
    public WorkflowTaskContext getContext() {
        return workflowTaskContextBuilder.get()
                .variables(task.getProcessVariables())
                .get();
    }
}
