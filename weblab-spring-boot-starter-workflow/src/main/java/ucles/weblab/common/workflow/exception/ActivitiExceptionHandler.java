package ucles.weblab.common.workflow.exception;

import java.sql.SQLException;
import org.activiti.engine.ActivitiException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ucles.weblab.common.webapi.resource.ErrorResource;

/**
 * An exception handler to handle Activiti exception thrown from the workflow 
 * engine. 
 * 
 * @author Sukhraj
 */
@ControllerAdvice
@ResponseBody
public class ActivitiExceptionHandler extends ResponseEntityExceptionHandler  {
 
    @Value("${suppress.errors:true}")
    private boolean suppressErrors;
    
    /**
     * A handler to get the root cause of an ActivitiException and throw a sensible
     * error code based on the cause. 
     * 
     * @param e - the ActivitiException
     * @param request - the request context
     * @return
     * @throws Throwable - if the cause is something that is not handled, throw it. 
     */
    @ExceptionHandler(value = {ActivitiException.class})
    protected ResponseEntity<Object> handleActivitiException(ActivitiException e, WebRequest request) throws Throwable {
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        
        if (rootCause instanceof SQLException) {
            return handleExceptionInternal(e, 
                                    suppressErrors? null : new ErrorResource(e.getMessage(), String.valueOf(e.getCause().getMessage())),
                                    new HttpHeaders(), 
                                    HttpStatus.CONFLICT, 
                                    request);
        } else {
            throw rootCause;
        }        
    }
 
}
