package club.ttg.dnd5.domain.common.rest.dto.container;

import club.ttg.dnd5.domain.common.rest.dto.select.SelectOptionDto;
import club.ttg.dnd5.domain.filter.model.FilterInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class PageMetadata {
    private Collection<SelectOptionDto> group;
    private Collection<SelectOptionDto> sort;
    private FilterInfo filter;
}
