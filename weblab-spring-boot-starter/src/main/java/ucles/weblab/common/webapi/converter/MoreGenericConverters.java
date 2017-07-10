package ucles.weblab.common.webapi.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import ucles.weblab.common.webapi.HateoasConverterRegistrar;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Configuration of more default utility converters .
 * These are omitted from the normal Spring {@link org.springframework.core.convert.support.DefaultConversionService}.
 * This configuration also registers the new converters with Spring HATEOAS if it is on the classpath.
 */
@Configuration
@ConditionalOnBean(ConversionService.class)
@ConditionalOnClass(name = "java.util.Optional")
public class MoreGenericConverters {

    @Bean
    @ConditionalOnBean(name = "mvcConversionService")
    public OptionalToObjectConverter optionalToObjectConverter(
            @Qualifier("mvcConversionService") ConversionService conversionService) {
        return new OptionalToObjectConverter(conversionService);
    }

    /**
     * Convert an {@code java.util.Optional<T>} to Object if necessary using the
     * {@code ConversionService} to convert the generic type
     * of Optional when known to the source Object.
     *
     * @see org.springframework.core.convert.support.ObjectToOptionalConverter
     * @since 14/07/15
     */
    public static class OptionalToObjectConverter implements ConditionalGenericConverter {
        private final ConversionService conversionService;

        public OptionalToObjectConverter(ConversionService conversionService) {
            this.conversionService = conversionService;
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(new ConvertiblePair(Optional.class, Object.class));
        }

        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            return sourceType.getResolvableType() == null || this.conversionService.canConvert(new GenericTypeDescriptor(sourceType), targetType);
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (targetType.getType() == Optional.class) {
                return source;
            }

            Optional optional = (Optional) source;
            if (sourceType.getResolvableType() == null) {
                return optional.orElse(null);
            } else {
                return optional.map(v -> this.conversionService.convert(v, new GenericTypeDescriptor(sourceType), targetType)).orElse(null);
            }
        }

        @SuppressWarnings("serial")
        private static class GenericTypeDescriptor extends TypeDescriptor {

            public GenericTypeDescriptor(TypeDescriptor typeDescriptor) {
                super(typeDescriptor.getResolvableType().getGeneric(0), null, typeDescriptor.getAnnotations());
            }
        }
    }

    @Configuration
    @ConditionalOnClass(name = "org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor")
    static class GenericConverterRegistration {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Bean
        CommandLineRunner registerGenericConvertersWithSpringHateoas() {
            return args -> {
                HateoasConverterRegistrar.registerConverter(OptionalToObjectConverter::new);
                logger.info("Registered more generic converters with spring-hateoas.");
            };
        }
    }
}
