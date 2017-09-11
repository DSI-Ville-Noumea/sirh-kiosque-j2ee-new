package nc.noumea.mairie.kiosque.eae.dto;

/*
 * #%L
 * sirh-kiosque-j2ee
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Mairie de Nouméa
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import nc.noumea.mairie.kiosque.dto.JsonDateDeserializer;
import nc.noumea.mairie.kiosque.dto.JsonDateSerializer;

public class EaeIdentificationStatutDto {

	private Integer	ancienneteEchelonJours;
	private String	cadre;
	private String	categorie;
	private String	classification;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date	dateEffet;
	private String	echelon;
	private String	grade;
	private String	nouvGrade;
	private String	nouvEchelon;
	private String	statutPrecision;
	private String	modeAcces;

	private String	statut;

	public Integer getAncienneteEchelonJours() {
		return ancienneteEchelonJours;
	}

	public void setAncienneteEchelonJours(Integer ancienneteEchelonJours) {
		this.ancienneteEchelonJours = ancienneteEchelonJours;
	}

	public String getCadre() {
		return cadre;
	}

	public void setCadre(String cadre) {
		this.cadre = cadre;
	}

	public String getCategorie() {
		return categorie;
	}

	public void setCategorie(String categorie) {
		this.categorie = categorie;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public Date getDateEffet() {
		return dateEffet;
	}

	public void setDateEffet(Date dateEffet) {
		this.dateEffet = dateEffet;
	}

	public String getEchelon() {
		return echelon;
	}

	public void setEchelon(String echelon) {
		this.echelon = echelon;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getNouvGrade() {
		return nouvGrade;
	}

	public void setNouvGrade(String nouvGrade) {
		this.nouvGrade = nouvGrade;
	}

	public String getNouvEchelon() {
		return nouvEchelon;
	}

	public void setNouvEchelon(String nouvEchelon) {
		this.nouvEchelon = nouvEchelon;
	}

	public String getStatutPrecision() {
		return statutPrecision;
	}

	public void setStatutPrecision(String statutPrecision) {
		this.statutPrecision = statutPrecision;
	}

	public String getStatut() {
		return statut;
	}

	public void setStatut(String statut) {
		this.statut = statut;
	}

	public String getModeAcces() {
		return modeAcces;
	}

	public void setModeAcces(String modeAcces) {
		this.modeAcces = modeAcces;
	}
}
