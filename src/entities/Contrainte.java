package Entities;

import java.time.LocalDate;
import java.util.Objects;

public class Contrainte {
	private Long id; 
	private String titre;
	private TypeContrainte type;
	private LocalDate heureDeb;
	private LocalDate heureFin;
	private boolean repetitif;
	private String jour;
	private Long utilisateurId;
	
	private static Long compteurId = 1L;//La variable appartient à la classe, pas aux objets //1L : commence par 1,L:Long
	 
	 
	 
	public Contrainte() {
		super();
        this.id = compteurId;
        compteurId++;

	}
	
	
	public Contrainte(String titre,TypeContrainte type, LocalDate heureDeb, LocalDate heureFin, boolean repetitif, String jour) {
		super();
        this.id = compteurId;
        this.titre=titre;
		this.type = type;
		this.heureDeb = heureDeb;
		this.heureFin = heureFin;
		this.repetitif = repetitif;
		this.jour = jour;
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
	public TypeContrainte getType() {
		return type;
	}
	public void setType(TypeContrainte type) {
		this.type = type;
	}
	public LocalDate getHeureDeb() {
		return heureDeb;
	}
	public void setHeureDeb(LocalDate heureDeb) {
		this.heureDeb = heureDeb;
	}
	public LocalDate getHeureFin() {
		return heureFin;
	}
	public void setHeureFin(LocalDate heureFin) {
		this.heureFin = heureFin;
	}
	public boolean isRepetitif() {
		return repetitif;
	}
	public void setRepetitif(boolean repetitif) {
		this.repetitif = repetitif;
	}
	public String getJour() {
		return jour;
	}
	public void setJour(String jour) {
		this.jour = jour;
	}


	public Long getUtilisateurId() {
		return utilisateurId;
	}


	@Override
	public String toString() {
		return "Contrainte [titre=" + titre + ", type=" + type + ", heureDeb=" + heureDeb + ", heureFin=" + heureFin
				+ ", repetitif=" + repetitif + ", jour=" + jour + "]";
	}


	
	
	
	
}


	
	 

