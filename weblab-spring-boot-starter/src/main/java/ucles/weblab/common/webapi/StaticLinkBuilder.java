package ucles.weblab.common.webapi;

import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Simple link builder which allows one to build links to absolute paths.
 *
 * @since 05/11/15
 */
public class StaticLinkBuilder extends LinkBuilderSupport<StaticLinkBuilder> {
    public StaticLinkBuilder(UriComponentsBuilder builder) {
        super(builder);
    }

    @Override
    protected StaticLinkBuilder getThis() {
        return this;
    }

    @Override
    protected StaticLinkBuilder createNewInstance(UriComponentsBuilder builder) {
        return new StaticLinkBuilder(builder);
    }
}

