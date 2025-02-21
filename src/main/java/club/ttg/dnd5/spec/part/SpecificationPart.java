package club.ttg.dnd5.spec.part;

import club.ttg.dnd5.dto.engine.SearchRequest;
import org.springframework.data.jpa.domain.Specification;

/**
 * A marker interface representing a part of a {@link Specification}.
 * <p>
 * Implementations of this interface define a contract that allows adding
 * specific parts to a {@link Specification}. Each part can be converted
 * into a {@link Specification} based on the provided {@link SearchRequest}.
 *
 * @param <T> the type of object that the {@link Specification} will operate on
 *
 * <p>This interface is used to build or extend specifications dynamically by
 * allowing modular parts to be added.</p>
 *
 * <p>For example, an implementation might convert a search request into a
 * query filter, a sort condition, or other criteria, and then append
 * it to an existing specification.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * SpecificationPart<User> userFilter = new UserFilter();
 * Specification<User> userSpecification = userFilter.toSpecification(request);
 * }</pre>
 *
 * @see Specification
 * @see SearchRequest
 */
public interface SpecificationPart<T> {

    /**
     * Converts the current part into a {@link Specification} based on the provided {@link SearchRequest}.
     * <p>
     * This method defines how a specific part of a search or filter criteria is transformed into
     * a {@link Specification} that can be added to a larger, composite specification.
     *
     * @param request the search request containing parameters for constructing the specification
     * @return a {@link Specification} representing this part of the search/filter criteria
     */
    Specification<T> toSpecification(SearchRequest request);
}