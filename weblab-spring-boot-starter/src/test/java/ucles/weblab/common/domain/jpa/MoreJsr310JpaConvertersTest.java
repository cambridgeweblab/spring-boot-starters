package ucles.weblab.common.domain.jpa;

import org.junit.Test;

import java.time.Year;
import java.time.YearMonth;

import static org.junit.Assert.assertEquals;

/**
 * @since 19/05/15
 */
public class MoreJsr310JpaConvertersTest {
    MoreJsr310JpaConverters.YearMonthConverter yearMonthConverter = new MoreJsr310JpaConverters.YearMonthConverter();
    MoreJsr310JpaConverters.YearConverter yearConverter = new MoreJsr310JpaConverters.YearConverter();

    @Test
    public void testYearMonthRoundTrip() {
        YearMonth yearMonth = YearMonth.now();
        final String converted = yearMonthConverter.convertToDatabaseColumn(yearMonth);
        final YearMonth reverted = yearMonthConverter.convertToEntityAttribute(converted);
        assertEquals("Expect the original value back", yearMonth, reverted);
    }

    @Test
    public void testYearRoundTrip() {
        Year year = Year.now();
        final String converted = yearConverter.convertToDatabaseColumn(year);
        final Year reverted = yearConverter.convertToEntityAttribute(converted);
        assertEquals("Expect the original value back", year, reverted);
    }
}