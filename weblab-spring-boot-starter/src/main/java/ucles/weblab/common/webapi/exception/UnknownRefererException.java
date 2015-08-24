package ucles.weblab.common.webapi.exception;

/**
 * Exception to be thrown when a request needs a refer to resolve a relative URI but no Referer header is present and/or
 * the referer is not know.
 *
 * @since 06/07/15
 */
public class UnknownRefererException extends RuntimeException {
}
