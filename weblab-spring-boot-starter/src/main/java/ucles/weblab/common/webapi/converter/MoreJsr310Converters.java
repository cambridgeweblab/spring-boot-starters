package ucles.weblab.common.webapi.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import ucles.weblab.common.webapi.HateoasConverterRegistrar;

import java.time.Year;
import java.time.YearMonth;

/**
 * Configuration of converters required for JSR-310 {@link java.time.YearMonth YearMonths} and {@link java.time.Year Years}.
 * These are omitted from the normal Spring JSR-310 converters.
 * This configuration also registers the new converters with Spring HATEOAS if it is on the classpath.
 *
 * @see org.springframework.data.convert.Jsr310Converters
 * @since 19/05/15
 */
@Configuration
@ConditionalOnClass(name = "java.time.YearMonth")
public class MoreJsr310Converters {
    @Bean
    public StringToYearMonthConverter stringYearMonthConverter() {
        return StringToYearMonthConverter.INSTANCE;
    }

    @Bean
    public YearMonthToStringConverter yearMonthStringConverter() {
        return YearMonthToStringConverter.INSTANCE;
    }

    @Bean
    public StringToYearConverter stringYearConverter() {
        return StringToYearConverter.INSTANCE;
    }

    @Bean
    public YearToStringConverter yearStringConverter() {
        return YearToStringConverter.INSTANCE;
    }

    public enum StringToYearMonthConverter implements Converter<String, YearMonth> {
        INSTANCE;

        @Override
        public YearMonth convert(String s) {
            return YearMonth.parse(s);
        }
    }

    public enum YearMonthToStringConverter implements Converter<YearMonth, String> {
        INSTANCE;

        @Override
        public String convert(YearMonth yearMonth) {
            return yearMonth.toString();
        }
    }

    public enum StringToYearConverter implements Converter<String, Year> {
        INSTANCE;

        @Override
        public Year convert(String s) {
            return Year.parse(s);
        }
    }

    public enum YearToStringConverter implements Converter<Year, String> {
        INSTANCE;

        @Override
        public String convert(Year year) {
            return year.toString();
        }
    }

    @Configuration
    @ConditionalOnClass(name = "org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor")
    static class Jsr310ConverterRegistration {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Bean
        CommandLineRunner registerJsr310ConvertersWithSpringHateoas() {
            return args -> {
                HateoasConverterRegistrar.registerConverters(StringToYearMonthConverter.INSTANCE, YearMonthToStringConverter.INSTANCE,
                        StringToYearConverter.INSTANCE, YearToStringConverter.INSTANCE);
                logger.info("Registered more JSR-310 converters with spring-hateoas.");
            };
        }
    }
}
