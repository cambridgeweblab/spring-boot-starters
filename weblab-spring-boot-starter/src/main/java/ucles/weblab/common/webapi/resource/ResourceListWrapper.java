package ucles.weblab.common.webapi.resource;

import org.springframework.hateoas.ResourceSupport;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * A wrapper around a list of resources which allows links to be added.
 *
 * @since 27/07/15
 */
public class ResourceListWrapper<T> extends ResourceSupport {
    private final List<T> list;

    ResourceListWrapper(List<T> list) {
        this.list = list;
    }

    public List<T> getList() {
        return list;
    }

    public static <T> ResourceListWrapper<T> wrap(List<T> list) {
        return new ResourceListWrapper<>(list);
    }

    public static <T> Collector<T, ?, ResourceListWrapper<T>> toResourceList() {
        return Collectors.collectingAndThen(Collectors.<T>toList(), ResourceListWrapper::new);
    }
}
