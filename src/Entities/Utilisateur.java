package entities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

public class Utilisateur {
	private int id;
	private String nom;
	private String prenom;
	private String email;
	private int age;
	private String genre;
	private String poste;
	private String motdepassehash;
	private String salt;
	private ArrayList<Activite> liste_activite;
    private ArrayList<Contrainte> liste_contrainte;
	

//constructeur complet	
public Utilisateur(int id, String nom, String prenom, String email, int age, String genre, String poste,
		String motdepassehash, ArrayList<Activite> liste_activite, ArrayList<Contrainte> liste_contrainte) {
	super();
	this.id = id;
	this.nom = nom;
	this.prenom = prenom;
	this.email = email;
	this.age = age;
	this.genre = genre;
	this.poste = poste;
	this.motdepassehash = motdepassehash;
	this.liste_activite = liste_activite;
	this.liste_contrainte = liste_contrainte;
}
//constructeur sans listes
	public Utilisateur(int id, String nom, String prenom, String email, int age, String genre, String poste,
			String motdepassehash) {
		super();
		this.id = id;
		this.nom = nom;
		this.prenom = prenom;
		this.email = email;
		this.age = age;
		this.genre = genre;
		this.poste = poste;
		this.motdepassehash = motdepassehash;
		
	}

public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public int getAge() {
	return age;
}
public void setAge(int age) {
	this.age = age;
}
public String getGenre() {
	return genre;
}
public void setGenre(String genre) {
	this.genre = genre;
}
public String getPoste() {
	return poste;
}
public void setPoste(String poste) {
	this.poste = poste;
}
public String getEmail() {
	return email;
}
public void setEmail(String email) {
	this.email = email;
}
public String getNom() {
	return nom;
}
public void setNom(String nom) {
	this.nom = nom;
}
public String getPrenom() {
	return prenom;
}
public void setPrenom(String prenom) {
	this.prenom = prenom;
}
public String getMotdepasse() {
	return motdepassehash;
}
public void setMotdepasse(String motdepasse) {
	this.motdepassehash = motdepasse;
}
public String getSalt() {
    return salt;
}
public void setSalt(String salt) {
	this.salt = salt;
}
public ArrayList<Activite> getListe_activite() {
	return liste_activite;
}
public void setListe_activite(ArrayList<Activite> liste_activite) {
	this.liste_activite = liste_activite;
}
public ArrayList<Contrainte> getListe_contrainte() {
	return liste_contrainte;
}
public void setListe_contrainte(ArrayList<Contrainte> liste_contrainte) {
	this.liste_contrainte = liste_contrainte;
}
 public String toString() {
        return "Info de l'utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", poste='" + poste + '\'' +
                ", motde passe='" + motdepassehash + '\'' +
                ", Nombres d'Activites=" + liste_activite.size() +
                ", Nombres de Contraintes=" + liste_contrainte.size() +
                '}';
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
		Utilisateur other = (Utilisateur) obj;
		return id == other.id;
	}
public void ajouter_activite(Activite activite) {
	liste_activite.add(activite);
}
public void supprimer_activite(Activite activite) {
	liste_activite.remove(activite);
}
public void ajouter_contrainte(Contrainte contrainte) {
	liste_contrainte.add(contrainte);
}
public void supprimer_contrainte(Contrainte contrainte) {
	liste_contrainte.remove(contrainte);
}
//hashage password : avec ces deux méthodes : SHA-256 + Salt
//generateSalt() crée une valeur aléatoire (le "sel") utilisée pour renforcer la sécurité des mots de passe stockés.
//generateSalt() : Comme S&P en traitement d'image
private String generate_salt() {
    byte[] salt = new SecureRandom().generateSeed(16);
    return Base64.getEncoder().encodeToString(salt);
}
private String hash_password(String motdepasse, String salt) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");//MessageDigest est une classe intégrée de Java (dans java.security) qui permet de calculer un hash cryptographique.
        String texte = motdepasse + salt;
        byte[] hash = digest.digest(texte.getBytes());

        return Base64.getEncoder().encodeToString(hash);

    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("Erreur lors du hashage du mot de passe.");
    }
}

public void set_mot_de_passe(String motdepasse) {
	this.salt = generate_salt();
	this.motdepassehash=hash_password(motdepasse, this.salt);	
}


public boolean modifierNom(String nouveauNom) {
	if (nouveauNom == null || nouveauNom.isEmpty()) {
		System.out.println(" Erreur : Le nom ne peut pas etre vide");
		return false;
	}
	this.nom = nouveauNom;
	System.out.println("Nom modifié avec succès : " + this.nom);
	return true;
}

public boolean modifierPrenom(String nouveauPrenom) {
	if (nouveauPrenom == null || nouveauPrenom.isEmpty()) {
		System.out.println("Erreur : Le prénom ne peut pas être vide");
		return false;
	}
	this.prenom = nouveauPrenom;
	System.out.println("Prénom modifié avec succès : " + this.prenom);
	return true;
}

public boolean modifierEmail(String nouvelEmail) {
	if (nouvelEmail == null || nouvelEmail.isEmpty()) {
		System.out.println("Erreur : L'email ne peut pas être vide");
		return false;
	}
	// Validation simple de l'email
	if (!nouvelEmail.contains("@") || !nouvelEmail.contains(".")) {
		System.out.println(" Erreur : Format d'email invalide");
		return false;
	}
	this.email = nouvelEmail;
	System.out.println("Email modifié avec succès : " + this.email);
	return true;
}


public boolean modifierAge(int nouvelAge) {
	if (nouvelAge < 0 || nouvelAge > 100) {
		System.out.println("Erreur : Âge invalide (doit être entre 0 et 0)");
		return false;
	}
	this.age = nouvelAge;
	System.out.println("Âge modifié avec succès : " + this.age);
	return true;
}

public boolean modifierGenre(String nouveauGenre) {
	if (nouveauGenre == null || nouveauGenre.isEmpty()) {
		System.out.println("Erreur : Le genre ne peut pas être vide");
		return false;
	}
	
	String genreNormal = nouveauGenre;
	if (!genreNormal.equalsIgnoreCase("Homme") && !genreNormal.equalsIgnoreCase("Femme")) {
		System.out.println("Erreur : Le genre doit être 'Homme' ou 'Femme'");
		return false;
	}
	
	this.genre = genreNormal.substring(0,1).toUpperCase() + genreNormal.substring(1).toLowerCase();
	System.out.println("Genre modifié avec succès : " + this.genre);
	return true;
}


public boolean modifierPoste(String nouveauPoste) {
	if (nouveauPoste == null || nouveauPoste.trim().isEmpty()) {
		System.out.println("Erreur : Le poste ne peut pas être vide");
		return false;
	}
	this.poste = nouveauPoste;
	System.out.println("Poste modifié avec succès : " + this.poste);
	return true;
}

public boolean modifierProfil(String nouveauNom, String nouveauPrenom, String nouvelEmail, 
                               int nouvelAge, String nouveauGenre, String nouveauPoste) {
	boolean modificationReussie = false;
	
	if (nouveauNom != null) {
		modificationReussie = modifierNom(nouveauNom) || modificationReussie;
	}
	
	if (nouveauPrenom != null) {
		modificationReussie = modifierPrenom(nouveauPrenom) || modificationReussie;
	}
	
	if (nouvelEmail != null) {
		modificationReussie = modifierEmail(nouvelEmail) || modificationReussie;
	}
	
	if (nouvelAge >= 0 && nouvelAge <= 100) {
		modificationReussie = modifierAge(nouvelAge) || modificationReussie;
	}
	
	if (nouveauGenre != null) {
		modificationReussie = modifierGenre(nouveauGenre) || modificationReussie;
	}
	
	if (nouveauPoste != null) {
		modificationReussie = modifierPoste(nouveauPoste) || modificationReussie;
	}
	
	if (modificationReussie) {
		System.out.println("\nProfil mis à jour avec succès!");
	} else {
		System.out.println("\nAucune modification n'a été effectuée");
	}
	
	return modificationReussie;
}

}
