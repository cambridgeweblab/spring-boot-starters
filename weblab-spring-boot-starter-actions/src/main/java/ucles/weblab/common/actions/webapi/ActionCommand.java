package ucles.weblab.common.actions.webapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an action which can be carried out on a resource.
 * Used along with {@link ActionCommands @ActionCommands} to populate a list of actions, or on its own on an
 * {@link ActionableResourceSupport} to add a single action.
 * <p>
 * Actions can result in messages being fired into a workflow engine, if {@link #message() message} is specified,
 * or direct calls to controller methods, if {@link #controller() controller} and {@link #method() method} are specified.
 *
 * @since 03/11/15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ActionCommand {
    /** @return name of the action (required) */
    String name();

    /** @return title of the action, to be used for display purposes. Defaults to {@link #message() message} if not specified. */
    String title() default "";

    /** @return description of the action, to be used for display purposes. */
    String description() default "";

    /** @return condition, using Spring-EL, to be evaluated against the resource to determine if this action applies. */
    String condition() default "";

    /** @return the message to be fired into the workflow engine to initiate the action. */
    String message() default "";

    /** @return the Spring-EL expression to be evaluated to determine if this action applies. */
    String authorization() default "";

    /** @return the controller to invoke to initiate the action. Must be specified in conjunction with {@link #method() method}. */
    Class<?> controller() default Void.class;

    /** @return the controller method name to invoke to initiate the action. Must be specified in conjunction with {@link #controller() controller}. */
    String method() default "";

    /** @return the parameters to be passed to the controller method for pre-defined path variables. */
    ActionParameter[] pathVariables() default {};
    
    ActionParameterNameValue[] workFlowVariables() default {};
    
    boolean createNewKey() default false;
}
