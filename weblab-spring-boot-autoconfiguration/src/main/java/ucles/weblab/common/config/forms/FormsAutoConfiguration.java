package ucles.weblab.common.config.forms;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
//import ucles.weblab.appeals.web.FormController;

/**
 *
 * @author Sukhraj
 */
@Configuration
@AutoConfigureAfter({FormsMongoAutoConfiguration.class})
//@ComponentScan(basePackageClasses = FormController.class)
public class FormsAutoConfiguration {
    
}
