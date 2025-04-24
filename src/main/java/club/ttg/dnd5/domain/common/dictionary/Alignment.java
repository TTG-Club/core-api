package club.ttg.dnd5.domain.common.dictionary;

import club.ttg.dnd5.domain.beastiary.model.BeastType;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum Alignment {
	LAWFUL_GOOD("ЗД", "законно-добрый", "законно-добрая", "законно-доброе"), //0 
	LAWFUL_NEUTRAL("ЗН", "законно-нейтральный", "законно-нейтральная", "законно-нейтральное"), // 1
	LAWFUL_EVIL("ЗЗ", "законно-злой","законно-злая", "законно-злое"), //2 
	TRUE_NEUTRAL("Н", "нейтральный", "нейтральная","нейтральное"), //3
	NEUTRAL_GOOD("НД", "нейтрально-добрый", "нейтрально-добрая", "нейтрально-доброе"), // 4
	NEUTRAL_EVIL("НЗ", "нейтрально-злой", "нейтрально-злая", "нейтрально-злое"), //5 
	CHAOTIC_GOOD("ХД", "хаотично-добрый", "хаотично-добрая","хаотично-доброе"), //6
	CHAOTIC_NEUTRAL("ХН", "хаотично-нейтральный",  "хаотично-нейтральная",  "хаотично-нейтральное"), //7
	CHAOTIC_EVIL("ХЗ", "хаотично-злой", "хаотично-злая", "хаотично-злое"), // 8
	NEUTRAL("Н", "нейтральный",  "нейтральная", "нейтральное"), //9
	WITHOUT("", "без мировоззрения", "без мировоззрения", "без мировоззрения");

	private final String shortName;
	private final String[] names;
	
	Alignment(String shortName, String... names){
		this.shortName = shortName;
		this.names = names;
	}

	public String getName() {
		return names[0];
	}

	public String getName(BeastType type) {
		return switch (type) {
			case ABERRATION, FEY, UNDEAD, SLIME, MONSTROSITY -> names[1];
			case FIEND, PLANT -> names[2];
			default -> names[0];
		};
	}
}