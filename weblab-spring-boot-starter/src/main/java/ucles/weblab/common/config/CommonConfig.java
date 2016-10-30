package ucles.weblab.common.config;

import com.mongodb.Mongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import ucles.weblab.common.domain.BuilderProxyFactory;
import ucles.weblab.common.domain.ConfigurableEntitySupport;
import ucles.weblab.common.domain.jpa.MoreCoreJavaJpaConverters;
import ucles.weblab.common.domain.jpa.MoreJsr310JpaConverters;
import ucles.weblab.common.domain.mongodb.MoreCoreJavaMongoConverters;
import ucles.weblab.common.webapi.ControllerExceptionHandler;
import ucles.weblab.common.webapi.converter.MoreGenericConverters;
import ucles.weblab.common.webapi.converter.MoreJsr310Converters;
import ucles.weblab.common.multipart.webapi.jersey.JerseyMultipartResolver;

import javax.persistence.EntityManager;
import javax.servlet.FilterChain;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import ucles.weblab.common.webapi.UnhandledExceptionHandler;
import ucles.weblab.common.webapi.exception.CommonControllerExceptionHandler;

/**
 * Auto-configuration for the common classes.
 *
 * @since 25/06/15
 */
@Configuration
@AutoConfigureBefore(DispatcherServletAutoConfiguration.class)
@ComponentScan(basePackageClasses = {CommonControllerExceptionHandler.class, UnhandledExceptionHandler.class})
@Import({ConfigurableEntitySupport.class, MoreGenericConverters.class, MoreJsr310Converters.class})
@EnableConfigurationProperties(MultipartProperties.class)
public class CommonConfig {
    private static final String X_FORWARDED_PORT = "X-Forwarded-Port";
    private static final String $_WSSP = "$wssp";

    @Autowired
   	MultipartProperties multipartProperties = new MultipartProperties();

    @Bean
    BuilderProxyFactory builderProxyFactory() {
        return new BuilderProxyFactory();
    }

    /**
     * Use JerseyMultipartResolver instead of the default StandardServletMultipartResolver or CommonsMultipartResolver
     * as it can handle base64 Content-Transfer-Encoding.
     */
    @Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
    @ConditionalOnMissingBean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
    MultipartResolver multipartResolver() {
        MultipartConfigElement multipartConfigElement = multipartProperties.createMultipartConfig();;
        JerseyMultipartResolver jerseyMultipartResolver = new JerseyMultipartResolver();
        jerseyMultipartResolver.setSizeMax(multipartConfigElement.getMaxRequestSize());
        jerseyMultipartResolver.setFileSizeMax(multipartConfigElement.getMaxFileSize());
        return jerseyMultipartResolver;
    }


    /**
     * Add an X-Forward-Port header if an $wssp header is present (i.e. if running on Bluemix Liberty for Java).
     * This is needed for the API to generate links correctly. Liberty already sets
     * X-Forwarded-Proto and Host correctly.
     */
    @Bean
    @ConditionalOnProperty(name = "bluemix.enable-wssp-filter", matchIfMissing = true)
    FilterRegistrationBean xForwardedPortFilterRegistration() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
                if (httpServletRequest.getHeader($_WSSP) != null && httpServletRequest.getHeader(X_FORWARDED_PORT) == null) {
                    // Wrap the request and add the header
                    httpServletRequest = new HttpServletRequestWrapper(httpServletRequest) {
                        List<String> headerNames = null;

                        @Override
                        public String getHeader(String name) {
                            return name.equalsIgnoreCase(X_FORWARDED_PORT)? super.getHeader($_WSSP) : super.getHeader(name);
                        }

                        @Override
                        public Enumeration<String> getHeaders(String name) {
                            return name.equalsIgnoreCase(X_FORWARDED_PORT)? super.getHeaders($_WSSP) : super.getHeaders(name);
                        }

                        @Override
                        public int getIntHeader(String name) {
                            return name.equalsIgnoreCase(X_FORWARDED_PORT)? super.getIntHeader($_WSSP) : super.getIntHeader(name);
                        }

                        @Override
                        public Enumeration<String> getHeaderNames() {
                            if (this.headerNames == null) {
                                this.headerNames = new ArrayList<>();
                                final Enumeration<String> e = super.getHeaderNames();
                                while (e.hasMoreElements()) {
                                    this.headerNames.add(e.nextElement());
                                }
                                this.headerNames.add(X_FORWARDED_PORT);
                            }
                            return Collections.enumeration(headerNames);
                        }
                    };
                }
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            }
        });
        registrationBean.setName("xForwardedPortFilter");
        return registrationBean;
    }


    @Configuration
    @ConditionalOnClass(EntityManager.class)
    @EntityScan(basePackageClasses = {MoreJsr310JpaConverters.class, MoreCoreJavaJpaConverters.class})
    public static class CommonJpaConvertersConfig {}

    @Configuration
    @ConditionalOnClass(Mongo.class)
    @Import({MoreCoreJavaMongoConverters.class})
    public static class CommonMongoConvertersConfig {}

}
