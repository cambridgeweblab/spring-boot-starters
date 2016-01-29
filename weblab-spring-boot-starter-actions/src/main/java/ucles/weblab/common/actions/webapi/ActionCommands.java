package ucles.weblab.common.actions.webapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines actions which can be carried out on a resource.
 * Used along with {@link ActionableResourceSupport} to populate a list of possible actions.
 *
 * @since 03/11/15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ActionCommands {
    ActionCommand[] value();
}
