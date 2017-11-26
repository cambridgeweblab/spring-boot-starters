package ucles.weblab.common.test.webapi;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utilities for running tests in a Web MVC context.
 *
 * @since 13/04/15
 */
public final class WebTestSupport {
    private WebTestSupport() { // Prevent instantiation
    }

    /**
     * Creates a mock request (for http://localhost/) and places it into the request context for use by
     * {@link org.springframework.hateoas.mvc.ControllerLinkBuilder} and other MVC-environment classes.
     */
    public static void setUpRequestContext() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
