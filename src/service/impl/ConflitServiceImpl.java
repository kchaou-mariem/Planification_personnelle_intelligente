package service.impl;

import dao.impl.ActiviteDAOImpl;
import dao.impl.ConflitDAOImpl;
import dao.interfaces.ActiviteDAO;
import dao.interfaces.ConflitDAO;
import entities.Activite;
import entities.Conflit;
import entities.TypeConflit;
import service.ConflitService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implémentation du service de gestion des conflits.
 * Stratégie de résolution automatique (simple) des chevauchements:
 * - Les activités sont triées par horaire de début
 * - En cas de chevauchement, on décale l'activité de plus faible priorité
 *   à l'heure de fin de l'activité la plus prioritaire (en conservant la durée)
 * - Si cela provoque un nouveau chevauchement, la résolution est abandonnée pour ce cas
 */
public class ConflitServiceImpl implements ConflitService {

	private final ActiviteDAO activiteDAO;
	private final ConflitDAO conflitDAO;

	public ConflitServiceImpl() {
		this.activiteDAO = new ActiviteDAOImpl();
		this.conflitDAO = new ConflitDAOImpl();
	}

	@Override
	public List<Conflit> detecterChevauchementsUtilisateur(Long idUtilisateur) {
		List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);
		activites.sort(Comparator.comparing(Activite::getHoraireDebut));

		List<Conflit> conflits = new ArrayList<>();
		for (int i = 0; i < activites.size(); i++) {
			for (int j = i + 1; j < activites.size(); j++) {
				Activite a = activites.get(i);
				Activite b = activites.get(j);
				boolean chevauche = a.getHoraireDebut().isBefore(b.getHoraireFin())
						&& a.getHoraireFin().isAfter(b.getHoraireDebut());
				if (chevauche) {
					Conflit conflit = new Conflit(
							null,
							LocalDateTime.now(),
							TypeConflit.CHEVAUCHEMENT_DES_ACTIVITES,
							false
					);
					Long id = conflitDAO.ajouter(conflit);
					if (id != null && id > 0) {
						conflit.setidConflit(id);
						conflits.add(conflit);
					}
				}
			}
		}
		return conflits;
	}

	@Override
	public int resoudreChevauchementsUtilisateur(Long idUtilisateur) {
		List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);
		activites.sort(Comparator.comparing(Activite::getHoraireDebut));

		int resolus = 0;
		for (int i = 0; i < activites.size(); i++) {
			for (int j = i + 1; j < activites.size(); j++) {
				Activite a = activites.get(i);
				Activite b = activites.get(j);

				boolean chevauche = a.getHoraireDebut().isBefore(b.getHoraireFin())
						&& a.getHoraireFin().isAfter(b.getHoraireDebut());
				if (!chevauche) {
					continue;
				}

				Activite prioritaire = a.getPriorite() >= b.getPriorite() ? a : b;
				Activite aDecaler = prioritaire == a ? b : a;

				LocalDateTime nouveauDebut = prioritaire.getHoraireFin();
				LocalDateTime nouveauFin = nouveauDebut.plusMinutes(aDecaler.getDuree());

				// Vérifier que le déplacement ne viole pas la deadline (si définie)
				if (aDecaler.getDeadline() != null && nouveauFin.isAfter(aDecaler.getDeadline())) {
					// Essayer l'autre option: placer avant l'activité prioritaire
					nouveauFin = prioritaire.getHoraireDebut();
					nouveauDebut = nouveauFin.minusMinutes(aDecaler.getDuree());
				}

				// Appliquer en mémoire
				LocalDateTime ancienDebut = aDecaler.getHoraireDebut();
				LocalDateTime ancienFin = aDecaler.getHoraireFin();
				aDecaler.setHoraireDebut(nouveauDebut);
				aDecaler.setHoraireFin(nouveauFin);

				// Valider qu'il n'y a pas de nouveau chevauchement
				boolean conflitApresDeplacement = activiteDAO.hasChevauchement(
						aDecaler.getIdActivite(),
						aDecaler.getHoraireDebut(),
						aDecaler.getHoraireFin()
				);

				if (!conflitApresDeplacement) {
					boolean ok = activiteDAO.modifier(aDecaler);
					if (ok) {
						resolus++;
					} else {
						// Annuler en mémoire si la BD refuse
						aDecaler.setHoraireDebut(ancienDebut);
						aDecaler.setHoraireFin(ancienFin);
					}
				} else {
					// Annuler en mémoire si conflit persiste
					aDecaler.setHoraireDebut(ancienDebut);
					aDecaler.setHoraireFin(ancienFin);
				}
			}
		}
		return resolus;
	}

	@Override
	public boolean marquerConflitCommeResolu(Long idConflit) {
		return conflitDAO.marquerCommeResolu(idConflit);
	}
}
