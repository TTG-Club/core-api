package club.ttg.dnd5.dictionary.item.magic;

import lombok.Getter;

import java.util.Arrays;
import java.util.Random;

@Getter
public enum Rarity {
	COMMON(100, "обычный", "обычная", "обычное"),
	UNCOMMON(400, "необычный", "необычная", "необычное"),
	RARE(4000, "редкий", "редкая", "редкое"),
	VERY_RARE(40_000, "очень редкий", "очень редкая", "очень редкое" ),
	LEGENDARY(200_000, "легендарный", "легендарная", "легендарное" ),
	ARTIFACT(1_500_000, "артефакт", "артефакт", "артефакт"),

	UNKNOWN(0, "редкость не определена", "редкость не определена", "редкость не определена"),
	VARIES(0, "редкость варьируется", "редкость варьируется", "редкость варьируется");

	private static final Random RND = new Random();

	private final String[] names;

	Rarity(int cost, String... names){
		baseCost = cost;
		this.names = names;
	}
	// базовая цена в золотых монетах
	private final int baseCost;

	public static Rarity parse(String value) {
		return Arrays.stream(values()).filter(f -> f.getName().equals(value)).findFirst().orElseThrow(IllegalArgumentException::new);
	}

	public String getName() {
		return names[0];
	}

	public String getFemaleName() {
		return names[1];
	}

	public String getMiddleName() {
		return names[2];
	}
}
