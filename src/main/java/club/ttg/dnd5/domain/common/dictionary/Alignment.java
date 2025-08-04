package club.ttg.dnd5.domain.common.dictionary;

import lombok.Getter;

@Getter
public enum Alignment {
	LAWFUL_GOOD("ЗД", "Законопослушный Добрый", "Законопослушная Добрая", "Законопослушное Доброе"),
	LAWFUL_NEUTRAL("ЗН", "Законопослушный Нейтральный", "Законопослушная Нейтральная", "Законопослушное Нейтральное"),
	LAWFUL_EVIL("ЗЗ", "Законопослушный Злой", "Законопослушная Злая", "Законопослушное Злое"),
	TRUE_NEUTRAL("Н", "Нейтральный", "Нейтральная", "Нейтральное"),
	NEUTRAL_GOOD("НД", "Нейтральный Добрый", "Нейтральная Добрая", "Нейтральное Доброе"),
	NEUTRAL_EVIL("НЗ", "Нейтральный Злой", "Нейтральная Злая", "Нейтральное Злое"),
	CHAOTIC_GOOD("ХД", "Хаотичный Добрый", "Хаотичная Добрая", "Хаотичное Доброе"),
	CHAOTIC_NEUTRAL("ХН", "Хаотично Нейтральный", "Хаотичная Нейтральная", "Хаотичное Нейтральное"),
	CHAOTIC_EVIL("ХЗ", "Хаотичный Злой", "Хаотично Злая", "Хаотично Злое"),
	NEUTRAL("Н", "Нейтральный", "Нейтральная", "Нейтральное"),
	WITHOUT("", "Без Мировоззрения", "Без Мировоззрения", "Без Мировоззрения");

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