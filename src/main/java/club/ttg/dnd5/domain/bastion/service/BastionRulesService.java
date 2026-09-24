package club.ttg.dnd5.domain.bastion.service;

import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.model.BastionRules;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilityPrerequisite;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionLabel;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionRulesResponse;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionRulesResponse.DefensiveWallRule;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionRulesResponse.OrderRule;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionRulesResponse.ProgressionRule;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionRulesResponse.SpaceRule;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

@Service
public class BastionRulesService {
    private static final BastionRulesResponse RULES = new BastionRulesResponse(
            Arrays.stream(FacilityCategory.values())
                    .map(v -> new BastionLabel(v.name(), v.getName()))
                    .toList(),
            Arrays.stream(FacilitySpace.values())
                    .map(v -> new SpaceRule(v.name(), v.getName(), v.getSquares(), v.getBuildCost(), v.getBuildDays(),
                            v.next() == null ? null : Objects.requireNonNull(v.next()).name(), v.getEnlargeCost(), v.getEnlargeDays()))
                    .toList(),
            Arrays.stream(BastionOrder.values())
                    .map(v -> new OrderRule(v.name(), v.getName(), v.getEnglish(), v.getDescription(), v.isBastionWide()))
                    .toList(),
            Arrays.stream(FacilityPrerequisite.values())
                    .map(v -> new BastionLabel(v.name(), v.getName()))
                    .toList(),
            BastionRules.SPECIAL_FACILITY_PROGRESSION.stream()
                    .map(p -> new ProgressionRule(p.level(), p.count()))
                    .toList(),
            BastionRules.STARTING_BASIC_FACILITIES.stream().map(Enum::name).toList(),
            new DefensiveWallRule(BastionRules.DEFENSIVE_WALL_HEIGHT, BastionRules.DEFENSIVE_WALL_COST_PER_SQUARE,
                    BastionRules.DEFENSIVE_WALL_DAYS_PER_SQUARE, BastionRules.DEFENSIVE_WALL_DEFENDER_DICE_REDUCTION));

    public BastionRulesResponse getRules() {
        return RULES;
    }
}
