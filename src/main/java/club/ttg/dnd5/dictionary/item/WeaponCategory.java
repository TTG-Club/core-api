package club.ttg.dnd5.dictionary.item;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WeaponCategory {
	SIMPLE_MELEE("Простое рукопашное"),
	SIMPLE_RANGED("Простое дальнобойное"),
	MATERIAL_MELEE("Воинское рукопашное"),
	MATERIAL_RANGED("Воинское дальнобойное"),
	FIREARM("Современное воинское огнестрельное оружие"),
	FUTURISTIC("Футуристичное воинское огнестрельное оружие");

	private final String name;
}