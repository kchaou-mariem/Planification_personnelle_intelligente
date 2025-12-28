package entities;

/**
 * Énumération des types d'activités disponibles
 */
public enum TypeActivite {
	/**
	 * Activités de sport et fitness
	 */
	Sport("Sport"),

	/**
	 * Activités d'étude et d'apprentissage
	 */
	Etude("Étude"),

	/**
	 * Activités de loisirs et de divertissement
	 */
	Loisirs("Loisirs"),

	/**
	 * Activités de repos et relaxation
	 */
	Repos("Repos"),

	/**
	 * Activités de travail professionnel
	 */
	Travail("Travail");

	private final String label;

	/**
	 * Constructeur de l'énumération
	 * 
	 * @param label Le label d'affichage
	 */
	TypeActivite(String label) {
		this.label = label;
	}

	/**
	 * Obtenir le label d'affichage
	 * 
	 * @return Le label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Obtenir le type à partir du label
	 * 
	 * @param label Le label de recherche
	 * @return Le TypeActivite correspondant
	 */
	public static TypeActivite fromLabel(String label) {
		for (TypeActivite type : values()) {
			if (type.label.equalsIgnoreCase(label)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Type d'activité inconnu: " + label);
	}

}
