package ucles.weblab.common.domain.jpa;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

/**
 * @since 30/06/15
 */
public class UriJpaConverterTest {
    MoreCoreJavaJpaConverters.UriConverter uriConverter = new MoreCoreJavaJpaConverters.UriConverter();

    @Test
    public void testRoundTrip() {
        final URI uri = URI.create("http://ca-uat-certstat.cfapps.io/api/files/fs.98ade6cf82c74ffc9e9b38655fcd67a5/");
        final String converted = uriConverter.convertToDatabaseColumn(uri);
        final URI reverted = uriConverter.convertToEntityAttribute(converted);
        assertEquals("Expect the original value back", uri, reverted);
    }
}
