package club.ttg.dnd5.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

/**
 * Кубы, они же дайсы
 */
@Getter
@AllArgsConstructor
public enum Dice {
	d4(4),
	d6(6),
	d8(8),
	d10(10),
	d12(12),
	d20(20),
	d100(100),
	d3(3),
	d2(2);

	private static final Random rnd = new Random();

	private final int maxValue;
	
	public int roll() {
		return 1 + rnd.nextInt(this.getMaxValue());
	}
	
	public int roll(int diceCount) {
		int result = 0;
		for (int i = 0; i < diceCount; i++) {
			result += roll();
		}
		return result;
	}
	
	public static Dice parse(int dice) {
		return switch (dice) {
			case 4 -> d4;
			case 6 -> d6;
			case 8 -> d8;
			case 10 -> d10;
			case 12 -> d12;
			case 20 -> d20;
			case 100 -> d100;
			default -> null;
		};
	}
	
	public static Dice parse(String dice) {
		if (dice == null) {
			return null;
		}
		var diceEng = dice.replace("к", "d");
		return switch (diceEng) {
			case "d4" -> d4;
			case "d6" -> d6;
			case "d8" -> d8;
			case "d10" -> d10;
			case "d12" -> d12;
			case "d20" -> d20;
			case "d100" -> d100;
			default -> null;
		};
	}
	
	public String getName() {
		return "к" + maxValue;
	}

	public static Set<Dice> getCreatures() {
		return EnumSet.of(d4, d6, d8, d10, d12, d20);
	}

	public static int roll(Dice dice) {
		return 1 + rnd.nextInt(dice.maxValue);
	}

	public static int roll(int countDice, Dice dice) {
		int result = 0;
		for (int i = 0; i < countDice; i++) {
			result += roll(dice);
		}
		return result;
	}
}