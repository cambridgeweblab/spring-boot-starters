package ucles.weblab.common.webapi.exception;

import org.springframework.core.NestedRuntimeException;

import java.io.Serializable;

/**
 * This exception is thrown by controllers when an entity which is referenced by the request parameters or body,
 * rather than by the URL, is not found.
 * <p>
 * When it's by the URL, then it should be a straightforward 404 instead.
 *
 * @since 27/03/15
 */
public class ReferencedEntityNotFoundException extends NestedRuntimeException {
    private final Serializable entityReference;

    public ReferencedEntityNotFoundException(String msg, Serializable entityReference) {
        super(msg);
        this.entityReference = entityReference;
    }

    public ReferencedEntityNotFoundException(String msg, Serializable entityReference, Throwable cause) {
        super(msg, cause);
        this.entityReference = entityReference;
    }

    public Serializable getEntityReference() {
        return entityReference;
    }
}
