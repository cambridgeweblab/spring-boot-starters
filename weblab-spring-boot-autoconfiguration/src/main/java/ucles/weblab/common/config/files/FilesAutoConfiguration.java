package ucles.weblab.common.config.files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ucles.weblab.common.files.domain.AesGcmEncryptionStrategy;
import ucles.weblab.common.files.domain.AutoPurgeSecureFileCollectionServiceImpl;
import ucles.weblab.common.files.domain.DummyEncryptionStrategy;
import ucles.weblab.common.files.domain.EncryptionService;
import ucles.weblab.common.files.domain.EncryptionServiceImpl;
import ucles.weblab.common.files.domain.FilesBuilders;
import ucles.weblab.common.files.domain.SecureFileCollectionRepository;
import ucles.weblab.common.files.domain.SecureFileCollectionService;
import ucles.weblab.common.files.webapi.FileController;
import ucles.weblab.common.files.webapi.converter.FilesConverters;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Auto-configuration for the files domain.
 */
@Configuration
@ConditionalOnClass({EncryptionService.class, SecureFileCollectionRepository.class})
@AutoConfigureAfter({FilesJpaAutoConfiguration.class, FilesMongoAutoConfiguration.class})
@Import({FilesConverters.class, FilesBuilders.class})
@ComponentScan(basePackageClasses = FileController.class)
public class FilesAutoConfiguration {
    @Bean
    @ConditionalOnProperty("files.security.secretkey")
    @ConditionalOnMissingBean(EncryptionService.class)
    public EncryptionService encryptionService(@Value("${files.security.secretkey}") String secretKey) {
        return new EncryptionServiceImpl(Arrays.asList(new AesGcmEncryptionStrategy(), new DummyEncryptionStrategy()),
                secretKey.getBytes(UTF_8));
    }

    @Bean
    public SecureFileCollectionService secureFileCollectionService(SecureFileCollectionRepository secureFileCollectionRepository) {
        return new AutoPurgeSecureFileCollectionServiceImpl(secureFileCollectionRepository);
    }
}
