package ucles.weblab.common.config.files;

import com.amazonaws.auth.BasicAWSCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import ucles.weblab.common.files.domain.s3.SecureFileCollectionRepositoryS3;

/**
 *
 * @author Sukhraj
 */
@ConditionalOnClass(BasicAWSCredentials.class)
public class FilesS3AutoConfiguration {
    
    
}
