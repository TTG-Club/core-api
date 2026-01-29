package club.ttg.dnd5.domain.common.rest.dto.container;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Builder
@Getter
@Setter
public class ContainerResponse <T> {
    private MetadataResponse metadata;
    private Collection<OrderResponse<T>> data;
}
