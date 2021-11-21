package ucles.weblab.common.webapi;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.server.core.LinkBuilderSupport;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Simple link builder which allows one to build links to absolute paths.
 *
 * @since 05/11/15
 */
public class StaticLinkBuilder extends LinkBuilderSupport<StaticLinkBuilder> {

    public StaticLinkBuilder(UriComponentsBuilder builder) {
        super(builder.build());
    }

    public StaticLinkBuilder(UriComponents components, List<Affordance> affordances) {
        super(components, affordances);
    }

    @Override
    protected StaticLinkBuilder getThis() {
        return this;
    }

    @Override
    protected StaticLinkBuilder createNewInstance(UriComponents components, List<Affordance> affordances) {
        return new StaticLinkBuilder(components, affordances);
    }
}

