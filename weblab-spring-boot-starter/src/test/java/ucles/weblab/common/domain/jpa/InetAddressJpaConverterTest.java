package ucles.weblab.common.domain.jpa;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

/**
 * @since 30/06/15
 */
public class InetAddressJpaConverterTest {
    MoreCoreJavaJpaConverters.InetAddressConverter inetAddressConverter = new MoreCoreJavaJpaConverters.InetAddressConverter();

    @Test
    public void testRoundTrip() throws UnknownHostException {
        final InetAddress inetAddress = InetAddress.getLocalHost();

        final String converted = inetAddressConverter.convertToDatabaseColumn(inetAddress);
        final InetAddress reverted = inetAddressConverter.convertToEntityAttribute(converted);
        assertEquals("Expect the original value back", inetAddress, reverted);
    }
}
