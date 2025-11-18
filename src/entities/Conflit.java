package entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class Conflit {
	private Long idConflit;
    private LocalDateTime horaireDetection;
    private TypeConflit type;
    private boolean resolu;
    //private Activite activiteLiee;
    //private Contrainte contrainteAssociee;

    
    public Conflit(Long idConflit, LocalDateTime horaireDetection, TypeConflit type, boolean resolu) {
		super();
		this.idConflit = idConflit;
		this.horaireDetection = horaireDetection;
		this.type = type;
		this.resolu = resolu;
	}

    public Conflit(LocalDateTime detection, TypeConflit type, String description) {
        this.horaireDetection = detection;
        this.type = type;
        this.resolu = false;
    }
    
	public Long getidConflit() {
		return idConflit;
	}

	public void setidConflit(Long idConflit) {
		this.idConflit = idConflit;
	}


	public LocalDateTime getHoraireDetection() {
		return horaireDetection;
	}
	public void setHoraireDetection(LocalDateTime horaireDetection) {
		this.horaireDetection = horaireDetection;
	}
	public TypeConflit getType() {
		return type;
	}
	public void setType(TypeConflit type) {
		this.type = type;
	}
	public boolean isResolu() {
		return resolu;
	}
	public void marquerCommeResolu() {
	    this.resolu = true;
	}

	@Override
	public String toString() {
		return "Conflit [idConflit=" + idConflit + ", horaireDetection=" + horaireDetection + ", type=" + type
				+ ", resolu=" + resolu + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(idConflit);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Conflit other = (Conflit) obj;
		return Objects.equals(idConflit, other.idConflit);
	}

	
}