package club.ttg.dnd5.domain.bastion.rest.dto;

import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.filter.rest.FilterParam;
import club.ttg.dnd5.dto.base.filters.AbstractQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BastionFacilityQueryRequest extends AbstractQueryRequest
{
    @FilterParam(enumClass = FacilityCategory.class)
    private QueryFilter<FacilityCategory> category;

    @FilterParam
    private QueryFilter<Long> level;

    @FilterParam(enumClass = BastionOrder.class)
    private QueryFilter<BastionOrder> order;

    @FilterParam(enumClass = FacilitySpace.class)
    private QueryFilter<FacilitySpace> space;

    /** Наличие требования помимо уровня: {@code prerequisite=1} — с требованием, с режимом исключения — без. */
    @FilterParam
    private QueryFilter<String> prerequisite;
}
