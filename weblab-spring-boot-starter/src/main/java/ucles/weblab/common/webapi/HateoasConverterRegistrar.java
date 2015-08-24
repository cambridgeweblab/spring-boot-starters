package ucles.weblab.common.webapi;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.format.support.DefaultFormattingConversionService;

import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * Utility methods for registering converters with Spring HATEOAS.
 *
 * @since 24/08/15
 */
public class HateoasConverterRegistrar {
    private HateoasConverterRegistrar() { // Prevent instantiation
    }

    /**
     * Registers additional converters with Spring HATEOAS.
     * Spring HATEOAS otherwise only registers default converters.
     * @see <a href='http://stackoverflow.com/a/24081785'>Converter from @PathVariable DomainObject to String? (using ControllerLinkBuilder.methodOn)</a>
     *
     * @param converters the additional converters to register
     */
    public static void registerConverters(Converter<?, ?>... converters) {
        try {
            Class<?> clazz = Class.forName("org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor$BoundMethodParameter");
            Field field = clazz.getDeclaredField("CONVERSION_SERVICE");
            field.setAccessible(true);
            DefaultFormattingConversionService service = (DefaultFormattingConversionService) field.get(null);
            for (Converter<?, ?> converter : converters) {
                service.addConverter(converter);
            }
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Registers additional converters with Spring HATEOAS, where the converters need a back reference to the conversion service.
     * Spring HATEOAS otherwise only registers default converters.
     * @see <a href='http://stackoverflow.com/a/24081785'>Converter from @PathVariable DomainObject to String? (using ControllerLinkBuilder.methodOn)</a>
     *
     * @param converterSource the additional converters to register
     */
    public static void registerConverter(Function<ConversionService, GenericConverter> converterSource) {
        try {
            Class<?> clazz = Class.forName("org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor$BoundMethodParameter");
            Field field = clazz.getDeclaredField("CONVERSION_SERVICE");
            field.setAccessible(true);
            DefaultFormattingConversionService service = (DefaultFormattingConversionService) field.get(null);
            service.addConverter(converterSource.apply(service));
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
