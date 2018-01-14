package ucles.weblab.common.test.context.support.auth0;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.*;

/**
 * When used with {@link org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener}
 * this annotation can be added to a test method to emulate running with an JWT access token.
 * The {@link SecurityContext} that is used will have the following properties:
 * <ul>
 * <li>The {@link SecurityContext} created with be that of
 * {@link SecurityContextHolder#createEmptyContext()}</li>
 * <li>It will be populated with an {@link OAuth2Authentication} that uses the token from {@link #value()},
 * </ul>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@WithSecurityContext(factory = WithJsonWebToken.WithJsonWebTokenSecurityContextFactory.class)
/**
 * Provides auth using a JWT.
 * Note that spring-security-test dependency is required on the test classpath for this to work
 */
public @interface WithJsonWebToken {
    String value();

    class NullVerifier implements SignatureVerifier {

        @Override
        public void verify(byte[] content, byte[] signature) {
            // pretend we verified
        }

        @Override
        public String algorithm() {
            return null;
        }
    }

    class WithJsonWebTokenSecurityContextFactory implements WithSecurityContextFactory<WithJsonWebToken> {

        private final JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        private final JwtTokenStore tokenStore = new JwtTokenStore(converter);

        WithJsonWebTokenSecurityContextFactory() {
            converter.setVerifier(new NullVerifier()); // We could possibly be more sophisticated
        }

        @Override
        public SecurityContext createSecurityContext(WithJsonWebToken withJsonWebToken) {
            OAuth2Authentication authentication = tokenStore.readAuthentication(withJsonWebToken.value());
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            return context;
        }
    }
}
