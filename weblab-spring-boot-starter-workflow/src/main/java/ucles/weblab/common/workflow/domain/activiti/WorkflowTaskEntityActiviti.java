package ucles.weblab.common.workflow.domain.activiti;

import org.activiti.engine.FormService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.FormType;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import ucles.weblab.common.workflow.domain.WorkflowTaskEntity;
import ucles.weblab.common.workflow.domain.WorkflowTaskFormField;

import java.util.List;
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
    void configureWorkflowTaskFormFieldBuilder(Supplier<WorkflowTaskFormField.Builder> workflowTaskFormFieldBuilder) {
        this.workflowTaskFormFieldBuilder = workflowTaskFormFieldBuilder;
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
    public List<? extends WorkflowTaskFormField> getFormFields() {
        // See also formProperty.getType().getInformation("datePattern") and formProperty.getType().getInformation("values")
        Function<FormType, WorkflowTaskFormField.FormFieldType> typeMapper = t ->
                WorkflowTaskFormField.FormFieldType.valueOf(t.getName().toUpperCase());

        return formService.getTaskFormData(task.getId()).getFormProperties().stream()
                .filter(FormProperty::isWritable)
                .map(f -> workflowTaskFormFieldBuilder.get()
                        .name(f.getId())
                        .label(f.getName())
                        .description("")
                        .required(f.isRequired())
                        .defaultValue(f.getValue())
                        .type(typeMapper.apply(f.getType()))
                        .get())
                .collect(toList());
    }
}
