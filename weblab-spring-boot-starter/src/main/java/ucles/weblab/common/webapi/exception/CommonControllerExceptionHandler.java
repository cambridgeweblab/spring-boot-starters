package ucles.weblab.common.webapi.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import ucles.weblab.common.webapi.ControllerExceptionHandler;
import ucles.weblab.common.webapi.resource.ErrorResource;

/**
 * This exception controller extends ucles.weblab.common.webapi.ControllerExceptionHandler and 
 * provides specific handlers for this bounded context. 
 * 
 * Spring will iterate through the exception handlers IN ORDER until it finds ones that handles the 
 * exception being thrown. If an exception handler is registered before spring, 
 * that handler will be called. 
 * 
 * The ordering is needed so that specific exception other than those
 * in the superclass can be handled here and not by the java.lang.Exception handler. 
 * 
 * @author Sukhraj
 */
@ControllerAdvice
@ResponseBody
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonControllerExceptionHandler extends ControllerExceptionHandler{
    
    @ExceptionHandler(value = { DataIntegrityViolationException.class, TransactionSystemException.class })
    protected ResponseEntity<Object> handleDBException(DataIntegrityViolationException e, WebRequest request) {
        
        return handleExceptionInternal(e, new ErrorResource(e.getMessage(), String.valueOf(e.getMostSpecificCause().getMessage())),
                                      new HttpHeaders(), HttpStatus.CONFLICT, request);
    }
    
}
