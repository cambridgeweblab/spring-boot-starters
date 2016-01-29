package ucles.weblab.common.actions.webapi;

/**
 * Defines a parameter value to be substituted for a {@link org.springframework.web.bind.annotation.PathVariable @PathVariable}
 * when calling a controller-based action method.
 *
 * @since 06/11/15
 */
public @interface ActionParameter {
    /** @return Spring-EL expression to be evaluated against the resource to determine the value of the path variable. */
    String value();
}
