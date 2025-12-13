package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Activite {
	private String titre;
	private TypeActivite type;
	private int duree;
	private int priorite;
	private LocalDateTime deadline;
	private LocalDateTime horaireDebut;
	private LocalDateTime horaireFin;
	List<Activite> listeActivites = new ArrayList<>();


	public Activite(String titre, TypeActivite type, int duree, int priorite, LocalDateTime deadline,
			LocalDateTime horaireDebut, LocalDateTime horaireFin) {
		super();
		this.titre = titre;
		this.type = type;
		this.duree = duree;
		this.priorite = priorite;
		this.deadline = deadline;
		this.horaireDebut = horaireDebut;
		this.horaireFin = horaireFin;
	}



	public String getTitre() {
		return titre;
	}


	public void setTitre(String titre) {
		this.titre = titre;
	}


	public TypeActivite getType() {
		return type;
	}


	public void setType(TypeActivite type) {
		this.type = type;
	}


	public int getDuree() {
		return duree;
	}


	public void setDuree(int duree) {
		this.duree = duree;
	}


	public int getPriorite() {
		return priorite;
	}


	public void setPriorite(int priorite) {
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
	@Override
	public String toString() {
		return "Activite [titre=" + titre + ", type=" + type + ", duree=" + duree + ", priorite=" + priorite
				+ ", deadline=" + deadline + ", horaireDebut=" + horaireDebut + ", horaireFin=" + horaireFin + "]";
	}
	public boolean ajouterActivite(String titre, TypeActivite type, int duree, int priorite,
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

			boolean chevauchement =
					horaireDebut.isBefore(a.getHoraireFin()) &&
					horaireFin.isAfter(a.getHoraireDebut());

			if (chevauchement) {
				System.out.println("Conflit détecté avec l’activité : " + a.getTitre());
				return false; // ❌ Chevauchement
}
}

		// ✅ 3. Si tout est bon → on ajoute l’activité
		this.titre = titre;
		this.type = type;
		this.duree = duree;
		this.priorite = priorite;
		this.deadline = deadline;
		this.horaireDebut = horaireDebut;
		this.horaireFin = horaireFin;

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

			if (a == activiteAModifier) continue;

			boolean chevauchement =
					nouveauHoraireDebut.isBefore(a.getHoraireFin()) &&
					nouveauHoraireFin.isAfter(a.getHoraireDebut());

			if (chevauchement) {
				System.out.println("Conflit avec l’activité : " + a.getTitre());
				return false;
}
}

		// ✅ 4. Application de TOUTES les modifications
		activiteAModifier.setTitre(nouveauTitre);
		activiteAModifier.setType(nouveauType);
		activiteAModifier.setDuree(nouvelleDuree);
		activiteAModifier.setPriorite(nouvellePriorite);
		activiteAModifier.setDeadline(nouveauDeadline);
		activiteAModifier.setHoraireDebut(nouveauHoraireDebut);
		activiteAModifier.setHoraireFin(nouveauHoraireFin);

		return true; // ✅ Modification réussie
}



}