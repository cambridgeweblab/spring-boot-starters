package ucles.weblab.common.webapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ucles.weblab.common.webapi.exception.ConflictException;
import ucles.weblab.common.webapi.exception.ForbiddenException;
import ucles.weblab.common.webapi.exception.ReferencedEntityNotFoundException;
import ucles.weblab.common.webapi.exception.ResourceNotFoundException;
import ucles.weblab.common.webapi.exception.UnknownRefererException;
import ucles.weblab.common.webapi.resource.ErrorResource;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

/**
 * Formats system exception as error responses which can be interpreted by secure-ajax.js.
 * Exception handlers on the controller take priority.
 *
 * @since 23/03/15
 */
@ConditionalOnWebApplication
@ControllerAdvice
@ResponseBody
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${suppress.errors:true}")
    private boolean suppressErrors;

    @ExceptionHandler(ForbiddenException.class)
    protected ResponseEntity<Object> handleForbiddenException(ForbiddenException e, WebRequest request) {
        return handleExceptionInternal(e, null, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        return handleExceptionInternal(e, new ErrorResource(e.getMessage(), String.valueOf(e.getResourceId())),
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(UnknownRefererException.class)
    protected ResponseEntity<Object> handleUnknownRefererException(UnknownRefererException e, WebRequest request) {
        return handleExceptionInternal(e, null, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = ReferencedEntityNotFoundException.class)
    protected ResponseEntity<Object> handleReferencedEntityNotFoundException(ReferencedEntityNotFoundException e, WebRequest request) {
        return handleExceptionInternal(e, new ErrorResource(e.getMessage(), String.valueOf(e.getEntityReference())),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = ConflictException.class)
    protected ResponseEntity<Object> handleConflictException(ConflictException e, WebRequest request) {
        if (e.getConflictingItems() == null || e.getConflictingItems().isEmpty()) {
            return handleExceptionInternal(e, new ErrorResource(e.getMessage(), String.valueOf(e.getEntityReference())),
                new HttpHeaders(), HttpStatus.CONFLICT, request);
        } else {
            return handleExceptionInternal(e, new ErrorResource(e.getMessage(), e.getDetail(), e.getConflictingItems()),
                new HttpHeaders(), HttpStatus.CONFLICT, request);
        }
    }

    /**
     * Overrides the default implementation to use status code 422 Unprocessable Entity for validation errors.
     *
     * @param ex {@inheritDoc}
     * @param headers {@inheritDoc}
     * @param status {@inheritDoc}
     * @param request {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, null, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    /**
     * Returns 400 Bad Request for multipart exceptions.
     * @param e the exception
     * @param request the current request
     * @return delegated to {@link #handleExceptionInternal(Exception, Object, HttpHeaders, HttpStatus, WebRequest)}
     */
    @ExceptionHandler(MultipartException.class)
    protected ResponseEntity<Object> handleMultipartException(MultipartException e, WebRequest request) {
        return handleExceptionInternal(e, null, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Overrides the default implementation to provide an {@link ErrorResource} body for all exceptions.
     *
     * @param ex      {@inheritDoc}
     * @param body    {@inheritDoc}. If null, an {@link ErrorResource} will be created with the exception message and stacktrace.
     * @param headers {@inheritDoc}
     * @param status  {@inheritDoc}
     * @param request {@inheritDoc}
     * @return
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Object updatedBody = body;
        if (updatedBody == null) {
            if (suppressErrors) {
                //if suppress errors, then just show a generic message.
                updatedBody = new ErrorResource("An exception has occured", "An exception has occured, more details are available in server logs");
            } else {
                //if not suppress errors, then show the whole stack trace to the client
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);

                updatedBody = new ErrorResource(ex.getMessage(), sw.toString());
            }
        }

        headers.setContentType(APPLICATION_JSON_UTF8);
        return super.handleExceptionInternal(ex, updatedBody, headers, status, request);
    }
}
