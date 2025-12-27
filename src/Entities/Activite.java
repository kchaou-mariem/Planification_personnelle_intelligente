package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Activite {
	private Long idActivite;
	private String titre;
	private TypeActivite typeActivite;
	private String description;
	private Integer priorite;
	private LocalDateTime deadline;
	private LocalDateTime horaireDebut;
	private LocalDateTime horaireFin;
	private Long idUtilisateur;
	private LocalDateTime dateCreation;
	List<Activite> listeActivites = new ArrayList<>();

	// Constructeur complet avec ID
	public Activite(Long idActivite, String titre, TypeActivite typeActivite, String description,
			Integer priorite, LocalDateTime deadline, LocalDateTime horaireDebut, LocalDateTime horaireFin,
			Long idUtilisateur, LocalDateTime dateCreation) {
		super();
		this.idActivite = idActivite;
		this.titre = titre;
		this.typeActivite = typeActivite;
		this.description = description;
		this.priorite = priorite;
		this.deadline = deadline;
		this.horaireDebut = horaireDebut;
		this.horaireFin = horaireFin;
		this.idUtilisateur = idUtilisateur;
		this.dateCreation = dateCreation;
	}

	// Constructeur sans ID (pour création)
	public Activite(String titre, TypeActivite typeActivite, String description, Integer priorite,
			LocalDateTime deadline, LocalDateTime horaireDebut, LocalDateTime horaireFin, Long idUtilisateur) {
		super();
		this.titre = titre;
		this.typeActivite = typeActivite;
		this.description = description;
		this.priorite = priorite;
		this.deadline = deadline;
		this.horaireDebut = horaireDebut;
		this.horaireFin = horaireFin;
		this.idUtilisateur = idUtilisateur;
		this.dateCreation = LocalDateTime.now();
	}

	public Activite() {
		super();
		this.dateCreation = LocalDateTime.now();
	}

	public Long getIdActivite() {
		return idActivite;
	}

	public void setIdActivite(Long idActivite) {
		this.idActivite = idActivite;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TypeActivite getTypeActivite() {
		return typeActivite;
	}

	public void setTypeActivite(TypeActivite typeActivite) {
		this.typeActivite = typeActivite;
	}

	public Integer getPriorite() {
		return priorite;
	}

	public void setPriorite(Integer priorite) {
		this.priorite = priorite;
	}

	public LocalDateTime getDeadline() {
		return deadline;
	}

	public void setDeadline(LocalDateTime deadline) {
		this.deadline = deadline;
	}

	public LocalDateTime getHoraireDebut() {
		return horaireDebut;
	}

	public void setHoraireDebut(LocalDateTime horaireDebut) {
		this.horaireDebut = horaireDebut;
	}

	public LocalDateTime getHoraireFin() {
		return horaireFin;
	}

	public void setHoraireFin(LocalDateTime horaireFin) {
		this.horaireFin = horaireFin;
	}

	public Long getIdUtilisateur() {
		return idUtilisateur;
	}

	public void setIdUtilisateur(Long idUtilisateur) {
		this.idUtilisateur = idUtilisateur;
	}

	public LocalDateTime getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(LocalDateTime dateCreation) {
		this.dateCreation = dateCreation;
	}

	@Override
	public String toString() {
		return "Activite [idActivite=" + idActivite + ", titre=" + titre + ", typeActivite=" + typeActivite
				+ ", description=" + description + ", priorite=" + priorite
				+ ", deadline=" + deadline + ", horaireDebut=" + horaireDebut + ", horaireFin=" + horaireFin
				+ ", idUtilisateur=" + idUtilisateur + ", dateCreation=" + dateCreation + "]";
	}

	public boolean ajouterActivite(String titre, TypeActivite typeActivite, Integer priorite,
			LocalDateTime deadline, LocalDateTime horaireDebut,
			LocalDateTime horaireFin,
			List<Activite> activitesExistantes) {

		// ✅ 1. Vérification logique des horaires
		if (horaireDebut.isAfter(horaireFin) || horaireDebut.isEqual(horaireFin)) {
			System.out.println("Erreur : L'horaire de début doit être avant l'horaire de fin.");
			return false;
		}

		// ✅ 2. Vérification de chevauchement avec les autres activités
		for (Activite a : activitesExistantes) {
			boolean chevauchement = horaireDebut.isBefore(a.getHoraireFin()) &&
					horaireFin.isAfter(a.getHoraireDebut());

			if (chevauchement) {
				return false; // ❌ Chevauchement
			}
		}

		// ✅ 3. Si tout est bon → on ajoute l'activité
		this.titre = titre;
		this.typeActivite = typeActivite;
		this.priorite = priorite;
		this.deadline = deadline;
		this.horaireDebut = horaireDebut;
		this.horaireFin = horaireFin;
		this.dateCreation = LocalDateTime.now();

		return true;
	}

	public static boolean supprimerActivite(String titre, List<Activite> activites) {

		for (int i = 0; i < activites.size(); i++) {
			if (activites.get(i).getTitre().equalsIgnoreCase(titre)) {
				activites.remove(i);
				return true; // ✅ suppression réussie
			}
		}

		return false; // ❌ activité non trouvée
	}

	public static boolean modifierActivite(String ancienTitre,
			String nouveauTitre,
			TypeActivite nouveauType,
			int nouvelleDuree,
			int nouvellePriorite,
			LocalDateTime nouveauDeadline,
			LocalDateTime nouveauHoraireDebut,
			LocalDateTime nouveauHoraireFin,
			List<Activite> activites) {

		// ✅ 1. Vérification logique des horaires
		if (nouveauHoraireDebut.isAfter(nouveauHoraireFin)
				|| nouveauHoraireDebut.isEqual(nouveauHoraireFin)) {
			System.out.println("Erreur : horaire de début invalide.");
			return false;
		}

		// ✅ 2. Recherche de l’activité à modifier
		Activite activiteAModifier = null;
		for (Activite a : activites) {
			if (a.getTitre().equalsIgnoreCase(ancienTitre)) {
				activiteAModifier = a;
				break;
			}
		}

		if (activiteAModifier == null) {
			System.out.println("Activité non trouvée.");
			return false;
		}

		// ✅ 3. Vérification de chevauchement (en ignorant l’activité courante)

		for (Activite a : activites) {

			if (a == activiteAModifier)
				continue;

			boolean chevauchement = nouveauHoraireDebut.isBefore(a.getHoraireFin()) &&
					nouveauHoraireFin.isAfter(a.getHoraireDebut());

			if (chevauchement) {

				System.out.println("Conflit avec l'activité : " + a.getTitre());
				return false;
			}
		}

		// ✅ 4. Application de TOUTES les modifications
		activiteAModifier.setTitre(nouveauTitre);

		activiteAModifier.setPriorite(nouvellePriorite);
		activiteAModifier.setDeadline(nouveauDeadline);
		activiteAModifier.setHoraireDebut(nouveauHoraireDebut);
		activiteAModifier.setHoraireFin(nouveauHoraireFin);

		return true; // ✅ Modification réussie
	}

}