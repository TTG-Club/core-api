package club.ttg.dnd5.dto.base.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class AbstractFilterRange<V, I extends AbstractFilterItem<V>> extends AbstractFilterGroup<V, I> {


    public AbstractFilterRange(List<I> filters) {
        super(filters);
    }

    @Override
    @JsonProperty("range")
    public List<I> getFilters() {
        return super.getFilters();
    }

    @Override
    @JsonProperty("range")
    public void setFilters(List<I> filters) {
        super.setFilters(filters);
    }
}
