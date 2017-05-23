package ucles.weblab.common.webapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

/**
 * Any unhandled exceptions are handled by this exception handler and a 500 error is thrown.
 *
 * The DispatcherServlet iterates through the exception handlers (in order) until it finds
 * ones that handles the exception.
 *
 * So if this is found first, then any exception that is not handled by an exception handler,
 * will be handled by this. To prevent this, always handle
 * unhandled exceptions (with a 500 error) last (lower precedence).
 *
 * @author Sukhraj
 */
@ConditionalOnWebApplication
@ControllerAdvice
@ResponseBody
@Order(Ordered.LOWEST_PRECEDENCE)
public class UnhandledExceptionHandler extends ControllerExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(UnhandledExceptionHandler.class);

    @Value("${suppress.errors:true}")
    private boolean suppressErrors;

    @ExceptionHandler
    protected ResponseEntity<Object> handleUnhandledException(Exception e, WebRequest request) throws Exception {
        // If the exception is an AccessDeniedException, rethrow it and let the framework handle it.
        if (e instanceof AccessDeniedException) {
            throw e;
        }
        // If the exception is annotated with @ResponseStatus rethrow it and let
        // the framework handle it
        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null) {
            throw e;
        }

        logger.warn("An unhandled internal exception was returned", e);
        return handleExceptionInternal(e, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
