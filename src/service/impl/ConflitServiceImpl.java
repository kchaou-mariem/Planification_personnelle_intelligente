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
import java.util.Map;
import java.util.stream.Collectors;

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

	// ========== DÉTECTION ET RÉSOLUTION ==========

	@Override
	public List<Conflit> detecterChevauchementsUtilisateur(Long idUtilisateur) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}

		List<Activite> activites = activiteDAO.getByUtilisateur(idUtilisateur);
		
		// Logique métier: vérifier qu'il y a au moins 2 activités
		if (activites.size() < 2) {
			return new ArrayList<>(); // Pas de chevauchement possible
		}
		
		activites.sort(Comparator.comparing(Activite::getHoraireDebut));

		List<Conflit> conflits = new ArrayList<>();
		for (int i = 0; i < activites.size(); i++) {
			for (int j = i + 1; j < activites.size(); j++) {
				Activite a = activites.get(i);
				Activite b = activites.get(j);
				
				// Logique métier: vérifier que les activités ont des horaires valides
				if (a.getHoraireDebut() == null || a.getHoraireFin() == null ||
					b.getHoraireDebut() == null || b.getHoraireFin() == null) {
					continue;
				}
				
				boolean chevauche = a.getHoraireDebut().isBefore(b.getHoraireFin())
						&& a.getHoraireFin().isAfter(b.getHoraireDebut());
				
				if (chevauche) {
					// Logique métier: vérifier si ce conflit n'existe pas déjà
					if (!conflitDejaExiste(a.getIdActivite(), b.getIdActivite())) {
						Conflit conflit = new Conflit(
								null,
								LocalDateTime.now(),
								TypeConflit.CHEVAUCHEMENT_DES_ACTIVITES,
								false
						);
						Long id = conflitDAO.ajouter(conflit);
						if (id != null && id > 0) {
							conflit.setidConflit(id);
							// Lier les deux activités au conflit
							conflitDAO.lierActiviteAuConflit(id, a.getIdActivite());
							conflitDAO.lierActiviteAuConflit(id, b.getIdActivite());
							conflits.add(conflit);
						}
					}
				}
			}
		}
		return conflits;
	}
	@Override
	public boolean marquerConflitCommeResolu(Long idConflit) {
		// Validation des paramètres
		if (idConflit == null || idConflit <= 0) {
			throw new IllegalArgumentException("ID conflit invalide");
		}
		
		// Logique métier: vérifier que le conflit existe avant de le marquer
		if (!conflitDAO.getById(idConflit).isPresent()) {
			return false;
		}
		
		return conflitDAO.marquerCommeResolu(idConflit);
	}

	// ========== CONSULTATION ==========

	@Override
	public List<Conflit> getTousLesConflitsUtilisateur(Long idUtilisateur) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		
		List<Conflit> conflits = conflitDAO.getByUtilisateur(idUtilisateur);
		
		// Logique métier: trier par date de détection (plus récents d'abord)
		conflits.sort((c1, c2) -> c2.getHoraireDetection().compareTo(c1.getHoraireDetection()));
		
		return conflits;
	}

	@Override
	public List<Conflit> getConflitsNonResolusUtilisateur(Long idUtilisateur) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		
		List<Conflit> conflits = conflitDAO.getByUtilisateur(idUtilisateur);
		
		// Logique métier: filtrer uniquement les conflits non résolus
		conflits.removeIf(Conflit::isResolu);//Cette ligne supprime tous les conflits résolus d'une collection (conflits) en utilisant une méthode appelée removeIf().

		// Logique métier: trier par date (plus récents d'abord)
		conflits.sort((c1, c2) -> c2.getHoraireDetection().compareTo(c1.getHoraireDetection()));
		
		return conflits;
	}

	@Override
	public List<Conflit> getConflitsCritiquesUtilisateur(Long idUtilisateur) { //non résolus et prioritaires)
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		
		List<Conflit> tousConflits = conflitDAO.getByUtilisateur(idUtilisateur);
		List<Conflit> critiques = new ArrayList<>();

		// Logique métier: filtrer uniquement les conflits critiques non résolus
		for (Conflit c : tousConflits) {
			if (!c.isResolu() && estCritique(c.getType())) {
				critiques.add(c);
			}
		}
		
		// Logique métier: trier par date (plus récents d'abord)
		critiques.sort((c1, c2) -> c2.getHoraireDetection().compareTo(c1.getHoraireDetection()));
		
		return critiques;
	}

	@Override
	public List<Conflit> getConflitsParTypeUtilisateur(Long idUtilisateur, TypeConflit type) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		if (type == null) {
			throw new IllegalArgumentException("Type de conflit invalide");
		}
		
		List<Conflit> conflits = conflitDAO.getByType(type);
		
		// Logique métier: filtrer par utilisateur
		List<Conflit> conflitsUtilisateur = new ArrayList<>();
		for (Conflit c : conflits) {
			// Vérifier si ce conflit appartient à l'utilisateur
			List<Long> activites = conflitDAO.getActivitesLieesAuConflit(c.getidConflit());
			if (!activites.isEmpty()) {
				Activite a = activiteDAO.getById(activites.get(0)).orElse(null);
				if (a != null && a.getIdUtilisateur().equals(idUtilisateur)) {
					conflitsUtilisateur.add(c);
				}
			}
		}
		
		// Logique métier: trier par date de détection
		conflitsUtilisateur.sort((c1, c2) -> c2.getHoraireDetection().compareTo(c1.getHoraireDetection()));
		
		return conflitsUtilisateur;
	}

	@Override
	public List<Conflit> getConflitsResolusUtilisateur(Long idUtilisateur) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		
		List<Conflit> conflits = conflitDAO.getByUtilisateur(idUtilisateur);
		
		// Logique métier: filtrer uniquement les conflits résolus
		conflits.removeIf(c -> !c.isResolu());//c -> !c.isResolu() est une expression lambda en Java qui selectionne les conflits non résolus.
		
		// Logique métier: trier par date de détection
		conflits.sort((c1, c2) -> c2.getHoraireDetection().compareTo(c1.getHoraireDetection()));
		
		return conflits;
	}

	@Override
	public List<Conflit> getConflitsParPeriodeUtilisateur(Long idUtilisateur, LocalDateTime debut, LocalDateTime fin) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		if (debut == null || fin == null) {
			throw new IllegalArgumentException("Période invalide");
		}
		if (debut.isAfter(fin)) {
			throw new IllegalArgumentException("La date de début doit être avant la date de fin");
		}
		
		List<Conflit> tousConflits = conflitDAO.getByUtilisateur(idUtilisateur);
		List<Conflit> conflitsPeriode = new ArrayList<>();
		
		// Logique métier: filtrer par période
		for (Conflit c : tousConflits) {
			if (!c.getHoraireDetection().isBefore(debut) && !c.getHoraireDetection().isAfter(fin)) {
				conflitsPeriode.add(c);
			}
		}
		
		// Logique métier: trier par date
		conflitsPeriode.sort((c1, c2) -> c2.getHoraireDetection().compareTo(c1.getHoraireDetection()));
		
		return conflitsPeriode;
	}

	@Override
	public List<Conflit> rechercherConflitsParMotCle(Long idUtilisateur, String motCle) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		if (motCle == null || motCle.trim().isEmpty()) {
			throw new IllegalArgumentException("Mot-clé invalide");
		}
		
		List<Conflit> tousConflits = conflitDAO.getByUtilisateur(idUtilisateur);
		List<Conflit> resultat = new ArrayList<>();
		String motCleNormalise = motCle.toLowerCase().trim();
		
		// Logique métier: rechercher dans le type de conflit et les activités liées
		for (Conflit c : tousConflits) {
			boolean correspondance = false;
			
			// Recherche dans le type
			if (c.getType().toString().toLowerCase().contains(motCleNormalise)) {
				correspondance = true;
			}
			
			if (correspondance) {
				resultat.add(c);
			}
		}
		
		// Logique métier: trier par pertinence (d'abord les non résolus, puis par date)
		resultat.sort((c1, c2) -> {
			if (c1.isResolu() != c2.isResolu()) {
				return c1.isResolu() ? 1 : -1;
			}
			return c2.getHoraireDetection().compareTo(c1.getHoraireDetection());
		});
		
		return resultat;
	}

	@Override
	public List<Activite> getActivitesImpliqueesDansConflit(Long idConflit) {
		// Validation des paramètres
		if (idConflit == null || idConflit <= 0) {
			throw new IllegalArgumentException("ID conflit invalide");
		}
		
		// Logique métier: vérifier que le conflit existe
		if (!conflitDAO.getById(idConflit).isPresent()) {
			return new ArrayList<>();
		}
		
		List<Long> idsActivites = conflitDAO.getActivitesLieesAuConflit(idConflit);
		List<Activite> activites = new ArrayList<>();

		// Logique métier: récupérer les activités et les trier par horaire
		for (Long idActivite : idsActivites) {
			activiteDAO.getById(idActivite).ifPresent(activites::add);
		}
		
		// Trier par horaire de début
		activites.sort(Comparator.comparing(Activite::getHoraireDebut));
		
		return activites;
	}

	// ========== STATISTIQUES ==========

	@Override
	public int compterConflitsUtilisateur(Long idUtilisateur) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		
		// Logique métier: utiliser le DAO optimisé pour le comptage
		return conflitDAO.compterParUtilisateur(idUtilisateur);
	}

	@Override
	public int compterConflitsNonResolusUtilisateur(Long idUtilisateur) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		
		// Logique métier: compter uniquement les conflits actifs
		List<Conflit> conflits = conflitDAO.getByUtilisateur(idUtilisateur);
		return (int) conflits.stream().filter(c -> !c.isResolu()).count();
	}

	@Override
	public double getTauxResolutionUtilisateur(Long idUtilisateur) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		
		List<Conflit> conflits = conflitDAO.getByUtilisateur(idUtilisateur);
		
		// Logique métier: retourner 100% si aucun conflit (convention)
		if (conflits.isEmpty()) {
			return 100.0;
		}
		
		// Compter les conflits résolus
		long resolus = conflits.stream().filter(Conflit::isResolu).count();
		
		// Logique métier: arrondir à 2 décimales
		double taux = (resolus * 100.0) / conflits.size();
		return Math.round(taux * 100.0) / 100.0;
	}

	@Override
	public Map<TypeConflit, Integer> getStatistiquesParTypeUtilisateur(Long idUtilisateur) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		
		List<Conflit> conflits = conflitDAO.getByUtilisateur(idUtilisateur);
		
		// Logique métier: grouper par type
		return conflits.stream()
			.collect(Collectors.groupingBy(
				Conflit::getType,
				Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
			));
	}

	@Override
	public int compterConflitsParTypeUtilisateur(Long idUtilisateur, TypeConflit type) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		if (type == null) {
			throw new IllegalArgumentException("Type de conflit invalide");
		}
		
		List<Conflit> conflits = conflitDAO.getByUtilisateur(idUtilisateur);
		
		// Logique métier: compter uniquement les conflits du type spécifié
		return (int) conflits.stream()
			.filter(c -> c.getType() == type)
			.count();
	}

	// ========== GESTION AVANCÉE ==========

	@Override
	public boolean supprimerConflit(Long idConflit) {
		// Validation des paramètres
		if (idConflit == null || idConflit <= 0) {
			throw new IllegalArgumentException("ID conflit invalide");
		}
		
		// Logique métier: vérifier que le conflit existe
		if (!conflitDAO.getById(idConflit).isPresent()) {
			return false;
		}
		
		// Logique métier: supprimer d'abord les liens (intégrité référentielle)
		conflitDAO.supprimerLiensConflit(idConflit);
		
		// Puis supprimer le conflit lui-même
		return conflitDAO.supprimer(idConflit);
	}

	@Override
	public int nettoyerConflitsResolusUtilisateur(Long idUtilisateur) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		
		List<Conflit> conflitsResolus = conflitDAO.getByUtilisateur(idUtilisateur);
		
		// Logique métier: filtrer uniquement les conflits résolus
		List<Conflit> aSupprimer = new ArrayList<>();
		for (Conflit c : conflitsResolus) {
			if (c.isResolu()) {
				aSupprimer.add(c);
			}
		}
		
		// Logique métier: aucun conflit à nettoyer
		if (aSupprimer.isEmpty()) {
			return 0;
		}

		// Logique métier: supprimer en transaction logique
		int supprimes = 0;
		for (Conflit c : aSupprimer) {
			if (supprimerConflit(c.getidConflit())) {
				supprimes++;
			}
		}
		
		return supprimes;
	}

	@Override
	public int marquerPlusieursConflitsCommeResolus(List<Long> idsConflits) {
		// Validation des paramètres
		if (idsConflits == null || idsConflits.isEmpty()) {
			throw new IllegalArgumentException("Liste d'IDs invalide");
		}
		
		int marques = 0;
		
		// Logique métier: marquer chaque conflit individuellement avec validation
		for (Long idConflit : idsConflits) {
			if (idConflit != null && idConflit > 0) {
				if (marquerConflitCommeResolu(idConflit)) {
					marques++;
				}
			}
		}
		
		return marques;
	}

	@Override
	public int supprimerConflitsResolusAvant(Long idUtilisateur, LocalDateTime date) {
		// Validation des paramètres
		if (idUtilisateur == null || idUtilisateur <= 0) {
			throw new IllegalArgumentException("ID utilisateur invalide");
		}
		if (date == null) {
			throw new IllegalArgumentException("Date invalide");
		}
		
		// Logique métier: ne pas supprimer les conflits futurs
		if (date.isAfter(LocalDateTime.now())) {
			throw new IllegalArgumentException("La date ne peut pas être dans le futur");
		}
		
		// Récupérer les conflits résolus de l'utilisateur
		List<Conflit> conflitsResolus = getConflitsResolusUtilisateur(idUtilisateur);
		int supprimes = 0;
		
		// Logique métier: filtrer et supprimer uniquement les conflits résolus avant la date
		for (Conflit c : conflitsResolus) {
			if (c.getHoraireDetection().isBefore(date)) {
				if (supprimerConflit(c.getidConflit())) {
					supprimes++;
				}
			}
		}
		
		return supprimes;
	}

	// ========== MÉTHODES UTILITAIRES PRIVÉES ==========

	/**
	 * Vérifie si un conflit existe déjà entre deux activités.
	 */
	private boolean conflitDejaExiste(Long idActivite1, Long idActivite2) {
		// Récupérer tous les conflits non résolus
		List<Conflit> conflitsNonResolus = conflitDAO.getConflitsNonResolus();
		
		for (Conflit conflit : conflitsNonResolus) {
			List<Long> activitesLiees = conflitDAO.getActivitesLieesAuConflit(conflit.getidConflit());
			
			// Vérifier si les deux activités sont déjà liées à ce conflit
			if (activitesLiees.size() == 2 &&
				activitesLiees.contains(idActivite1) &&
				activitesLiees.contains(idActivite2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Détermine si un type de conflit est critique.
	 */
	private boolean estCritique(TypeConflit type) {
		return type == TypeConflit.CHEVAUCHEMENT_DES_ACTIVITES ||
				type == TypeConflit.DEADLINE ||
				type == TypeConflit.VIOLATION_DE_CONTRAINTE;
	}
}
