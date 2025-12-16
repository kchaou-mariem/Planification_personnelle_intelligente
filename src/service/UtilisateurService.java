package service;

import entities.Utilisateur;
import java.util.List;

public interface UtilisateurService {
    // Opérations CRUD
    boolean creerUtilisateur(Utilisateur utilisateur);
    boolean modifierUtilisateur(Utilisateur utilisateur);
    boolean supprimerUtilisateur(int id);
    Utilisateur getUtilisateurById(int id);
    List<Utilisateur> getAllUtilisateurs();
    
    // Modifications spécifiques
    boolean modifierNom(int userId, String nouveauNom);
    boolean modifierPrenom(int userId, String nouveauPrenom);
    boolean modifierEmail(int userId, String nouvelEmail);
    boolean modifierAge(int userId, int nouvelAge);
    boolean modifierGenre(int userId, String nouveauGenre);
    boolean modifierPoste(int userId, String nouveauPoste);
    boolean modifierProfil(int userId, String nouveauNom, String nouveauPrenom, String nouvelEmail, int nouvelAge, String nouveauGenre, String nouveauPoste);
    
    // Authentification
    Utilisateur authentifier(String email, String motDePasse);
}
