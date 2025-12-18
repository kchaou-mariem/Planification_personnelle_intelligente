package dao.interfaces;

import entities.Utilisateur;
import java.util.List;

public interface UtilisateurDAOinterface {
    
    boolean inserer(Utilisateur utilisateur);
    
    
    boolean modifier(Utilisateur utilisateur);
    
    // Suppression
    boolean supprimer(int id);
    
   
    Utilisateur getById(int id);
    List<Utilisateur> getAll();
    Utilisateur getByEmail(String email);
}
