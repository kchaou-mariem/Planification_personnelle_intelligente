package entities;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Contrainte {
	private Long id; 
	private String titre;
	private TypeContrainte type;
	private LocalTime dateHeureDeb;
	private LocalTime dateHeureFin;
	private boolean repetitif;
	/**
	 * Dates spécifiques où la contrainte s'applique (ex: 2025-12-01, 2025-12-03)
	 */
	private List<LocalDate> datesSpecifiques = new ArrayList<>();
	/**
	 * Jours de semaine pour répétition hebdomadaire (ex: MONDAY, WEDNESDAY)
	 */
	private List<DayOfWeek> joursSemaine = new ArrayList<>();
	private Long utilisateurId; // chaque contrainte appartient à un seul utilisateur
	private StatutContrainte statut = StatutContrainte.ACTIVE; // par défaut ACTIVE
	
	// Placeholder: la classe Utilisateur n'est pas encore créée
	// private Utilisateur proprietaire; // à décommenter lorsque Utilisateur existera
	
	private static Long compteurId = 1L; // identifiants simples auto-incr
	 
	public Contrainte() {
		super();
		this.id = compteurId;
		compteurId++;

	}
	
	/**
	 * Constructeur principal pour une contrainte ; les heures sont fournies via LocalTime.
	 */
	public Contrainte(String titre, TypeContrainte type, LocalTime dateHeureDeb, LocalTime dateHeureFin,
				boolean repetitif, List<LocalDate> datesSpecifiques, List<DayOfWeek> joursSemaine) {
		super();
		this.id = compteurId;
		this.titre = titre;
		this.type = type;
		this.dateHeureDeb = dateHeureDeb;
		this.dateHeureFin = dateHeureFin;
		this.repetitif = repetitif;
		if (datesSpecifiques != null) this.datesSpecifiques = datesSpecifiques;
		if (joursSemaine != null) this.joursSemaine = joursSemaine;
		compteurId++;
	}

	public String getTitre() {
		return titre;
	}		
	public void setTitre(String titre) {
		this.titre = titre;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public TypeContrainte getType() {
		return type;
	}
	public void setType(TypeContrainte type) {
		this.type = type;
	}
	public LocalTime getDateHeureDeb() {
		return dateHeureDeb;
	}
	public void setDateHeureDeb(LocalTime dateHeureDeb) {
		this.dateHeureDeb = dateHeureDeb;
	}
	public LocalTime getDateHeureFin() {
		return dateHeureFin;
	}
	public void setDateHeureFin(LocalTime dateHeureFin) {
		this.dateHeureFin = dateHeureFin;
	}
	public boolean isRepetitif() {
		return repetitif;
	}
	public void setRepetitif(boolean repetitif) {
		this.repetitif = repetitif;
	}
	public List<LocalDate> getDatesSpecifiques() {
		return datesSpecifiques;
	}
	public void setDatesSpecifiques(List<LocalDate> datesSpecifiques) {
		this.datesSpecifiques = datesSpecifiques;
	}
	public List<DayOfWeek> getJoursSemaine() {
		return joursSemaine;
	}
	public void setJoursSemaine(List<DayOfWeek> joursSemaine) {
		this.joursSemaine = joursSemaine;
	}

	public Long getUtilisateurId() {
		return utilisateurId;
	}

	public void setUtilisateurId(Long utilisateurId) {
		this.utilisateurId = utilisateurId;
	}

	public StatutContrainte getStatut() {
		return statut;
	}

	public void setStatut(StatutContrainte statut) {
		this.statut = statut;
	}

	@Override
	public String toString() {
		return "Contrainte [id=" + id + ", titre=" + titre + ", type=" + type + ", dateHeureDeb=" + dateHeureDeb + ", dateHeureFin=" + dateHeureFin
			+ ", repetitif=" + repetitif + ", datesSpecifiques=" + datesSpecifiques + ", joursSemaine=" + joursSemaine + ", statut=" + statut + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contrainte other = (Contrainte) obj;
		return Objects.equals(id, other.id);
	}

}