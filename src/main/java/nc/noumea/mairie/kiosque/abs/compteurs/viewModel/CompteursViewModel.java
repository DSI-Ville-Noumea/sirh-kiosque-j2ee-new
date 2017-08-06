package nc.noumea.mairie.kiosque.abs.compteurs.viewModel;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

import nc.noumea.mairie.ads.dto.EntiteDto;
import nc.noumea.mairie.kiosque.abs.dto.CompteurDto;
import nc.noumea.mairie.kiosque.abs.dto.FiltreSoldeDto;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeAbsenceEnum;
import nc.noumea.mairie.kiosque.abs.dto.SaisieGardeDto;
import nc.noumea.mairie.kiosque.abs.dto.SoldeDto;
import nc.noumea.mairie.kiosque.dto.AgentDto;
import nc.noumea.mairie.kiosque.dto.ReturnMessageDto;
import nc.noumea.mairie.kiosque.validation.ValidationMessage;
import nc.noumea.mairie.kiosque.viewModel.AbstractViewModel;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class CompteursViewModel extends AbstractViewModel implements Serializable {

	private static final long serialVersionUID = -7086425630331413874L;

	private String formulaireRecup;

	private SoldeDto soldeCourant;

	private CompteurDto compteurACreer;

	private String anneePrec;

	/* Pour les filtres */

	private List<RefTypeAbsenceDto> listeTypeAbsenceFiltre;

	private RefTypeAbsenceDto typeAbsenceFiltre;

	private List<EntiteDto> listeServicesFiltre;

	private EntiteDto serviceFiltre;

	private List<AgentDto> listeAgentsFiltre;

	private AgentDto agentFiltre;

	private List<Date> listeMoisFiltre;

	private Date moisFiltre;

	private SaisieGardeDto saisieGarde;

	List<String> listVide = new ArrayList<String>();

	@Init
	public void initCompteurs() {

		// on charge les types d'absences pour les filtres
		List<RefTypeAbsenceDto> filtreFamille = absWsConsumer.getRefGroupeAbsenceCompteur();
		setListeTypeAbsenceFiltre(filtreFamille);
		// on charge les service pour les filtres
		List<EntiteDto> filtreService = absWsConsumer.getServicesAbsencesOperateur(getCurrentUser().getAgent().getIdAgent());
		setListeServicesFiltre(filtreService);
		// pour les agents, on ne remplit pas la liste, elle le sera avec le
		// choix du service sauf si un seul service (#15772)
		if (getListeServicesFiltre().size() == 1) {
			setServiceFiltre(getListeServicesFiltre().get(0));
			chargeAgent();
		} else {
			setListeAgentsFiltre(null);
		}

		// on recupere les droits afin d afficher ou non la saisie des jours de
		// repos
		if (getDroitsAbsence().isSaisieGarde()) {
			List<Date> listeDate = new ArrayList<Date>();
			listeDate.add(getFirstDayOfCurrentMonth().toDate());
			listeDate.add(getFirstDayOfCurrentMonth().plusMonths(1).toDate());
			listeDate.add(getFirstDayOfCurrentMonth().plusMonths(2).toDate());
			setListeMoisFiltre(listeDate);
		}
	}

	@Command
	@NotifyChange({ "saisieGarde" })
	public void chargeListSaisieGarde() {

		Date dateDebutMois = getMoisFiltre();
		DateTime dateFin = new DateTime(getMoisFiltre());
		Date dateFinMois = dateFin.dayOfMonth().withMaximumValue().toDate();

		if (getDroitsAbsence().isSaisieGarde()) {
			setSaisieGarde(absWsConsumer.getListAgentsWithJoursFeriesEnGarde(getCurrentUser().getAgent().getIdAgent(), null,
					dateDebutMois, dateFinMois));
		}
	}

	@Command
	@NotifyChange({ "saisieGarde" })
	public void saveSaisieGarde() {
		Date dateDebutMois = getMoisFiltre();
		DateTime dateFin = new DateTime(getMoisFiltre());
		Date dateFinMois = dateFin.dayOfMonth().withMaximumValue().toDate();

		if (getDroitsAbsence().isSaisieGarde()) {
			ReturnMessageDto result = absWsConsumer.setListAgentsWithJoursFeriesEnGarde(getCurrentUser().getAgent()
					.getIdAgent(), dateDebutMois, dateFinMois, getSaisieGarde().getListAgentAvecGarde());

			setSaisieGarde(absWsConsumer.getListAgentsWithJoursFeriesEnGarde(getCurrentUser().getAgent().getIdAgent(), null,
					dateDebutMois, dateFinMois));

			final HashMap<String, Object> map = new HashMap<String, Object>();
			List<ValidationMessage> listErreur = new ArrayList<ValidationMessage>();
			List<ValidationMessage> listInfo = new ArrayList<ValidationMessage>();
			if (result.getErrors().size() == 0)
				result.getInfos().add("Les jours de garde des agents ont été enregistrés correctement.");
			for (String error : result.getErrors()) {
				ValidationMessage vm = new ValidationMessage(error);
				listErreur.add(vm);
			}
			for (String info : result.getInfos()) {
				ValidationMessage vm = new ValidationMessage(info);
				listInfo.add(vm);
			}
			map.put("errors", listErreur);
			map.put("infos", listInfo);
			Executions.createComponents("/messages/returnMessage.zul", null, map);
		}
	}

	@Command
	@NotifyChange({ "listeAgentsFiltre" })
	public void chargeAgent() {
		// on charge les agents pour les filtres
		List<AgentDto> filtreAgent = absWsConsumer.getAgentsAbsencesOperateur(getCurrentUser().getAgent().getIdAgent(),
				getServiceFiltre().getIdEntite());
		setListeAgentsFiltre(filtreAgent);
	}

	@Command
	@NotifyChange({ "formulaireRecup", "soldeCourant" })
	public void refreshNomAgent() {
		setFormulaireRecup(null);
		setSoldeCourant(null);
		if (getTypeAbsenceFiltre().getIdRefTypeAbsence() == RefTypeAbsenceEnum.RECUP.getValue()) {
			// juste pour afficher le nom de l'agent dans le formulaire
			setFormulaireRecup("Solde du compteur de récupération pour l'agent " + getAgentFiltre().getNom() + " "
					+ getAgentFiltre().getPrenom());
			FiltreSoldeDto filtreDto = new FiltreSoldeDto();
			filtreDto.setDateDebut(new Date());
			filtreDto.setDateFin(new Date());
			SoldeDto result = absWsConsumer.getAgentSolde(getAgentFiltre().getIdAgent(), filtreDto);
			setSoldeCourant(result);
		} 
	}

	@Command
	@NotifyChange({ "typeAbsenceFiltre", "serviceFiltre", "agentFiltre", "formulaireRecup", 
			"soldeCourant", "compteurACreer", "nouveauSoldeAnneePrec" })
	public void chargeFormulaire() {
		videFormulaireSansType();
		if (getTypeAbsenceFiltre().getIdRefTypeAbsence() == RefTypeAbsenceEnum.RECUP.getValue()) {
			setAnneePrec("0");
			if (getAgentFiltre() == null) {
				setFormulaireRecup("Solde du compteur de récupération pour l'agent ");
			} else {
				setFormulaireRecup("Solde du compteur de récupération pour l'agent " + getAgentFiltre().getNom() + " "
						+ getAgentFiltre().getPrenom());
				FiltreSoldeDto filtreDto = new FiltreSoldeDto();
				filtreDto.setDateDebut(new Date());
				filtreDto.setDateFin(new Date());
				SoldeDto result = absWsConsumer.getAgentSolde(getAgentFiltre().getIdAgent(), filtreDto);
				setSoldeCourant(result);
			}
			setCompteurACreer(new CompteurDto());

		}
	}

	public String soldeJour(Double solde) {
		if (solde == 0)
			return "aucun";
		return solde + " j";
	}

	public String soldeHeure(Double solde) {
		if (solde == 0)
			return "aucun";
		return getHeureMinute(solde.intValue());
	}

	private static String getHeureMinute(int nombreMinute) {
		int heure = nombreMinute / 60;
		int minute = nombreMinute % 60;
		String res = "";
		if (heure > 0)
			res += heure + "h";
		if (minute > 0)
			res += minute + "m";

		return res;
	}

	public String concatAgent(String nom, String prenom) {
		return nom + " " + prenom;
	}

	private void videFormulaireSansType() {
		setAnneePrec("0");
		setFormulaireRecup(null);
		setSoldeCourant(null);
		setCompteurACreer(null);
		setAgentFiltre(null);
		setServiceFiltre(null);
	}

	public DateTime getFirstDayOfCurrentMonth() {
		DateTime date = new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

		return date.dayOfMonth() // Accès à la propriété 'Jour du Mois'
				.withMinimumValue();
	}

	public List<RefTypeAbsenceDto> getListeTypeAbsenceFiltre() {
		return listeTypeAbsenceFiltre;
	}

	public void setListeTypeAbsenceFiltre(List<RefTypeAbsenceDto> listeTypeAbsenceFiltre) {
		this.listeTypeAbsenceFiltre = listeTypeAbsenceFiltre;
	}

	public RefTypeAbsenceDto getTypeAbsenceFiltre() {
		return typeAbsenceFiltre;
	}

	public void setTypeAbsenceFiltre(RefTypeAbsenceDto typeAbsenceFiltre) {
		this.typeAbsenceFiltre = typeAbsenceFiltre;
	}

	public List<EntiteDto> getListeServicesFiltre() {
		return listeServicesFiltre;
	}

	public void setListeServicesFiltre(List<EntiteDto> listeServicesFiltre) {
		if (null != listeServicesFiltre) {
			Collections.sort(listeServicesFiltre);
		}
		this.listeServicesFiltre = listeServicesFiltre;
	}

	public EntiteDto getServiceFiltre() {
		return serviceFiltre;
	}

	public void setServiceFiltre(EntiteDto serviceFiltre) {
		this.serviceFiltre = serviceFiltre;
	}

	public List<AgentDto> getListeAgentsFiltre() {
		return listeAgentsFiltre;
	}

	public void setListeAgentsFiltre(List<AgentDto> listeAgentsFiltre) {
		if (null != listeAgentsFiltre) {
			Collections.sort(listeAgentsFiltre);
		}
		this.listeAgentsFiltre = listeAgentsFiltre;
	}

	public AgentDto getAgentFiltre() {
		return agentFiltre;
	}

	public void setAgentFiltre(AgentDto agentFiltre) {
		this.agentFiltre = agentFiltre;
	}

	public String getFormulaireRecup() {
		return formulaireRecup;
	}

	public void setFormulaireRecup(String formulaireRecup) {
		this.formulaireRecup = formulaireRecup;
	}

	public SoldeDto getSoldeCourant() {
		return soldeCourant;
	}

	public void setSoldeCourant(SoldeDto soldeCourant) {
		this.soldeCourant = soldeCourant;
	}

	public CompteurDto getCompteurACreer() {
		return compteurACreer;
	}

	public void setCompteurACreer(CompteurDto compteurACreer) {
		this.compteurACreer = compteurACreer;
	}

	public String getAnneePrec() {
		return anneePrec;
	}

	public void setAnneePrec(String anneePrec) {
		this.anneePrec = anneePrec;
	}

	public SaisieGardeDto getSaisieGarde() {
		return saisieGarde;
	}

	public void setSaisieGarde(SaisieGardeDto saisieGarde) {
		this.saisieGarde = saisieGarde;
	}

	public List<Date> getListeMoisFiltre() {
		return listeMoisFiltre;
	}

	public void setListeMoisFiltre(List<Date> listeMoisFiltre) {
		this.listeMoisFiltre = listeMoisFiltre;
	}

	public Date getMoisFiltre() {
		return moisFiltre;
	}

	public void setMoisFiltre(Date moisFiltre) {
		this.moisFiltre = moisFiltre;
	}

	public List<String> getListVide() {
		return listVide;
	}

	public void setListVide(List<String> listVide) {
		this.listVide = listVide;
	}

	public String getNomatrAgent(Integer idAgent) {
		return "(" + idAgent.toString().substring(3, idAgent.toString().length()) + ")";
	}

}
