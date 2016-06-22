package ucles.weblab.common.config.forms;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import ucles.weblab.common.forms.domain.FormFactory;
import ucles.weblab.common.forms.domain.FormRepository;
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
@ConditionalOnBean(MongoOperations.class)
@ConditionalOnProperty(prefix = "spring.data.mongodb.repositories", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackageClasses = FormController.class)
@EnableMongoRepositories(basePackageClasses = {FormRepositoryMongo.class})
public class FormsMongoAutoConfiguration {
    
    @Bean
    public FormDelegate formDelegate(FormRepositoryMongo formRepositoryMongo,
                                    FormResourceAssembler formAssembler,
                                    FormFactory formFactory) {
        return new FormDelegate(formRepositoryMongo, formAssembler, formFactory);
    }
    
    @Bean
    public FormFactory formFactoryMongo() {
        return new FormFactoryMongo();
    }
}
