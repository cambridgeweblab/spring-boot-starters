package ucles.weblab.common.config.i18n;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ucles.weblab.common.i18n.service.LocalisationService;
import ucles.weblab.common.i18n.service.impl.LocalisationServiceImpl;

/**
 * Configure the {@link ucles.weblab.common.i18n.service.LocalisationService} ready to be injected into controllers.
 *
 * @since 07/10/15
 */
@Configuration
@ConditionalOnClass({ LocalisationServiceImpl.class })
public class LocalisationAutoConfiguration {
    @Bean
    LocalisationService localisationService(MessageSource messageSource) {
        return new LocalisationServiceImpl(messageSource);
    }
}
