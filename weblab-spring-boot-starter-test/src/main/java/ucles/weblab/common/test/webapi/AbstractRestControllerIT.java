package ucles.weblab.common.test.webapi;

import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Base class for controller tests to assist with JSON formatting and other common tasks.
 *
 * @since 25/06/15
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class AbstractRestControllerIT {
    protected static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8);

    protected MockMvc mockMvc;
    protected HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected HttpMessageConverter<?>[] converters;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    /**
     * Given any absolute URL to a resource in this application, derive the absolute context root URI.
     *
     * @param absoluteUrl an absolute URL to a resource
     * @return the context root URI
     */
    protected URI getContextRoot(String absoluteUrl) {
        return UriComponentsBuilder.fromHttpUrl(absoluteUrl)
                .replacePath(webApplicationContext.getServletContext().getContextPath())
                .replaceQuery(null)
                .fragment(null)
                .build().toUri();
    }

    /**
     * Given any URI returned as a link and a context root, get a relative URI.
     *
     * @param linkUri a link URI
     * @param contextRootUri the context root URI (e.g. from {@link #getContextRoot(String)} above).
     * @return the relative URI
     */
    protected URI toContextRelativeUri(URI linkUri, URI contextRootUri) {
        return URI.create("/").resolve(contextRootUri.relativize(linkUri));
    }

    /** Format an object as JSON. */
    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        //noinspection unchecked
        this.mappingJackson2HttpMessageConverter.write(o, APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

}
