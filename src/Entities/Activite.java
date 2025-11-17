package Entities;

import java.time.LocalDateTime;

public class Activite {
	private String titre;
	private TypeActivite type;
	private int duree;
	private int priorite;
	private LocalDateTime deadline;
	private LocalDateTime horaireDebut;
	private LocalDateTime horaireFin;


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

}
