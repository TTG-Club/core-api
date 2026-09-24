package club.ttg.dnd5.domain.bastion.service;

import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.model.QBastionFacility;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityQueryRequest;
import club.ttg.dnd5.dto.base.filters.PredicateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BastionFacilityPredicateBuilder
{
    private static final QBastionFacility Q = QBastionFacility.bastionFacility;
    private static final StringPath CATEGORY_PATH = Expressions.stringPath("category");
    private static final StringPath SPACE_PATH = Expressions.stringPath("space");

    public BooleanBuilder build(final BastionFacilityQueryRequest request)
    {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(Q.isHiddenEntity.isFalse());
        builder.and(PredicateUtils.buildTextSearch(request.getSearch(), Q.name, Q.english, Q.alternative));
        PredicateUtils.applyFilterEnum(builder, request.getCategory(), CATEGORY_PATH, FacilityCategory.class);
        PredicateUtils.applyFilter(builder, request.getLevel(), Q.level);
        PredicateUtils.applyJsonbEnumArrayFilter(builder, request.getOrder(), "orders");
        PredicateUtils.applyFilterEnum(builder, request.getSpace(), SPACE_PATH, FacilitySpace.class);
        if (request.getPrerequisite() != null && request.getPrerequisite().isActive())
        {
            builder.and(request.getPrerequisite().isExclude()
                    ? Q.prerequisite.isNull()
                    : Q.prerequisite.isNotNull());
        }
        PredicateUtils.applySourcesFilter(builder, request.getSource(), "bastion_facility", "source");
        PredicateUtils.applyStringFilter(builder, request.getSrdVersion(), Q.srdVersion);
        return builder;
    }
}
