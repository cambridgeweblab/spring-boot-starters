package ucles.weblab.common.domain.jpa;

import org.springframework.boot.orm.jpa.EntityScan;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Currency;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA 2.1 converter to turn {@link java.net.URI}, {@link java.net.InetAddress} and {@link java.util.Currency} types
 * into {@link java.lang.String Strings} for persistence.
 * To activate these converters, make sure your persistence provider detects it by including this class in the list of
 * mapped classes e.g. with the annotation property {@link EntityScan#basePackages()}.
 * <p>
 * Currencies are persisted using their ISO 4217 currency codes.
 * </p>
 * @since 30/06/15
 */
public class MoreCoreJavaJpaConverters {
    @Converter(autoApply = true)
    public static class UriConverter implements AttributeConverter<URI, String> {
        @Override
        public String convertToDatabaseColumn(URI uri) {
            return uri == null? null : uri.toString();
        }

        @Override
        public URI convertToEntityAttribute(String dbData) {
            return dbData == null? null : URI.create(dbData);
        }
    }

    @Converter(autoApply = true)
    public static class CurrencyConverter implements AttributeConverter<Currency, String> {
        @Override
        public String convertToDatabaseColumn(Currency currency) {
            return currency.getCurrencyCode();
        }

        @Override
        public Currency convertToEntityAttribute(String dbData) {
            return Currency.getInstance(dbData);
        }
    }

    @Converter(autoApply = true)
    public static class InetAddressConverter implements AttributeConverter<InetAddress, String> {
        @Override
        public String convertToDatabaseColumn(InetAddress inetAddress) {
            return inetAddress.getHostAddress();
        }

        @Override
        public InetAddress convertToEntityAttribute(String dbData) {
            try {
                return InetAddress.getByName(dbData);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Unknown value for InetAddress: " + dbData, e);
            }
        }
    }
}
