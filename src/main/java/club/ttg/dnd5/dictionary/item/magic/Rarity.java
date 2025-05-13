package club.ttg.dnd5.dictionary.item.magic;

import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Rarity {
	VARIES(null, "редкость варьируется", "редкость варьируется", "редкость варьируется"),
	COMMON(100, "обычный", "обычная", "обычное"),
	UNCOMMON(400, "необычный", "необычная", "необычное"),
	RARE(4000, "редкий", "редкая", "редкое"),
	VERY_RARE(40_000, "очень редкий", "очень редкая", "очень редкое" ),
	LEGENDARY(200_000, "легендарный", "легендарная", "легендарное" ),
	ARTIFACT(null, "артефакт", "артефакт", "артефакт"),
	UNKNOWN(null, "редкость не определена", "редкость не определена", "редкость не определена"),
	;

	Rarity(Integer cost, String... names){
		baseCost = cost;
		this.names = names;
	}

	/**
	 * Цена в золотых монетах
 	 */
	private final Integer baseCost;
	private final String[] names;

	public static Rarity parse(String value) {
		return Arrays.stream(values())
				.filter(f -> f.getName().equals(value))
				.findFirst()
				.orElseThrow(IllegalArgumentException::new);
	}

	public String getName() {
		return names[0];
	}
	public String getName(MagicItemCategory category) {
		return switch (category) {
			case WAND -> names[1];
			case WEAPON, POTION, RING -> names[2];
			default -> names[0];
		};
	}
}
