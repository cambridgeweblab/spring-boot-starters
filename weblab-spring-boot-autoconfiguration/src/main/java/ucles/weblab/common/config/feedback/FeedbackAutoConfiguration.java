package ucles.weblab.common.config.feedback;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.web.bind.annotation.RestController;
import ucles.weblab.common.audit.domain.AccessAuditRepository;
import ucles.weblab.common.feedback.domain.jpa.FeedbackEntityJpa;
import ucles.weblab.common.feedback.web.FeedbackDelegate;
import ucles.weblab.common.feedback.web.FeedbackResource;
import ucles.weblab.common.feedback.domain.Feedback;
import ucles.weblab.common.feedback.domain.FeedbackFactory;
import ucles.weblab.common.feedback.domain.FeedbackRepository;
import ucles.weblab.common.feedback.domain.jpa.FeedbackFactoryJpa;
import ucles.weblab.common.feedback.web.FeedbackAdaptor;
import ucles.weblab.common.feedback.web.FeedbackController;
import ucles.weblab.common.feedback.web.FeedbackResourceAssembler;

import java.util.function.Function;

/**
 * Auto-configuration for the feedback bounded context.
 *
 * @author bodeng
 */
@Configuration
public class FeedbackAutoConfiguration {

    @Configuration
    @AutoConfigureAfter({DataSourceAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
    @ConditionalOnClass({JpaRepository.class, FeedbackRepository.class})
    @ConditionalOnProperty(prefix = "spring.data.jpa.repositories", name = "enabled", havingValue = "true", matchIfMissing = true)
    @EnableJpaRepositories(basePackageClasses = {FeedbackRepository.class})
    @EntityScan(basePackageClasses = {FeedbackEntityJpa.class, Jsr310JpaConverters.class})
    public static class FeedbackAutoConfigurationJpa {
        @Bean
        FeedbackFactory feedbackFactory() {
            return new FeedbackFactoryJpa();
        }
    }

    @Configuration
    @AutoConfigureAfter({DispatcherServletAutoConfiguration.class, WebMvcAutoConfiguration.class})
    @ConditionalOnWebApplication
    @ConditionalOnClass({FeedbackController.class, RestController.class, RepresentationModelAssembler.class, ObjectMapper.class})
    public static class FeedbackAutoConfigurationWeb {

        @Bean
        public FeedbackController feedbackController(FeedbackDelegate feedbackDelegate) {
            return new FeedbackController(feedbackDelegate);
        }

        @Bean
        public FeedbackResourceAssembler feedbackResourceAssembler() {
            return new FeedbackResourceAssembler();
        }

        @Bean
        public Function<FeedbackResource, Feedback> feedbackResourceToValue() {
            return FeedbackAdaptor::new;
        }

        @Bean
        FeedbackDelegate feedbackDelegate(FeedbackFactory feedbackFactory,
                                          FeedbackRepository feedbackRepository,
                                          AccessAuditRepository accessAuditRepository,
                                          FeedbackResourceAssembler feedbackResourceAssembler,
                                          Function<FeedbackResource, Feedback> feedbackResourceToValue) {

            return new FeedbackDelegate(feedbackFactory,
                                        feedbackRepository,
                                        accessAuditRepository,
                                        feedbackResourceAssembler,
                                        feedbackResourceToValue);
        }


    }

}
