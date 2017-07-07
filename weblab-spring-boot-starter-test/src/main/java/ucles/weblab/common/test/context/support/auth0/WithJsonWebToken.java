package ucles.weblab.common.test.context.support.auth0;

import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.*;

/**
 * When used with {@link org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener} this annotation can be
 * added to a test method to emulate running with an JWT access token.
 * The {@link SecurityContext} that is used will have the following
 * properties:
 *
 * <ul>
 * <li>The {@link SecurityContext} created with be that of
 * {@link SecurityContextHolder#createEmptyContext()}</li>
 * <li>It will be populated with an {@link PreAuthenticatedAuthenticationJsonWebToken} that uses
 * the token from {@link #value()},
 * </ul>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@WithSecurityContext(factory = WithJsonWebToken.WithJsonWebTokenSecurityContextFactory.class)
public @interface WithJsonWebToken {
    String value();

    class WithJsonWebTokenSecurityContextFactory implements WithSecurityContextFactory<WithJsonWebToken> {
        @Override
        public SecurityContext createSecurityContext(WithJsonWebToken withJsonWebToken) {
            PreAuthenticatedAuthenticationJsonWebToken authentication;

            authentication = PreAuthenticatedAuthenticationJsonWebToken.usingToken(withJsonWebToken.value());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            return context;
        }
    }
}
