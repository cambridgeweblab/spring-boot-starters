package ucles.weblab.common.config.forms;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Sukhraj
 */
@Configuration
@AutoConfigureAfter({FormsMongoAutoConfiguration.class})
public class FormsAutoConfiguration {
    
}
