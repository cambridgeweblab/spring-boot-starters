package ucles.weblab.common.domain.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converters to turn {@link java.net.URI} and {@link java.net.InetAddress} into {@link java.lang.String Strings}
 * for persistence. By default, these will be registered using a
 * {@link CustomConversions} bean. If you create your own {@code CustomConversions}
 * bean then you will need to make sure to include the converters returned by {@link #getConvertersToRegister} if you want them.
 *
 * @since 30/10/2016
 * @see org.springframework.data.mongodb.core.convert.MongoConverters
 */
@Configuration
@ConditionalOnMissingBean(CustomConversions.class)
public class MoreCoreJavaMongoConverters {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean(name = "MoreCoreJavaMongoConverters.customConversions")
    public CustomConversions customConversions() {
        logger.info("Registered more core Java converters with spring-data-mongodb.");
        return new CustomConversions(getConvertersToRegister());
    }

    /**
     * Returns the converters to be registered.
     */
    public static List<Object> getConvertersToRegister() {
        List<Object> converters = new ArrayList<Object>();
        converters.add(UriToStringConverter.INSTANCE);
        converters.add(StringToUriConverter.INSTANCE);
        converters.add(InetAddressToStringConverter.INSTANCE);
        converters.add(StringToInetAddressConverter.INSTANCE);

        return converters;
    }

    @WritingConverter
    public enum UriToStringConverter implements Converter<URI, String> {
        INSTANCE;

        @Override
        public String convert(URI source) {
            return source == null? null : source.toString();
        }
    }

    @ReadingConverter
    public enum StringToUriConverter implements Converter<String, URI> {
        INSTANCE;

        @Override
        public URI convert(String source) {
            return !StringUtils.hasText(source)? null: URI.create(source);
        }
    }

    @WritingConverter
    public enum InetAddressToStringConverter implements Converter<InetAddress, String> {
        INSTANCE;

        @Override
        public String convert(InetAddress source) {
            return source == null? null : source.getHostAddress();
        }
    }

    @ReadingConverter
    public enum StringToInetAddressConverter implements Converter<String, InetAddress> {
        INSTANCE;

        @Override
        public InetAddress convert(String source) {
            try {
                return !StringUtils.hasText(source)? null : InetAddress.getByName(source);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Unknown value for InetAddress: " + source, e);
            }
        }
    }
}
