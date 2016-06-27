package ucles.weblab.common.config.forms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.web.bind.annotation.RestController;
import ucles.weblab.common.forms.domain.FormFactory;
import ucles.weblab.common.forms.domain.mongo.FormFactoryMongo;
import ucles.weblab.common.forms.domain.mongo.FormRepositoryMongo;
import ucles.weblab.common.forms.webapi.FormController;
import ucles.weblab.common.forms.webapi.FormDelegate;
import ucles.weblab.common.forms.webapi.FormResourceAssembler;

/**
 *
 * @author Sukhraj
 */
@Configuration
@EnableAutoConfiguration
@AutoConfigureAfter({MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
//@ConditionalOnBean(MongoOperations.class)
@ConditionalOnProperty(prefix = "spring.data.mongodb.repositories", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableMongoRepositories(basePackageClasses = {FormRepositoryMongo.class})
public class FormsMongoAutoConfiguration {      
        
    
    @Configuration
    @AutoConfigureAfter({DispatcherServletAutoConfiguration.class, WebMvcAutoConfiguration.class})
    @ConditionalOnWebApplication
    @ConditionalOnClass({RestController.class, ResourceAssembler.class, ObjectMapper.class})
    @ComponentScan(basePackageClasses = {FormController.class })
    public static class FormsAutoConfigurationWeb {
       
        @Bean
        public FormResourceAssembler formResourceAssembler(ObjectMapper objectMapper) {
            return new FormResourceAssembler(objectMapper);
        }

        @Bean
        public FormDelegate formDelegate(FormRepositoryMongo formRepositoryMongo,
                                        FormResourceAssembler formAssembler,
                                        FormFactory formFactory,
                                        ObjectMapper objectMapper) {
            return new FormDelegate(formRepositoryMongo, formAssembler, formFactory, objectMapper);
        }
    
    }
    
    @Bean
    public FormFactory formFactoryMongo() {
        return new FormFactoryMongo();
    }
}
