package ucles.weblab.common.domain.jpa;

import org.springframework.boot.orm.jpa.EntityScan;

import java.time.Year;
import java.time.YearMonth;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA 2.1 converter to turn JSR-310  {@link java.time.YearMonth} and {@link java.time.Year} types into {@link java.lang.String Strings}
 * for persistence.
 * To activate these converters, make sure your persistence provider detects it by including this class in the list of
 * mapped classes e.g. with the annotation property {@link EntityScan#basePackages()}.
 *
 * @see org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters
 *
 * @since 19/05/15
 */
public class MoreJsr310JpaConverters {
    @Converter(autoApply = true)
    public static class YearMonthConverter implements AttributeConverter<YearMonth, String> {
        @Override
        public String convertToDatabaseColumn(YearMonth yearMonth) {
            return yearMonth.toString();
        }

        @Override
        public YearMonth convertToEntityAttribute(String dbData) {
            return YearMonth.parse(dbData);
        }
    }

    @Converter(autoApply = true)
    public static class YearConverter implements AttributeConverter<Year, String> {
        @Override
        public String convertToDatabaseColumn(Year year) {
            return year.toString();
        }

        @Override
        public Year convertToEntityAttribute(String dbData) {
            return Year.parse(dbData);
        }
    }
}
