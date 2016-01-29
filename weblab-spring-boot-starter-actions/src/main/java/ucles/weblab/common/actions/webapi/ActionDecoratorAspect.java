package ucles.weblab.common.actions.webapi;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import ucles.weblab.common.webapi.resource.ResourceListWrapper;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.util.Collections.singleton;

/**
 * Aspect which intercepts controller methods returning {@link ActionableResourceSupport} instances annotated with
 * either {@link ActionCommand @ActionCommand} or {@link ActionCommands @ActionCommands}.
 * <p>
 * If said action commands have messages associated, it consults the workflow engine to see if there are any processes
 * which can be started with that message and if so, returns the details of the process and any form data required.
 * <p>
 * To use this aspect with controllers and dynamic proxies, the controllers must either implement no interfaces at all,
 * or they must implement an interface with all the
 * {@code @RequestMapping} etc annotations on the interface, otherwise the controllers will not be autodiscovered.
 * See <a href='http://stackoverflow.com/a/9685792/1112217'>http://stackoverflow.com/a/9685792/1112217</a>.
 *
 * @since 03/11/15
 */
@Aspect
public class ActionDecoratorAspect {
    private final ActionDecorator actionDecorator;

    public ActionDecoratorAspect(ActionDecorator actionDecorator) {
        this.actionDecorator = actionDecorator;
    }

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    private void inRestController() {
    }

    @Pointcut("execution(public ActionableResourceSupport+ *(..))")
    private void publicMethodReturningActionableResource() {
    }

    @Pointcut("execution(public org.springframework.http.ResponseEntity<ActionableResourceSupport+> *(..))")
    private void publicMethodReturningActionableResourceResponseEntity() {
    }

    @Pointcut("execution(public ucles.weblab.common.webapi.resource.ResourceListWrapper<ActionableResourceSupport+> *(..))")
    private void publicMethodReturningActionableResources() {
    }

    @AfterReturning(pointcut = "inRestController() && publicMethodReturningActionableResource()", returning = "resource")
    public void addActionsToSingleResource(ActionableResourceSupport resource) {
        actionDecorator.processResource(resource);
    }

    @AfterReturning(pointcut = "inRestController() && publicMethodReturningActionableResourceResponseEntity()", returning = "responseEntity")
    public void addActionsToSingleResponseEntity(ResponseEntity<? extends ActionableResourceSupport> responseEntity) {
        actionDecorator.processResource(responseEntity.getBody());
    }

    @AfterReturning(pointcut = "inRestController() && publicMethodReturningActionableResources()", returning = "resourceList")
    public void addActionsToMultipleResources(ResourceListWrapper<? extends ActionableResourceSupport> resourceList) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        final Set<Thread> propagated = new CopyOnWriteArraySet<>(singleton(Thread.currentThread()));
        resourceList.getList().parallelStream().forEach((resource) -> {
            if (propagated.add(Thread.currentThread())) {
                // Propagate authentication and request into parallel stream handler since it is thread-bound...
                SecurityContextHolder.getContext().setAuthentication(authentication);
                RequestContextHolder.setRequestAttributes(requestAttributes);
            }
            actionDecorator.processResource(resource);
        });
    }
}
