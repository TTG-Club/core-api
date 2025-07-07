package club.ttg.dnd5.domain.common.dictionary;

import lombok.Getter;

@Getter
public enum Alignment {
	LAWFUL_GOOD("ЗД", "законопослушный добрый", "законопослушная добрая", "законопослушное доброе"),
	LAWFUL_NEUTRAL("ЗН", "законопослушный нейтральный", "законопослушная нейтральная", "законопослушное нейтральное"),
	LAWFUL_EVIL("ЗЗ", "законопослушный злой","законопослушная злая", "законопослушное злое"),
	TRUE_NEUTRAL("Н", "нейтральный", "нейтральная","нейтральное"),
	NEUTRAL_GOOD("НД", "нейтральный добрый", "нейтральная добрая", "нейтральное доброе"),
	NEUTRAL_EVIL("НЗ", "нейтральный злой", "нейтральная злая", "нейтральное злое"),
	CHAOTIC_GOOD("ХД", "хаотичный добрый", "хаотичная добрая","хаотичное доброе"),
	CHAOTIC_NEUTRAL("ХН", "хаотично нейтральный",  "хаотичная нейтральная",  "хаотичное нейтральное"),
	CHAOTIC_EVIL("ХЗ", "хаотичный злой", "хаотично злая", "хаотично злое"),
	NEUTRAL("Н", "нейтральный",  "нейтральная", "нейтральное"),
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

	public String getName(CreatureType type) {
		return switch (type) {
			case ABERRATION, FEY, UNDEAD, SLIME, MONSTROSITY -> names[1];
			case FIEND, PLANT -> names[2];
			default -> names[0];
		};
	}
}