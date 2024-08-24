package club.ttg.dnd5.model.character;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "class_feature_Levels")
public class ClassFeatureLevels {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;

	private String prefix;
	private String suffix;

	private Byte l1;
	private Byte l2;
	private Byte l3;
	private Byte l4;
	private Byte l5;
	private Byte l6;
	private Byte l7;
	private Byte l8;
	private Byte l9;
	private Byte l10;
	private Byte l11;
	private Byte l12;
	private Byte l13;
	private Byte l14;
	private Byte l15;
	private Byte l16;
	private Byte l17;
	private Byte l18;
	private Byte l19;
	private Byte l20;
}