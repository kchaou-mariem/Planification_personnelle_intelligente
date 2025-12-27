package dao.interfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import entities.Activite;
import entities.TypeActivite;

public interface ActiviteDAO {

    // ========== MÉTHODES DE BASE ==========
    Long ajouter(Activite activite);

    boolean modifier(Activite activite);

    boolean supprimer(Long idActivite);

    Optional<Activite> getById(Long idActivite);

    List<Activite> getAll();

    // ========== MÉTHODES AVEC userId ==========
    List<Activite> getByUtilisateur(Long idUtilisateur);

    List<Activite> getByTypeAndUtilisateur(Long idUtilisateur, TypeActivite type);

    List<Activite> getByUtilisateurAndPeriode(Long idUtilisateur, LocalDateTime dateDebut, LocalDateTime dateFin);

    List<Activite> rechercherParMotCleUtilisateur(Long idUtilisateur, String motCle);

    List<Activite> getActivitesChevauchantesUtilisateur(Long idUtilisateur, LocalDateTime horaireDebut,
            LocalDateTime horaireFin);

    int compterActivitesUtilisateur(Long idUtilisateur);

    // ========== MÉTHODES GÉNÉRALES ==========
    List<Activite> getByType(TypeActivite type);

    List<Activite> getByPeriode(LocalDateTime dateDebut, LocalDateTime dateFin);

    List<Activite> getActivitesAvecDeadlineProche(int joursAvance);

    List<Activite> getByPriorite(int priorite);

    List<Activite> rechercherParMotCle(String motCle);

    boolean hasChevauchement(Long idActivite, LocalDateTime horaireDebut, LocalDateTime horaireFin);

    List<Activite> getActivitesChevauchantes(LocalDateTime horaireDebut, LocalDateTime horaireFin);

    int compterToutesLesActivites();

    int compterParType(TypeActivite type);

    List<Activite> getActivitesRecentes(int limite);

    List<Activite> getActivitesHautePriorite();
}