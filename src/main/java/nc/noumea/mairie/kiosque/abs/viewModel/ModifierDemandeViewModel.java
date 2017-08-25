package nc.noumea.mairie.kiosque.abs.viewModel;

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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

import nc.noumea.mairie.kiosque.abs.dto.DemandeDto;
import nc.noumea.mairie.kiosque.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.kiosque.abs.dto.PieceJointeDto;
import nc.noumea.mairie.kiosque.abs.dto.RefEtatEnum;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeAbsenceEnum;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeDto;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeSaisieCongeAnnuelEnum;
import nc.noumea.mairie.kiosque.dto.ReturnMessageAbsDto;
import nc.noumea.mairie.kiosque.dto.ReturnMessageDto;
import nc.noumea.mairie.kiosque.profil.dto.ProfilAgentDto;
import nc.noumea.mairie.kiosque.validation.ValidationMessage;
import nc.noumea.mairie.kiosque.viewModel.TimePicker;
import nc.noumea.mairie.ws.ISirhAbsWSConsumer;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ModifierDemandeViewModel {

	@WireVariable
	private ISirhAbsWSConsumer absWsConsumer;

	private DemandeDto demandeCourant;

	private List<OrganisationSyndicaleDto> listeOrganisationsSyndicale;

	private OrganisationSyndicaleDto organisationsSyndicaleCourant;
	
	private List<RefTypeDto> listeSiegeLesion;
	private List<DemandeDto> listeATReference;

	// pour savoir si la date de debut est le matin
	private String selectDebutAM;
	// pour savoir si la date de fin est le matin
	private String selectFinAM;
	private String dureeHeureDemande;
	private String dureeMinuteDemande;
	private String dureeCongeAnnuel;
	private String samediOffertCongeAnnuel;
	private String etatDemande;

	// POUR LA GESTION DES HEURES
	private List<String> listeHeures;
	private List<String> listeMinutes;

	private String heureDebut;
	private String minuteDebut;
	private String heureFin;
	private String minuteFin;
	
	private boolean saisieManuelleDuree;
	
	private SimpleDateFormat sdfddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");

	@AfterCompose
	public void doAfterCompose(@ExecutionArgParam("demandeCourant") DemandeDto demande) {
		// on recupere la demande selectionnée
		setDemandeCourant(demande);

		// on recharge les organisations syndicales
		List<OrganisationSyndicaleDto> orga = absWsConsumer.getListOrganisationSyndicale(getDemandeCourant()
				.getAgentWithServiceDto().getIdAgent(), getDemandeCourant().getIdTypeDemande());
		if (orga.size() == 0) {
			OrganisationSyndicaleDto dto = new OrganisationSyndicaleDto();
			dto.setLibelle("L'agent n'est affecté à ausune organisation syndicale");
			dto.setSigle("ERREUR");
			orga.add(dto);
		}
		setListeOrganisationsSyndicale(orga);
		setOrganisationsSyndicaleCourant(getDemandeCourant().getOrganisationSyndicale());

		// date AM/PM
		setSelectDebutAM(getDemandeCourant().isDateDebutAM() ? "AM" : "PM");
		setSelectFinAM(getDemandeCourant().isDateFinAM() ? "AM" : "PM");

		// durée congé annuel
		setDureeCongeAnnuel(getDureeHeureMinutes(getDemandeCourant().getDuree(), getDemandeCourant().getTypeSaisi(),
				getDemandeCourant().getTypeSaisiCongeAnnuel()).toString());
		// samedi offert
		setSamediOffertCongeAnnuel(getDemandeCourant().isSamediOffert() ? "Samedi offert : pris"
				: "Samedi offert : non pris");
		// etat
		setEtatDemande(getDemandeCourant().getIdRefEtat().toString());

		// minutes et heures
		TimePicker timePicker = new TimePicker();
		setListeMinutes(timePicker.getListeMinutes());
		setListeHeures(timePicker.getListeHeures());

		if (getDemandeCourant().getTypeSaisi() != null) {
			SimpleDateFormat heure = new SimpleDateFormat("HH");
			SimpleDateFormat minute = new SimpleDateFormat("mm");
			if (getDemandeCourant().getTypeSaisi().isCalendarHeureDebut()) {
				setHeureDebut(heure.format(getDemandeCourant().getDateDebut()));
				setMinuteDebut(minute.format(getDemandeCourant().getDateDebut()));
			}
			if (getDemandeCourant().getTypeSaisi().isCalendarHeureFin()) {
				setHeureFin(heure.format(getDemandeCourant().getDateFin()));
				setMinuteFin(minute.format(getDemandeCourant().getDateFin()));
			}
			// duree
			if (getDemandeCourant().getTypeSaisi().isDuree()) {
				int heureDuree = getDemandeCourant().getDuree().intValue() / 60;
				int minuteDuree = getDemandeCourant().getDuree().intValue() % 60;

				setDureeHeureDemande(String.valueOf(heureDuree));
				setDureeMinuteDemande(String.valueOf(minuteDuree));
			}
			
			// maladies
			if (getDemandeCourant() != null 
					&& getDemandeCourant().getTypeSaisi() != null){
				if(getDemandeCourant().getTypeSaisi().isSiegeLesion()) {
					setListeSiegeLesion(absWsConsumer.getListSiegeLesion());
				}
			}
			if (getDemandeCourant() != null 
					&& getDemandeCourant().getTypeSaisi() != null){
				if(getDemandeCourant().getTypeSaisi().isAtReference()) {
					List<DemandeDto> listATReference = absWsConsumer.getListeATReferenceForAgent(getDemandeCourant().getAgentWithServiceDto().getIdAgent());
					
					Collections.sort(listATReference);
					
					setListeATReference(listATReference);
				}
			}
		}
	}

	/**
	 * #40134 : Alimenter le nombre de jours d'ITT selon les règles spécifiées dans le redmine.
	 */
	@Command
	@NotifyChange({ "demandeCreation" })
	public void refreshNombreITT() {
		if (getDemandeCourant().getDateDebut() != null && getDemandeCourant().getDateFin() != null) {
			Long nbITT = null;
		    
			switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(getDemandeCourant().getIdTypeDemande())) {
			case ACCIDENT_TRAVAIL :
				nbITT = ChronoUnit.DAYS.between(getDemandeCourant().getDateDebut().toInstant(), getDemandeCourant().getDateFin().toInstant());
				// #40134 : Une prolongation fonctionne comme une rechute
				// Il faut donc ajouter une journée supplémentaire.
				if (getDemandeCourant().isProlongation())
					++nbITT;
				break;
			case RECHUTE_AT :
				nbITT = ChronoUnit.DAYS.between(getDemandeCourant().getDateDebut().toInstant(), getDemandeCourant().getDateFin().toInstant()) + 1;
				break;
			default:
				break;
			}
			
			// On n'autorise pas un nombre de jour négatif (dans le cas d'un AT sur une journée)
			if (nbITT != null) {
				nbITT = nbITT > 0 ? nbITT : 0;
				getDemandeCourant().setNombreITT((double) nbITT);
			}
		}
	}

	@Command
	@NotifyChange({ "dureeCongeAnnuel", "samediOffertCongeAnnuel" })
	public void refreshDuree() {
		getDemandeCourant().setDateDebutAM(
				getSelectDebutAM() == null ? false : getSelectDebutAM().equals("AM") ? true : false);
		getDemandeCourant().setDateDebutPM(
				getSelectDebutAM() == null ? false : getSelectDebutAM().equals("PM") ? true : false);
		getDemandeCourant().setDateFinAM(
				getSelectFinAM() == null ? false : getSelectFinAM().equals("AM") ? true : false);
		getDemandeCourant().setDateFinPM(
				getSelectFinAM() == null ? false : getSelectFinAM().equals("PM") ? true : false);
		setDureeCongeAnnuel(getCalculDureeCongeAnnuel(getDemandeCourant().getTypeSaisiCongeAnnuel()
				.getCodeBaseHoraireAbsence(), getDemandeCourant()));

		setSamediOffertCongeAnnuel(getSamediOffertDureeCongeAnnuel(getDemandeCourant().getTypeSaisiCongeAnnuel()
				.getCodeBaseHoraireAbsence(), getDemandeCourant()));
	}

	public String getCalculDureeCongeAnnuel(String codeBaseHoraireAbsence, DemandeDto demandeDto) {
		if (demandeDto.getDateDebut() != null && demandeDto.getDateFin() != null) {
			DemandeDto dureeDto = absWsConsumer.getDureeCongeAnnuel(demandeDto);
			return dureeDto.getDuree().toString();
		}
		return null;
	}

	private String getSamediOffertDureeCongeAnnuel(String codeBaseHoraireAbsence, DemandeDto demandeDto) {
		if (demandeDto.getDateDebut() != null && demandeDto.getDateFin() != null) {
			DemandeDto dureeDto = absWsConsumer.getDureeCongeAnnuel(demandeDto);
			if (dureeDto.isSamediOffert())
				return "Samedi offert";
			else
				return null;
		}
		return null;
	}

	private Double getDureeHeureMinutes(Double duree, RefTypeSaisiDto typeSaisi,
			RefTypeSaisiCongeAnnuelDto typeSaisiCongeAnnuel) {
		if (typeSaisi != null && typeSaisi.getUniteDecompte().equals("minutes")) {
			return duree / 60;
		}
		if (typeSaisiCongeAnnuel != null) {
			return duree;
		}
		return (double) 0;
	}

	@Command
	public void cancelDemande(@BindingParam("win") Window window) {
		refreshDemande();
		window.detach();
	}

	@Command
	public void saveDemande(@BindingParam("win") Window window) throws IOException {

		if (IsFormValid(getDemandeCourant().getTypeSaisi())) {

			if (getDemandeCourant().getTypeSaisi() != null) {
				if (getDemandeCourant().getTypeSaisi().isCalendarHeureDebut()) {
					// recup heure debut
					Calendar calDebut = Calendar.getInstance();
					calDebut.setTimeZone(TimeZone.getTimeZone("Pacific/Noumea"));
					calDebut.setTime(getDemandeCourant().getDateDebut());
					calDebut.set(Calendar.HOUR_OF_DAY, Integer.valueOf(getHeureDebut()));
					calDebut.set(Calendar.MINUTE, Integer.valueOf(getMinuteDebut()));
					calDebut.set(Calendar.SECOND, 0);

					getDemandeCourant().setDateDebut(calDebut.getTime());
				}
				if (getDemandeCourant().getTypeSaisi().isCalendarHeureFin()) {
					// recup heure fin
					Calendar calFin = Calendar.getInstance();
					calFin.setTimeZone(TimeZone.getTimeZone("Pacific/Noumea"));
					calFin.setTime(getDemandeCourant().getDateDebut());
					calFin.set(Calendar.HOUR_OF_DAY, Integer.valueOf(getHeureFin()));
					calFin.set(Calendar.MINUTE, Integer.valueOf(getMinuteFin()));
					calFin.set(Calendar.SECOND, 0);

					getDemandeCourant().setDateFin(calFin.getTime());
				}
				if (getDemandeCourant().getTypeSaisi().isDuree()) {
					String dureeTotale = getDureeHeureDemande()
							+ "."
							+ (getDureeMinuteDemande().length() == 1 ? "0" + getDureeMinuteDemande()
									: getDureeMinuteDemande());
					getDemandeCourant().setDuree(Double.valueOf(dureeTotale));
				}
			}

			getDemandeCourant().setIdRefEtat(Integer.valueOf(getEtatDemande()));
			getDemandeCourant().setOrganisationSyndicale(getOrganisationsSyndicaleCourant());
			getDemandeCourant().setDateDebutAM(
					getSelectDebutAM() == null ? false : getSelectDebutAM().equals("AM") ? true : false);
			getDemandeCourant().setDateDebutPM(
					getSelectDebutAM() == null ? false : getSelectDebutAM().equals("PM") ? true : false);
			getDemandeCourant().setDateFinAM(
					getSelectFinAM() == null ? false : getSelectFinAM().equals("AM") ? true : false);
			getDemandeCourant().setDateFinPM(
					getSelectFinAM() == null ? false : getSelectFinAM().equals("PM") ? true : false);

			if(getDemandeCourant().isForceSaisieManuelleDuree()) {
				try {
					getDemandeCourant().setDuree(new Double(getDureeCongeAnnuel()));
				} catch(java.lang.NumberFormatException e) {
					final HashMap<String, Object> map = new HashMap<String, Object>();
					List<ValidationMessage> listErreur = new ArrayList<ValidationMessage>();
					ValidationMessage vm = new ValidationMessage("Merci de saisir une durée valide.");
					listErreur.add(vm);
					map.put("errors", listErreur);
					map.put("infos", new ArrayList<ValidationMessage>());
					Executions.createComponents("/messages/returnMessage.zul", null, map);
					if (listErreur.size() == 0) {
						BindUtils.postGlobalCommand(null, null, "refreshListeDemande", null);
						window.detach();
					}
					return;
				}
			}
			
			ProfilAgentDto currentUser = (ProfilAgentDto) Sessions.getCurrent().getAttribute("currentUser");

			// sauvegarde de la demande sans creation de pj mais avec suppression de pj
			ReturnMessageAbsDto result = absWsConsumer.saveDemandeAbsence(currentUser.getAgent().getIdAgent(),
					getDemandeCourant());

			if (result.getErrors().size() > 0) {
				final HashMap<String, Object> map = new HashMap<String, Object>();
				List<ValidationMessage> listErreur = new ArrayList<ValidationMessage>();
				for (String error : result.getErrors()) {
					ValidationMessage vm = new ValidationMessage(error);
					listErreur.add(vm);
				}
				map.put("errors", listErreur);
				Executions.createComponents("/messages/returnMessage.zul", null, map);
				if (listErreur.size() == 0) {
					BindUtils.postGlobalCommand(null, null, "refreshListeDemande", null);
					window.detach();
				}
			}
			
			// puis on sauvegarde les pieces jointes si pas d erreur avant
			if(getDemandeCourant().getPiecesJointes() != null && !getDemandeCourant().getPiecesJointes().isEmpty()) {
				for (PieceJointeDto pj : getDemandeCourant().getPiecesJointes()) {
					
					if(null != pj.getNodeRefAlfresco()
							&& !"".equals(pj.getNodeRefAlfresco().trim())) {
						continue;
					}
					
					ReturnMessageDto resultPJ = new ReturnMessageDto();
					resultPJ = absWsConsumer.savePJWithInputStream(getDemandeCourant().getAgentWithServiceDto().getIdAgent(), 
							currentUser.getAgent().getIdAgent(), result.getIdDemande(), pj);
		
					pj.getFileInputStream().close();
					if (resultPJ.getErrors().size() > 0 || resultPJ.getInfos().size() > 0) {
						result.getInfos().addAll(resultPJ.getInfos());
						result.getErrors().addAll(resultPJ.getErrors());
					}
				}
			}
			
			// message de succes
			if (result.getInfos().size() > 0) {
				final HashMap<String, Object> map = new HashMap<String, Object>();
				List<ValidationMessage> listErreur = new ArrayList<ValidationMessage>();
				List<ValidationMessage> listInfo = new ArrayList<ValidationMessage>();
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
				if (listErreur.size() == 0) {
					BindUtils.postGlobalCommand(null, null, "refreshListeDemande", null);
					window.detach();
				}
			}
		}
	}

	@Command
	@NotifyChange({ "demandeCourant", "dureeCongeAnnuel" })
	public void alimenteDateFin() {
		if (null == getDemandeCourant().getDateFin()
				&& getDemandeCourant().getTypeSaisiCongeAnnuel().isCalendarDateFin()) {
			getDemandeCourant().setDateFin(getDemandeCourant().getDateDebut());
		}
		if (null == getDemandeCourant().getDateFin()
				&& getDemandeCourant().getTypeSaisiCongeAnnuel().isCalendarDateReprise()) {
			Calendar calReprise = Calendar.getInstance();
			calReprise.setTimeZone(TimeZone.getTimeZone("Pacific/Noumea"));
			calReprise.setTime(getDemandeCourant().getDateDebut());
			calReprise.add(Calendar.DAY_OF_MONTH, 1);
			getDemandeCourant().setDateReprise(calReprise.getTime());
			getDemandeCourant().setDateFin(calReprise.getTime());
		}
		refreshDuree();
		
		// #40183 : Calcul du nombre d'ITT pour un AT ou une rechute d'AT
		if (getDemandeCourant().getIdTypeDemande().equals(RefTypeAbsenceEnum.ACCIDENT_TRAVAIL.getValue()) 
				|| getDemandeCourant().getIdTypeDemande().equals(RefTypeAbsenceEnum.ACCIDENT_TRAVAIL.getValue()))
				refreshNombreITT();
	}
	
	@Command
	public void refreshDemande() {
		ProfilAgentDto currentUser = (ProfilAgentDto) Sessions.getCurrent().getAttribute("currentUser");
		DemandeDto demande = absWsConsumer.getDemande(currentUser.getAgent().getIdAgent(),
					getDemandeCourant().getIdDemande());
		
		getDemandeCourant().setDateDebut(demande.getDateDebut());
		getDemandeCourant().setDateDebutAM(demande.isDateDebutAM());
		getDemandeCourant().setDateDebutPM(demande.isDateDebutPM());
		getDemandeCourant().setDateFin(demande.getDateFin());
		getDemandeCourant().setDateFinAM(demande.isDateFinAM());
		getDemandeCourant().setDateFinPM(demande.isDateFinPM());
		getDemandeCourant().setDateReprise(demande.getDateReprise());
		getDemandeCourant().setCommentaire(demande.getCommentaire());
		getDemandeCourant().setDuree(demande.getDuree());
		getDemandeCourant().setIdRefEtat(demande.getIdRefEtat());
		getDemandeCourant().setMotif(demande.getMotif());
		getDemandeCourant().setPiecesJointes(demande.getPiecesJointes());
	}

	private boolean IsFormValid(RefTypeSaisiDto typeSaisie) {

		List<ValidationMessage> vList = new ArrayList<ValidationMessage>();

		// Date de debut
		if (getDemandeCourant().getDateDebut() == null) {
			vList.add(new ValidationMessage("La date de début est obligatoire."));
		}
		if (getDemandeCourant().getTypeSaisi() != null) {
			if (typeSaisie.isChkDateDebut()) {
				if (getSelectDebutAM() == null) {
					vList.add(new ValidationMessage("Merci de choisir M/A pour la date de début."));
				}
			}
			// heure debut
			if (typeSaisie.isCalendarHeureDebut()) {
				if (getHeureDebut() == null || getMinuteDebut() == null) {
					vList.add(new ValidationMessage("Merci de choisir une heure de début."));
				}
			}
			// heure fin
			if (typeSaisie.isCalendarHeureFin()) {
				if (getHeureFin() == null || getMinuteFin() == null) {
					vList.add(new ValidationMessage("Merci de choisir une heure de fin."));
				}
			}

			// OS
			if (typeSaisie.isCompteurCollectif()) {
				if (getOrganisationsSyndicaleCourant() == null) {
					vList.add(new ValidationMessage("L'organisation syndicale est obligatoire."));
				}
			}

			// DUREE
			if (typeSaisie.isDuree()) {
				if (getDureeHeureDemande() == null || getDureeMinuteDemande() == null) {
					vList.add(new ValidationMessage("La durée est obligatoire."));
				}
			}

			// DATE FIN
			if (typeSaisie.isCalendarDateFin()) {
				if (getDemandeCourant().getDateFin() == null) {
					vList.add(new ValidationMessage("La date de fin est obligatoire."));
				}
			}
			if (typeSaisie.isChkDateFin()) {
				if (getSelectFinAM() == null) {
					vList.add(new ValidationMessage("Merci de choisir M/A pour la date de fin."));
				}
			}

			// MOTIF
			if (typeSaisie.isMotif()) {
				if (getDemandeCourant().getCommentaire() == null) {
					vList.add(new ValidationMessage("Le commentaire est obligatoire."));
				}
			}

			// Prescripteur
			if (getDemandeCourant().getTypeSaisi().isPrescripteur()) {
				if (StringUtils.isBlank(getDemandeCourant().getPrescripteur())) {
					vList.add(new ValidationMessage("Le prescripteur est obligatoire."));
				}
			}
		} else if (getDemandeCourant().getTypeSaisiCongeAnnuel() != null) {
			if (getDemandeCourant().getTypeSaisiCongeAnnuel().isChkDateDebut()) {
				if (getSelectDebutAM() == null) {
					vList.add(new ValidationMessage("Merci de choisir M/A pour la date de début."));
				}
			}

			// MOTIF
			if (getDemandeCourant().getTypeSaisiCongeAnnuel().isMotif()) {
				if (getDemandeCourant().getCommentaire() == null) {
					vList.add(new ValidationMessage("Le commentaire est obligatoire."));
				}
			}

			// DATE FIN
			if (getDemandeCourant().getTypeSaisiCongeAnnuel().isCalendarDateFin()) {
				if (getDemandeCourant().getDateFin() == null) {
					vList.add(new ValidationMessage("La date de fin est obligatoire."));
				}
			}
			if (getDemandeCourant().getTypeSaisiCongeAnnuel().isChkDateFin()) {
				if (getSelectFinAM() == null) {
					vList.add(new ValidationMessage("Merci de choisir M/A pour la date de fin."));
				}
			}

			// DATE REPRISE
			if (getDemandeCourant().getTypeSaisiCongeAnnuel().isCalendarDateReprise()) {
				if (getDemandeCourant().getDateReprise() == null) {
					vList.add(new ValidationMessage("La date de reprise est obligatoire."));
				}
			}
		} else {
			vList.add(new ValidationMessage(
					"Une erreur est survenue dans l'enregistrement de la demande. Merci de recommencer."));
		}

		if (vList.size() > 0) {
			final HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("errors", vList);
			Executions.createComponents("/messages/returnMessage.zul", null, map);
			return false;
		} else
			return true;
	}
	
	@Command
	@NotifyChange("saisieManuelleDuree")
	public void forcerSaisieDuree() {
		setSaisieManuelleDuree(true);
		getDemandeCourant().setForceSaisieManuelleDuree(true);
	}
	
	public boolean getAfficherBoutonForcerDuree() {
		
		ProfilAgentDto currentUser = (ProfilAgentDto) Sessions.getCurrent().getAttribute("currentUser");
		
		if(null != getDemandeCourant()
				&& null != getDemandeCourant().getTypeSaisiCongeAnnuel()
				&& null != getDemandeCourant().getTypeSaisiCongeAnnuel().getCodeBaseHoraireAbsence()
				&& getDemandeCourant().getTypeSaisiCongeAnnuel().getCodeBaseHoraireAbsence().equals(RefTypeSaisieCongeAnnuelEnum.C.getCodeBase())
				&& !currentUser.getAgent().getIdAgent().equals(getDemandeCourant().getAgentWithServiceDto().getIdAgent())) {
			return true;
		}
		return false;
	}

	public String afficheATReference(DemandeDto demandeAT) {
		
		String result = "";
		
		if(null != demandeAT.getDateDeclaration()) {
			result += sdfddMMyyyy.format(demandeAT.getDateDeclaration()) + " - ";
		}
		if(null != demandeAT.getTypeSiegeLesion()) {
			result += demandeAT.getTypeSiegeLesion().getLibelle();
		}
		
		return result;
	}

	@Command
	@NotifyChange("demandeCourant")
	public void onUploadPDF(
			@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx)
			throws IOException {

		UploadEvent upEvent = null;
		Object objUploadEvent = ctx.getTriggerEvent();
		if (objUploadEvent != null && (objUploadEvent instanceof UploadEvent)) {
			upEvent = (UploadEvent) objUploadEvent;
		}
		if (upEvent != null
				&& null != upEvent.getMedias()) {
			for(Media media : upEvent.getMedias()) {
				
				PieceJointeDto pj = new PieceJointeDto();
				pj.setTypeFile(media.getContentType());
				//pj.setbFile(media.getByteData());
				// bug #30020
				pj.setTitre(media.getName());
				// #37756 : Upload via stream
				pj.setFileInputStream(media.getStreamData());
				
				getDemandeCourant().getPiecesJointes().add(pj);
			}
		}
	}

	@Command
	@NotifyChange("demandeCourant")
	public void supprimerPieceJointe(
			@BindingParam("ref") PieceJointeDto pieceJointeDto)
			throws IOException {

		if(null != getDemandeCourant().getPiecesJointes()
				&& !getDemandeCourant().getPiecesJointes().isEmpty()
				&& getDemandeCourant().getPiecesJointes().contains(pieceJointeDto)) {
			getDemandeCourant().getPiecesJointes().remove(pieceJointeDto);
		}
	}

	public DemandeDto getDemandeCourant() {
		return demandeCourant;
	}

	public void setDemandeCourant(DemandeDto demandeCourant) {
		this.demandeCourant = demandeCourant;
	}

	public List<OrganisationSyndicaleDto> getListeOrganisationsSyndicale() {
		return listeOrganisationsSyndicale;
	}

	public void setListeOrganisationsSyndicale(List<OrganisationSyndicaleDto> listeOrganisationsSyndicale) {
		this.listeOrganisationsSyndicale = listeOrganisationsSyndicale;
	}

	public OrganisationSyndicaleDto getOrganisationsSyndicaleCourant() {
		return organisationsSyndicaleCourant;
	}

	public void setOrganisationsSyndicaleCourant(OrganisationSyndicaleDto organisationsSyndicaleCourant) {
		this.organisationsSyndicaleCourant = organisationsSyndicaleCourant;
	}

	public String getSelectDebutAM() {
		return selectDebutAM;
	}

	public void setSelectDebutAM(String selectDebutAM) {
		this.selectDebutAM = selectDebutAM;
	}

	public String getSelectFinAM() {
		return selectFinAM;
	}

	public void setSelectFinAM(String selectFinAM) {
		this.selectFinAM = selectFinAM;
	}

	public String getEtatDemande() {
		// bug #30012
		if(demandeCourant.getGroupeAbsence().getIdRefGroupeAbsence()==RefTypeGroupeAbsenceEnum.MALADIES.getValue()
				&& etatDemande.equals(new Integer(RefEtatEnum.A_VALIDER.getCodeEtat()).toString())) {
			return new Integer(RefEtatEnum.SAISIE.getCodeEtat()).toString();
		}
		return etatDemande;
	}

	public void setEtatDemande(String etatDemande) {
		this.etatDemande = etatDemande;
	}

	public String getDureeCongeAnnuel() {
		return dureeCongeAnnuel;
	}

	public void setDureeCongeAnnuel(String dureeCongeAnnuel) {
		this.dureeCongeAnnuel = dureeCongeAnnuel;
	}

	public List<String> getListeHeures() {
		return listeHeures;
	}

	public void setListeHeures(List<String> listeHeures) {
		this.listeHeures = listeHeures;
	}

	public List<String> getListeMinutes() {
		return listeMinutes;
	}

	public void setListeMinutes(List<String> listeMinutes) {
		this.listeMinutes = listeMinutes;
	}

	public String getHeureDebut() {
		return heureDebut;
	}

	public void setHeureDebut(String heureDebut) {
		this.heureDebut = heureDebut;
	}

	public String getMinuteDebut() {
		return minuteDebut;
	}

	public void setMinuteDebut(String minuteDebut) {
		this.minuteDebut = minuteDebut;
	}

	public String getHeureFin() {
		return heureFin;
	}

	public void setHeureFin(String heureFin) {
		this.heureFin = heureFin;
	}

	public String getMinuteFin() {
		return minuteFin;
	}

	public void setMinuteFin(String minuteFin) {
		this.minuteFin = minuteFin;
	}

	public String getDureeHeureDemande() {
		return dureeHeureDemande;
	}

	public void setDureeHeureDemande(String dureeHeureDemande) {
		this.dureeHeureDemande = dureeHeureDemande;
	}

	public String getDureeMinuteDemande() {
		return dureeMinuteDemande;
	}

	public void setDureeMinuteDemande(String dureeMinuteDemande) {
		this.dureeMinuteDemande = dureeMinuteDemande;
	}

	public String getSamediOffertCongeAnnuel() {
		return samediOffertCongeAnnuel;
	}

	public void setSamediOffertCongeAnnuel(String samediOffertCongeAnnuel) {
		this.samediOffertCongeAnnuel = samediOffertCongeAnnuel;
	}

	public boolean isSaisieManuelleDuree() {
		return saisieManuelleDuree;
	}

	public void setSaisieManuelleDuree(boolean saisieManuelleDuree) {
		this.saisieManuelleDuree = saisieManuelleDuree;
	}

	public List<RefTypeDto> getListeSiegeLesion() {
		return listeSiegeLesion;
	}

	public void setListeSiegeLesion(List<RefTypeDto> listeSiegeLesion) {
		this.listeSiegeLesion = listeSiegeLesion;
	}

	public List<DemandeDto> getListeATReference() {
		return listeATReference;
	}

	public void setListeATReference(List<DemandeDto> listeATReference) {
		this.listeATReference = listeATReference;
	}
	
	public boolean isMaladie(){
		if(getDemandeCourant().getGroupeAbsence()==null || getDemandeCourant().getGroupeAbsence().getIdRefGroupeAbsence()==null){
			return false;
		}else if(getDemandeCourant().getGroupeAbsence().getIdRefGroupeAbsence()==RefTypeGroupeAbsenceEnum.MALADIES.getValue()){
			return true;
		}
		return false;
	}
	
}
