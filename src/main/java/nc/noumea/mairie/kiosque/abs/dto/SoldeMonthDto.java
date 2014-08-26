package nc.noumea.mairie.kiosque.abs.dto;

import java.util.Date;

public class SoldeMonthDto {

	private int soldeAsaA55;
	private Date dateDebut;
	private Date dateFin;

	public Date getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public Date getDateFin() {
		return dateFin;
	}

	public void setDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}

	public int getSoldeAsaA55() {
		return soldeAsaA55;
	}

	public void setSoldeAsaA55(int soldeAsaA55) {
		this.soldeAsaA55 = soldeAsaA55;
	}

}
