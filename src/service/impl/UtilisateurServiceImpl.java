package service.impl;

import service.UtilisateurService;
import dao.interfaces.UtilisateurDAOinterface;
import dao.impl.UtilisateurDAOImpl;
import entities.Utilisateur;
import java.util.List;

public class UtilisateurServiceImpl implements UtilisateurService {
    
    private UtilisateurDAOinterface utilisateurDAO;
    
    public UtilisateurServiceImpl() {
        this.utilisateurDAO = new UtilisateurDAOImpl();
    }
    

    @Override
    public boolean creerUtilisateur(Utilisateur utilisateur) {
        if (utilisateur == null) {
            System.out.println("Erreur : L'utilisateur ne peut pas être null");
            return false;
        }
        return utilisateurDAO.inserer(utilisateur);
    }
    
    @Override
    public boolean modifierUtilisateur(Utilisateur utilisateur) {
        if (utilisateur == null) {
            System.out.println("Erreur : L'utilisateur ne peut pas être null");
            return false;
        }
        return utilisateurDAO.modifier(utilisateur);
    }
    
    @Override
    public boolean supprimerUtilisateur(int id) {
        return utilisateurDAO.supprimer(id);
    }
    
    @Override
    public Utilisateur getUtilisateurById(int id) {
        return utilisateurDAO.getById(id);
    }
    
    @Override
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurDAO.getAll();
    }
    
    @Override
    public boolean modifierNom(int userId, String nouveauNom) {
        if (nouveauNom == null || nouveauNom.isEmpty()) {
            System.out.println("Erreur : Le nom ne peut pas être vide");
            return false;
        }
        
        Utilisateur utilisateur = utilisateurDAO.getById(userId);
        if (utilisateur == null) {
            System.out.println("Erreur : Utilisateur introuvable");
            return false;
        }
        
        utilisateur.setNom(nouveauNom);
        return utilisateurDAO.modifier(utilisateur);
    }
    
    @Override
    public boolean modifierPrenom(int userId, String nouveauPrenom) {
        if (nouveauPrenom == null || nouveauPrenom.isEmpty()) {
            System.out.println("Erreur : Le prénom ne peut pas être vide");
            return false;
        }
        
        Utilisateur utilisateur = utilisateurDAO.getById(userId);
        if (utilisateur == null) {
            System.out.println("Erreur : Utilisateur introuvable");
            return false;
        }
        
        utilisateur.setPrenom(nouveauPrenom);
        return utilisateurDAO.modifier(utilisateur);
    }
    @Override
    public Utilisateur getUtilisateurByEmail(String email) {
        return utilisateurDAO.getByEmail(email);
    }
    @Override
    public boolean modifierEmail(int userId, String nouvelEmail) {
        if (nouvelEmail == null || nouvelEmail.isEmpty()) {
            System.out.println("Erreur : L'email ne peut pas être vide");
            return false;
        }
        
        if (!nouvelEmail.contains("@") || !nouvelEmail.contains(".")) {
            System.out.println("Erreur : Format d'email invalide");
            return false;
        }
        
        Utilisateur utilisateur = utilisateurDAO.getById(userId);
        if (utilisateur == null) {
            System.out.println("Erreur : Utilisateur introuvable");
            return false;
        }
        
        utilisateur.setEmail(nouvelEmail);
        return utilisateurDAO.modifier(utilisateur);
    }
    
    @Override
    public boolean modifierAge(int userId, int nouvelAge) {
        if (nouvelAge < 0 || nouvelAge > 100) {
            System.out.println("Erreur : Âge invalide (doit être entre 0 et 100)");
            return false;
        }
        
        Utilisateur utilisateur = utilisateurDAO.getById(userId);
        if (utilisateur == null) {
            System.out.println("Erreur : Utilisateur introuvable");
            return false;
        }
        
        utilisateur.setAge(nouvelAge);
        return utilisateurDAO.modifier(utilisateur);
    }
    
    @Override
    public boolean modifierGenre(int userId, String nouveauGenre) {
        if (nouveauGenre == null || nouveauGenre.isEmpty()) {
            System.out.println("Erreur : Le genre ne peut pas être vide");
            return false;
        }
        
        if (!nouveauGenre.equalsIgnoreCase("Homme") && !nouveauGenre.equalsIgnoreCase("Femme")) {
            System.out.println("Erreur : Le genre doit être 'Homme' ou 'Femme'");
            return false;
        }
        
        Utilisateur utilisateur = utilisateurDAO.getById(userId);
        if (utilisateur == null) {
            System.out.println("Erreur : Utilisateur introuvable");
            return false;
        }
        
        String genreFormate = nouveauGenre.substring(0,1).toUpperCase() + nouveauGenre.substring(1).toLowerCase();
        utilisateur.setGenre(genreFormate);
        return utilisateurDAO.modifier(utilisateur);
    }
    
    @Override
    public boolean modifierPoste(int userId, String nouveauPoste) {
        if (nouveauPoste == null || nouveauPoste.trim().isEmpty()) {
            System.out.println("Erreur : Le poste ne peut pas être vide");
            return false;
        }
        
        Utilisateur utilisateur = utilisateurDAO.getById(userId);
        if (utilisateur == null) {
            System.out.println("Erreur : Utilisateur introuvable");
            return false;
        }
        
        utilisateur.setPoste(nouveauPoste);
        return utilisateurDAO.modifier(utilisateur);
    }
    
    @Override
    public boolean modifierProfil(int userId, String nouveauNom, String nouveauPrenom, String nouvelEmail, 
                                   int nouvelAge, String nouveauGenre, String nouveauPoste) {
        Utilisateur utilisateur = utilisateurDAO.getById(userId);
        if (utilisateur == null) {
            System.out.println("Erreur : Utilisateur introuvable");
            return false;
        }
        
        boolean modificationEffectuee = false;
        
        if (nouveauNom != null && !nouveauNom.isEmpty()) {
            utilisateur.setNom(nouveauNom);
            modificationEffectuee = true;
        }
        
        if (nouveauPrenom != null && !nouveauPrenom.isEmpty()) {
            utilisateur.setPrenom(nouveauPrenom);
            modificationEffectuee = true;
        }
        
        if (nouvelEmail != null && !nouvelEmail.isEmpty() && nouvelEmail.contains("@")) {
            utilisateur.setEmail(nouvelEmail);
            modificationEffectuee = true;
        }
        
        if (nouvelAge >= 0 && nouvelAge <= 100) {
            utilisateur.setAge(nouvelAge);
            modificationEffectuee = true;
        }
        
        if (nouveauGenre != null && (nouveauGenre.equalsIgnoreCase("Homme") || nouveauGenre.equalsIgnoreCase("Femme"))) {
            String genreFormate = nouveauGenre.substring(0,1).toUpperCase() + nouveauGenre.substring(1).toLowerCase();
            utilisateur.setGenre(genreFormate);
            modificationEffectuee = true;
        }
        
        if (nouveauPoste != null && !nouveauPoste.trim().isEmpty()) {
            utilisateur.setPoste(nouveauPoste);
            modificationEffectuee = true;
        }
        
        if (modificationEffectuee) {
            return utilisateurDAO.modifier(utilisateur);
        } else {
            System.out.println("Aucune modification n'a été effectuée");
            return false;
        }
    }
    
    @Override
    public Utilisateur authentifier(String email, String motDePasse) {
        if (email == null || motDePasse == null) {
            System.out.println("Erreur : Email et mot de passe requis");
            return null;
        }
        
        Utilisateur utilisateur = utilisateurDAO.getByEmail(email);
        if (utilisateur == null) {
            System.out.println("Erreur : Email incorrect");
            return null;
        }
        
        // Vérifier le mot de passe avec le hashage
        String salt = utilisateur.getSalt();
        if (salt == null || utilisateur.getMotdepasse() == null) {
            System.out.println("Erreur : Données d'authentification manquantes");
            return null;
        }
        
        // Hasher le mot de passe fourni avec le salt stocké
        String hashedPassword = hashPassword(motDePasse, salt);
        
        // Comparer avec le hash stocké
        if (hashedPassword != null && hashedPassword.equals(utilisateur.getMotdepasse())) {
            System.out.println("Authentification réussie pour : " + email);
            return utilisateur;
        } else {
            System.out.println("Erreur : Mot de passe incorrect");
            return null;
        }
    }
    
    /**
     * Hash un mot de passe avec un salt en utilisant SHA-256
     */
    private String hashPassword(String motdepasse, String salt) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            String texte = motdepasse + salt;
            byte[] hash = digest.digest(texte.getBytes());
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            System.err.println("Erreur lors du hashage du mot de passe.");
            return null;
        }
    }
}
