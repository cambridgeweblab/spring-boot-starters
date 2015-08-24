package ucles.weblab.common.domain.jpa;

import org.junit.Test;

import java.util.Currency;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @since 30/06/15
 */
public class CurrencyJpaConverterTest {
    MoreCoreJavaJpaConverters.CurrencyConverter currencyConverter = new MoreCoreJavaJpaConverters.CurrencyConverter();

    @Test
    public void testRoundTrip() {
        final Set<Currency> currencies = Currency.getAvailableCurrencies();

        for (Currency currency : currencies) {
            final String converted = currencyConverter.convertToDatabaseColumn(currency);
            final Currency reverted = currencyConverter.convertToEntityAttribute(converted);
            assertEquals("Expect the original value back", currency, reverted);
        }
    }
}
