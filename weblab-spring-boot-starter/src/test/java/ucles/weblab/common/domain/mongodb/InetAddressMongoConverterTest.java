package ucles.weblab.common.domain.mongodb;

import org.junit.Test;
import ucles.weblab.common.domain.jpa.MoreCoreJavaJpaConverters;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

/**
 * @since 30/06/15
 */
public class InetAddressMongoConverterTest {
    MoreCoreJavaMongoConverters.InetAddressToStringConverter inetAddressToStringConverter = MoreCoreJavaMongoConverters.InetAddressToStringConverter.INSTANCE;
    MoreCoreJavaMongoConverters.StringToInetAddressConverter stringToInetAddressConverter = MoreCoreJavaMongoConverters.StringToInetAddressConverter.INSTANCE;

    @Test
    public void testRoundTrip() throws UnknownHostException {
        final InetAddress inetAddress = InetAddress.getLocalHost();

        final String converted = inetAddressToStringConverter.convert(inetAddress);
        final InetAddress reverted = stringToInetAddressConverter.convert(converted);
        assertEquals("Expect the original value back", inetAddress, reverted);
    }
}
