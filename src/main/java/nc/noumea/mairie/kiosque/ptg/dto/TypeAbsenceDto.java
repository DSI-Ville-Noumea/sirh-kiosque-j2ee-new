package nc.noumea.mairie.kiosque.ptg.dto;

import nc.noumea.mairie.kiosque.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeSaisiDto;

public class TypeAbsenceDto {

	private Integer idRefTypeAbsence;
	private String libelle;
	private RefGroupeAbsenceDto groupeAbsence;
	private RefTypeSaisiDto typeSaisiDto;

	public TypeAbsenceDto() {

	}

	public Integer getIdRefTypeAbsence() {
		return idRefTypeAbsence;
	}

	public void setIdRefTypeAbsence(Integer idRefTypeAbsence) {
		this.idRefTypeAbsence = idRefTypeAbsence;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	@Override
	public boolean equals(Object obj) {
		return idRefTypeAbsence.toString().equals(((TypeAbsenceDto) obj).getIdRefTypeAbsence().toString());
	}

	public RefGroupeAbsenceDto getGroupeAbsence() {
		return groupeAbsence;
	}

	public void setGroupeAbsence(RefGroupeAbsenceDto groupeAbsence) {
		this.groupeAbsence = groupeAbsence;
	}

	public RefTypeSaisiDto getTypeSaisiDto() {
		return typeSaisiDto;
	}

	public void setTypeSaisiDto(RefTypeSaisiDto typeSaisiDto) {
		this.typeSaisiDto = typeSaisiDto;
	}
}
