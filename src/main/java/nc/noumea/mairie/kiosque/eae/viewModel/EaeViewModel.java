package nc.noumea.mairie.kiosque.eae.viewModel;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.noumea.mairie.kiosque.travail.dto.FichePosteDto;
import nc.noumea.mairie.ws.ISirhWSConsumer;
import org.apache.commons.lang.StringUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Div;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;

import nc.noumea.mairie.kiosque.dto.ReturnMessageDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeAppreciationDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeAutoEvaluationDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeCommentaireDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeDeveloppementDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeEtatEnum;
import nc.noumea.mairie.kiosque.eae.dto.EaeEvaluationDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeEvolutionDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeEvolutionSouhaitDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeFichePosteDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeIdentificationDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeItemPlanActionDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeListItemDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeObjectifProDto;
import nc.noumea.mairie.kiosque.eae.dto.EaePlanActionDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeResultatDto;
import nc.noumea.mairie.kiosque.eae.dto.EaeResultatsDto;
import nc.noumea.mairie.kiosque.profil.dto.ProfilAgentDto;
import nc.noumea.mairie.kiosque.validation.ValidationMessage;
import nc.noumea.mairie.kiosque.viewModel.TimePicker;
import nc.noumea.mairie.ws.ISirhEaeWSConsumer;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EaeViewModel {

	private ProfilAgentDto			currentUser;

	@WireVariable
	private ISirhEaeWSConsumer		eaeWsConsumer;

	@WireVariable
	private ISirhWSConsumer 		sirhWsConsumer;

	private EaeListItemDto			eaeCourant;

	private Tab						tabCourant;

	private Tabbox					tabboxCourant;

	private EaeIdentificationDto	identification;

	private List<EaeFichePosteDto>	listeFichePoste;

	private EaeFichePosteDto		fichePostePrimaire;

	private EaeFichePosteDto		fichePosteSecondaire;

	private FichePosteDto			sirhWsFichePostePrimaire;

	private FichePosteDto			sirhWsFichePosteSecondaire;

	private EaeResultatsDto			resultat;

	private EaeAppreciationDto		appreciationAnnee;

	private EaeAppreciationDto		appreciationAnneePrec;

	private Integer					annee;

	private Integer					anneePrec;

	private EaeEvaluationDto		evaluation;

	private EaeAutoEvaluationDto	autoEvaluation;

	private EaePlanActionDto		planAction;

	private EaeEvolutionDto			evolution;

	private List<Integer>			listePriorisationEvolution;

	// POUR LA GESTION DE LA DUREE DE L'ENTRETIEN
	private List<String>			listeHeures;
	private List<String>			listeMinutes;

	private String					heureDuree;
	private String					minuteDuree;

	/* Pour savoir si on est en modif ou en visu */
	private String					modeSaisi;
	private boolean					isModification;
	/* Pour savoir si on affiche la disquette de sauvegarde */
	private boolean					hasTextChangedIdentification;
	private boolean					hasTextChangedFichePoste;
	private boolean					hasTextChangedResultat;
	private boolean					hasTextChangedAppreciation;
	private boolean					hasTextChangedEvaluation;
	private boolean					hasTextChangedAutoEvaluation;
	private boolean					hasTextChangedPlanAction;
	private boolean					hasTextChangedEvolution;

	/* Pour la finalisation */
	@SuppressWarnings("unused")
	private boolean					finalisationPossible;
	@SuppressWarnings("unused")
	private String					titreBadFinalisation;

	@Init
	public void initEae(@ExecutionArgParam("eae") EaeListItemDto eae, @ExecutionArgParam("mode") String modeSaisi) {

		currentUser = (ProfilAgentDto) Sessions.getCurrent().getAttribute("currentUser");
		setEaeCourant(eae);

		setModeSaisi(modeSaisi);
		setModification(modeSaisi.equals("EDIT"));

		// on charge l'identification
		initIdentification();
		// on charge les fiches de poste
		initFichePoste();
		// on charge les resultats
		initResultat();
		// on charge les appreciations
		initAppreciation();
		// on charge l'évaluation
		initEvaluation();
		// on charge l'auto-évaluation
		initAutoEvaluation();
		// on charge le plan d'action
		initPlanAction();
		// on charge l'evolution
		initEvolution();
	}

	private void initEvolution() {
		EaeEvolutionDto evo = eaeWsConsumer.getEvolutionEae(getEaeCourant().getIdEae(), currentUser.getAgent().getIdAgent());
		if(evo.getCommentaireEvalue()==null){
			evo.setCommentaireEvalue(new EaeCommentaireDto());			
		}
		if(evo.getCommentaireEvolution()==null){
			evo.setCommentaireEvolution(new EaeCommentaireDto());			
		}
		if(evo.getCommentaireEvaluateur()==null){
			evo.setCommentaireEvaluateur(new EaeCommentaireDto());			
		}
		setEvolution(evo);
		// on charge les priorisations
		Integer tailleDeveloppement = getEvolution().getDeveloppementConnaissances().size() + getEvolution().getDeveloppementCompetences().size()
				+ getEvolution().getDeveloppementExamensConcours().size() + getEvolution().getDeveloppementPersonnel().size()
				+ getEvolution().getDeveloppementComportement().size();
		List<Integer> temp = new ArrayList<>();
		for (int i = 1; i <= tailleDeveloppement; i++) {
			temp.add(i);
		}
		setListePriorisationEvolution(temp);
	}

	private void initPlanAction() {
		// pour le moment ca plante car on a developé des nouveaux DTO qui ne
		// sont pas encore en place en intégration/recette/prod
		// cd redmine #13840
		EaePlanActionDto plan = eaeWsConsumer.getPlanActionEae(getEaeCourant().getIdEae(), currentUser.getAgent().getIdAgent());
		for (EaeObjectifProDto objPro : plan.getObjectifsProfessionnels()) {
			if (objPro.getIndicateur() == null)
				objPro.setIndicateur("");
			if (objPro.getObjectif() == null)
				objPro.setObjectif("");
		}
		setPlanAction(plan);
	}

	private void initAutoEvaluation() {
		EaeAutoEvaluationDto autoEvaluation = eaeWsConsumer.getAutoEvaluationEae(getEaeCourant().getIdEae(), currentUser.getAgent().getIdAgent());
		setAutoEvaluation(autoEvaluation);
	}

	private void initEvaluation() {
		EaeEvaluationDto evaluation = eaeWsConsumer.getEvaluationEae(getEaeCourant().getIdEae(), currentUser.getAgent().getIdAgent());
		setEvaluation(evaluation);

		// minutes et heures
		TimePicker timePicker = new TimePicker();
		setListeMinutes(timePicker.getListeMinutes());
		setListeHeures(timePicker.getListeHeuresEaeDureeEntretien());
		if (getEvaluation().getDureeEntretien() != null) {
			if (getEvaluation().getDureeEntretien() != null) {
				String heure = "" + getEvaluation().getDureeEntretien() / 60;
				String minute = "" + getEvaluation().getDureeEntretien() % 60;
				if (heure.length() == 1) {
					heure = "0" + heure;
				}
				if (minute.length() == 1) {
					minute = "0" + minute;
				}
				setHeureDuree(heure);
				setMinuteDuree(minute);
			}
		}
	}

	private void initAppreciation() {
		EaeAppreciationDto appreciationAnnee = eaeWsConsumer.getAppreciationEae(getEaeCourant().getIdEae(), currentUser.getAgent().getIdAgent(),
				eaeWsConsumer.getCampagneEae().getAnnee());
		setAppreciationAnnee(appreciationAnnee);
		// on charge les appreciations de l'annéee précédente
		EaeAppreciationDto appreciationAnneePrec = eaeWsConsumer.getAppreciationEae(getEaeCourant().getIdEae(), currentUser.getAgent().getIdAgent(),
				eaeWsConsumer.getCampagneEae().getAnnee() - 1);
		setAppreciationAnneePrec(appreciationAnneePrec);

		setAnnee(eaeWsConsumer.getCampagneEae().getAnnee());
		setAnneePrec(eaeWsConsumer.getCampagneEae().getAnnee() - 1);
	}

	private void initResultat() {
		EaeResultatsDto resultat = eaeWsConsumer.getResultatEae(getEaeCourant().getIdEae(), currentUser.getAgent().getIdAgent());
		for (EaeResultatDto objIndi : resultat.getObjectifsIndividuels()) {
			if (objIndi.getCommentaire() == null)
				objIndi.setCommentaire(new EaeCommentaireDto(""));
			if (objIndi.getObjectif() == null)
				objIndi.setObjectif("");
			if (objIndi.getResultat() == null)
				objIndi.setResultat("");
		}
		for (EaeResultatDto objPro : resultat.getObjectifsProfessionnels()) {
			if (objPro.getCommentaire() == null)
				objPro.setCommentaire(new EaeCommentaireDto(""));
			if (objPro.getObjectif() == null)
				objPro.setObjectif("");
			if (objPro.getResultat() == null)
				objPro.setResultat("");
		}
		setResultat(resultat);
	}

	private void initFichePoste() {
		List<EaeFichePosteDto> listeFDP = eaeWsConsumer.getListeFichePosteEae(getEaeCourant().getIdEae(), currentUser.getAgent().getIdAgent());
		setListeFichePoste(listeFDP);
		if (getListeFichePoste().size() == 1) {
			FichePosteDto fpPrimaire = sirhWsConsumer.getFichePoste(getListeFichePoste().get(0).getIdAgentShd());
			setSirhWsFichePostePrimaire(fpPrimaire);
			setFichePostePrimaire(getListeFichePoste().get(0));
		} else if (getListeFichePoste().size() == 2) {
			FichePosteDto fpPrimaire = sirhWsConsumer.getFichePoste(getListeFichePoste().get(0).getIdAgentShd());
			setSirhWsFichePostePrimaire(fpPrimaire);
			setFichePostePrimaire(getListeFichePoste().get(0));
			FichePosteDto fpSecondaire = sirhWsConsumer.getFichePoste(getListeFichePoste().get(1).getIdAgentShd());
			setSirhWsFichePosteSecondaire(fpSecondaire);
			setFichePosteSecondaire(getListeFichePoste().get(1));
		}
	}

	private void initIdentification() {
		EaeIdentificationDto identification = eaeWsConsumer.getIdentificationEae(getEaeCourant().getIdEae(), currentUser.getAgent().getIdAgent());
		setIdentification(identification);
	}

	@GlobalCommand
	@Command
	@NotifyChange({ "identification", "resultat", "appreciationAnnee", "evaluation", "autoEvaluation", "planAction", "evolution",
			"hasTextChangedAppreciation", "hasTextChangedAutoEvaluation", "hasTextChangedEvaluation", "hasTextChangedEvolution",
			"hasTextChangedFichePoste", "hasTextChangedIdentification", "hasTextChangedPlanAction", "hasTextChangedResultat", "finalisationPossible",
			"titreBadFinalisation" })
	public void engistreOnglet() {
		// on sauvegarde l'onglet
		ReturnMessageDto result = new ReturnMessageDto();
		if (getTabCourant().getId().equals("IDENTIFICATION")) {
			result = isFormIdentificationValid(result, getIdentification());
			if (result.getErrors().size() == 0) {
				result = eaeWsConsumer.saveIdentification(getIdentification().getIdEae(), currentUser.getAgent().getIdAgent(), getIdentification());
			}
		} else if (getTabCourant().getId().equals("RESULTAT")) {
			result = isFormResultatValid(result, getResultat());
			if (result.getErrors().size() == 0) {
				result = eaeWsConsumer.saveResultat(getResultat().getIdEae(), currentUser.getAgent().getIdAgent(), getResultat());
			}
		} else if (getTabCourant().getId().equals("APPRECIATION")) {
			result = eaeWsConsumer.saveAppreciation(getResultat().getIdEae(), currentUser.getAgent().getIdAgent(), getAppreciationAnnee());
		} else if (getTabCourant().getId().equals("EVALUATION")) {
			result = isFormEvaluationValid(result, getEvaluation());
			if (result.getErrors().size() == 0) {
				// on construit la durée de l'entretien
				Integer res = (Integer.valueOf(getHeureDuree()) * 60) + Integer.valueOf(getMinuteDuree());
				getEvaluation().setDureeEntretien(res);
				result = eaeWsConsumer.saveEvaluation(getResultat().getIdEae(), currentUser.getAgent().getIdAgent(), getEvaluation());
			}
		} else if (getTabCourant().getId().equals("AUTOEVALUATION")) {
			result = eaeWsConsumer.saveAutoEvaluation(getResultat().getIdEae(), currentUser.getAgent().getIdAgent(), getAutoEvaluation());
		} else if (getTabCourant().getId().equals("PLANACTION")) {
			result = isFormPlanActionValid(result, getPlanAction());
			if (result.getErrors().size() == 0) {
				result = eaeWsConsumer.savePlanAction(getResultat().getIdEae(), currentUser.getAgent().getIdAgent(), getPlanAction());
			}
		} else if (getTabCourant().getId().equals("EVOLUTION")) {
			result = isFormEvolutionValid(result, getEvolution());
			if (result.getErrors().size() == 0) {
				result = eaeWsConsumer.saveEvolution(getResultat().getIdEae(), currentUser.getAgent().getIdAgent(), getEvolution());
			}
		}

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
			setHasTextChangedIdentification(false);
			setHasTextChangedAppreciation(false);
			setHasTextChangedAutoEvaluation(false);
			setHasTextChangedEvaluation(false);
			setHasTextChangedEvolution(false);
			setHasTextChangedFichePoste(false);
			setHasTextChangedPlanAction(false);
			setHasTextChangedResultat(false);

			// on recharge l'eae pour vider les eventuelles modifs
			initEae(getEaeCourant(), getModeSaisi());
		}
	}

	private ReturnMessageDto isFormEvaluationValid(ReturnMessageDto result, EaeEvaluationDto evaluation) {
		if (getHeureDuree() == null || getMinuteDuree() == null) {
			result.getErrors().add("La durée de l'entretien annuel d'échange ne doit pas être vide.");
		} else if (getHeureDuree().equals("00") && getMinuteDuree().equals("00")) {
			result.getErrors().add("La durée de l'entretien annuel d'échange ne doit pas être égale à zéro.");
		}
		// Si statut = F et type AD alors le rapport circonstancié obligatoire
		// si mini ou maxi si moyen alors il doit être vide.
		if (evaluation.getTypeAvct() != null && evaluation.getStatut().equals("F") && evaluation.getTypeAvct().equals("AD")) {
			if ((evaluation.getPropositionAvancement().getCourant().equals("MINI")
					|| evaluation.getPropositionAvancement().getCourant().equals("MAXI"))
					&& (evaluation.getCommentaireAvctEvaluateur() == null || StringUtils.isBlank(evaluation.getCommentaireAvctEvaluateur().getText()))) {
				result.getErrors().add("Le contenu du rapport circonstancié ne doit pas être vide pour une durée d'avancement minimum ou maximum.");
			} else if (evaluation.getPropositionAvancement().getCourant().equals("MOY") && evaluation.getCommentaireAvctEvaluateur() != null && StringUtils.isNotBlank(evaluation.getCommentaireAvctEvaluateur().getText())) {
				result.getErrors().add("Le contenu du rapport circonstancié ne doit pas être rempli pour une durée d'avancement moyenne.");
			}

		}
		return result;
	}

	private ReturnMessageDto isFormEvolutionValid(ReturnMessageDto result, EaeEvolutionDto evolution) {
		for (EaeDeveloppementDto conn : evolution.getDeveloppementConnaissances()) {
			if (conn.getEcheance() == null || conn.getLibelle() == null || conn.getPriorisation() == null) {
				String message = "Veuillez remplir tous les champs pour vos besoins en formation.";
				if (!result.getErrors().contains(message))
					result.getErrors().add(message);
			}
		}
		for (EaeDeveloppementDto conn : evolution.getDeveloppementCompetences()) {
			if (conn.getEcheance() == null || conn.getLibelle() == null || conn.getPriorisation() == null) {
				String message = "Veuillez remplir tous les champs pour vos besoins en formation.";
				if (!result.getErrors().contains(message))
					result.getErrors().add(message);
			}
		}
		for (EaeDeveloppementDto conn : evolution.getDeveloppementExamensConcours()) {
			if (conn.getEcheance() == null || conn.getLibelle() == null || conn.getPriorisation() == null) {
				String message = "Veuillez remplir tous les champs pour vos besoins en formation.";
				if (!result.getErrors().contains(message))
					result.getErrors().add(message);
			}
		}
		for (EaeDeveloppementDto conn : evolution.getDeveloppementPersonnel()) {
			if (conn.getEcheance() == null || conn.getLibelle() == null || conn.getPriorisation() == null) {
				String message = "Veuillez remplir tous les champs pour vos besoins en formation.";
				if (!result.getErrors().contains(message))
					result.getErrors().add(message);
			}
		}
		for (EaeDeveloppementDto conn : evolution.getDeveloppementComportement()) {
			if (conn.getEcheance() == null || conn.getLibelle() == null || conn.getPriorisation() == null) {
				String message = "Veuillez remplir tous les champs pour vos besoins en formation.";
				if (!result.getErrors().contains(message))
					result.getErrors().add(message);
			}
		}
		for (EaeDeveloppementDto conn : evolution.getDeveloppementFormateur()) {
			if (conn.getLibelle() == null) {
				String message = "Veuillez remplir tous les champs pour vos besoins en formation.";
				if (!result.getErrors().contains(message))
					result.getErrors().add(message);
			}
		}
		if (evolution.isMobiliteCollectivite() && (evolution.getNomCollectivite() == null || evolution.getNomCollectivite().equals(""))) {
			result.getErrors().add("Le champ de mobilité au sein de la collectivité doit être rempli.");
		}
		if (evolution.isConcours() && (evolution.getNomConcours() == null || evolution.getNomConcours().equals(""))) {
			result.getErrors().add("L'intitulé du concours ou de l'examen doit être rempli.");
		}
		if (evolution.isVae() && (evolution.getNomVae() == null || evolution.getNomVae().equals(""))) {
			result.getErrors().add("L'intitulé du diplôme doit être rempli.");
		}
		if (evolution.isTempsPartiel() && evolution.getPourcentageTempsPartiel().getCourant() == null) {
			result.getErrors().add("Le pourcentage de temps partiel doit être rempli.");
		}
		if (evolution.isRetraite() && evolution.getDateRetraite() == null) {
			result.getErrors().add("La date de départ en retraite doit être remplie.");
		}
		if (evolution.isAutrePerspective() && (evolution.getLibelleAutrePerspective() == null || evolution.getLibelleAutrePerspective().equals(""))) {
			result.getErrors().add("Le champ 'autres perspective' doit être renseigné.");
		}

		// reste à checker les priorités
		// on regroupe tous les types de besoin en formation pour checker les
		// priorités
		List<EaeDeveloppementDto> listeBesoinFormation = new ArrayList<EaeDeveloppementDto>();
		listeBesoinFormation.addAll(evolution.getDeveloppementConnaissances());
		listeBesoinFormation.addAll(evolution.getDeveloppementCompetences());
		listeBesoinFormation.addAll(evolution.getDeveloppementExamensConcours());
		listeBesoinFormation.addAll(evolution.getDeveloppementPersonnel());
		listeBesoinFormation.addAll(evolution.getDeveloppementComportement());
		for (EaeDeveloppementDto besoinF : listeBesoinFormation) {
			for (EaeDeveloppementDto besoinF2 : listeBesoinFormation) {
				if (besoinF2.getPriorisation() == besoinF.getPriorisation() && besoinF2 != besoinF) {
					String message = "Attention, plusieurs besoins en formation ont la même priorité. Veuillez rectifier les priorités de vos besoins en formation avant d'enregistrer.";
					if (!result.getErrors().contains(message))
						result.getErrors().add(message);
					break;
				}
			}
		}

		return result;
	}

	private ReturnMessageDto isFormPlanActionValid(ReturnMessageDto result, EaePlanActionDto planAction) {
		for (EaeObjectifProDto objPro : planAction.getObjectifsProfessionnels()) {
			if (!objPro.getIndicateur().equals("") && objPro.getObjectif().equals("")) {
				result.getErrors().add("L'objectif professionnel est obligatoire.");
			}
		}
		return result;
	}

	private ReturnMessageDto isFormResultatValid(ReturnMessageDto result, EaeResultatsDto resultatDto) {
		for (EaeResultatDto objIndiv : resultatDto.getObjectifsIndividuels()) {
			boolean resultIndiv = true;
			if (objIndiv.getResultat() != null) {
				if (!objIndiv.getResultat().equals("") && (objIndiv.getObjectif() == null || objIndiv.getObjectif().equals(""))) {
					resultIndiv = false;
				}
			}
			if (objIndiv.getCommentaire() != null) {
				if (!objIndiv.getCommentaire().equals("") && (objIndiv.getObjectif() == null || objIndiv.getObjectif().equals(""))) {
					resultIndiv = false;
				}
			}
			if (!resultIndiv) {
				result.getErrors().add("L'objectif de progrès individuel est obligatoire.");
			}
		}
		for (EaeResultatDto objPro : resultatDto.getObjectifsProfessionnels()) {
			boolean resultPro = true;
			if (objPro.getResultat() != null) {
				if (!objPro.getResultat().equals("") && (objPro.getObjectif() == null || objPro.getObjectif().equals(""))) {
					resultPro = false;
				}
			}
			if (objPro.getCommentaire() != null) {
				if (!objPro.getCommentaire().equals("") && (objPro.getObjectif() == null || objPro.getObjectif().equals(""))) {
					resultPro = false;
				}
			}
			if (!resultPro) {
				result.getErrors().add("L'objectif professionnel est obligatoire.");
			}
		}
		return result;
	}

	private ReturnMessageDto isFormIdentificationValid(ReturnMessageDto result, EaeIdentificationDto eaeIdentificationDto) {
		if (eaeIdentificationDto.getDateEntretien() == null) {
			result.getErrors().add("La date de l'entretien annuel d'échange ne doit pas être vide.");
		}
		return result;
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution", "listePriorisationEvolution" })
	public void supprimerLigneDeveloppementExamensConcours(@BindingParam("ref") EaeDeveloppementDto developpement) {
		if (getEvolution().getDeveloppementExamensConcours().contains(developpement)) {
			getEvolution().getDeveloppementExamensConcours().remove(developpement);
			getListePriorisationEvolution().remove(getListePriorisationEvolution().size() - 1);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution", "listePriorisationEvolution" })
	public void ajouterLigneDeveloppementExamensConcours() {
		EaeDeveloppementDto dto = new EaeDeveloppementDto();
		dto.setPriorisation(getListePriorisationEvolution().size() + 1);
		if (getEvolution().getDeveloppementExamensConcours() != null) {
			getEvolution().getDeveloppementExamensConcours().add(dto);
			getListePriorisationEvolution().add(getListePriorisationEvolution().size() + 1);
		} else {
			List<EaeDeveloppementDto> liste = new ArrayList<>();
			liste.add(dto);
			getEvolution().setDeveloppementExamensConcours(liste);
			getListePriorisationEvolution().add(getListePriorisationEvolution().size() + 1);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution", "listePriorisationEvolution" })
	public void supprimerLigneDeveloppementPersonnel(@BindingParam("ref") EaeDeveloppementDto developpement) {
		if (getEvolution().getDeveloppementPersonnel().contains(developpement)) {
			getEvolution().getDeveloppementPersonnel().remove(developpement);
			getListePriorisationEvolution().remove(getListePriorisationEvolution().size() - 1);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution", "listePriorisationEvolution" })
	public void ajouterLigneDeveloppementPersonnel() {
		EaeDeveloppementDto dto = new EaeDeveloppementDto();
		dto.setPriorisation(getListePriorisationEvolution().size() + 1);
		if (getEvolution().getDeveloppementPersonnel() != null) {
			getEvolution().getDeveloppementPersonnel().add(dto);
			getListePriorisationEvolution().add(getListePriorisationEvolution().size() + 1);
		} else {
			List<EaeDeveloppementDto> liste = new ArrayList<>();
			liste.add(dto);
			getEvolution().setDeveloppementPersonnel(liste);
			getListePriorisationEvolution().add(getListePriorisationEvolution().size() + 1);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution", "listePriorisationEvolution" })
	public void supprimerLigneDeveloppementComportement(@BindingParam("ref") EaeDeveloppementDto developpement) {
		if (getEvolution().getDeveloppementComportement().contains(developpement)) {
			getEvolution().getDeveloppementComportement().remove(developpement);
			getListePriorisationEvolution().remove(getListePriorisationEvolution().size() - 1);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution", "listePriorisationEvolution" })
	public void ajouterLigneDeveloppementComportement() {
		EaeDeveloppementDto dto = new EaeDeveloppementDto();
		dto.setPriorisation(getListePriorisationEvolution().size() + 1);
		if (getEvolution().getDeveloppementComportement() != null) {
			getEvolution().getDeveloppementComportement().add(dto);
			getListePriorisationEvolution().add(getListePriorisationEvolution().size() + 1);
		} else {
			List<EaeDeveloppementDto> liste = new ArrayList<>();
			liste.add(dto);
			getEvolution().setDeveloppementComportement(liste);
			getListePriorisationEvolution().add(getListePriorisationEvolution().size() + 1);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution" })
	public void supprimerLigneDeveloppementFormateur(@BindingParam("ref") EaeDeveloppementDto developpement) {
		if (getEvolution().getDeveloppementFormateur().contains(developpement)) {
			getEvolution().getDeveloppementFormateur().remove(developpement);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution" })
	public void ajouterLigneDeveloppementFormateur() {
		EaeDeveloppementDto dto = new EaeDeveloppementDto();
		dto.setPriorisation(null);
		dto.setEcheance(null);

		if (getEvolution().getDeveloppementFormateur() != null) {
			getEvolution().getDeveloppementFormateur().add(dto);
		} else {
			List<EaeDeveloppementDto> liste = new ArrayList<>();
			liste.add(dto);
			getEvolution().setDeveloppementFormateur(liste);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution", "listePriorisationEvolution" })
	public void supprimerLigneDeveloppementCompetence(@BindingParam("ref") EaeDeveloppementDto developpement) {
		if (getEvolution().getDeveloppementCompetences().contains(developpement)) {
			getEvolution().getDeveloppementCompetences().remove(developpement);
			getListePriorisationEvolution().remove(getListePriorisationEvolution().size() - 1);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution", "listePriorisationEvolution" })
	public void ajouterLigneDeveloppementCompetence() {
		EaeDeveloppementDto dto = new EaeDeveloppementDto();
		dto.setPriorisation(getListePriorisationEvolution().size() + 1);
		if (getEvolution().getDeveloppementCompetences() != null) {
			getEvolution().getDeveloppementCompetences().add(dto);
			getListePriorisationEvolution().add(getListePriorisationEvolution().size() + 1);
		} else {
			List<EaeDeveloppementDto> liste = new ArrayList<>();
			liste.add(dto);
			getEvolution().setDeveloppementCompetences(liste);
			getListePriorisationEvolution().add(getListePriorisationEvolution().size() + 1);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution", "listePriorisationEvolution" })
	public void supprimerLigneDeveloppementConnaissance(@BindingParam("ref") EaeDeveloppementDto developpement) {
		if (getEvolution().getDeveloppementConnaissances().contains(developpement)) {
			getEvolution().getDeveloppementConnaissances().remove(developpement);
			getListePriorisationEvolution().remove(getListePriorisationEvolution().size() - 1);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution", "listePriorisationEvolution" })
	public void ajouterLigneDeveloppementConnaissance() {
		EaeDeveloppementDto dto = new EaeDeveloppementDto();
		dto.setPriorisation(getListePriorisationEvolution().size() + 1);
		if (getEvolution().getDeveloppementConnaissances() != null) {
			getEvolution().getDeveloppementConnaissances().add(dto);
			getListePriorisationEvolution().add(getListePriorisationEvolution().size() + 1);
		} else {
			List<EaeDeveloppementDto> liste = new ArrayList<>();
			liste.add(dto);
			getEvolution().setDeveloppementConnaissances(liste);
			getListePriorisationEvolution().add(getListePriorisationEvolution().size() + 1);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution" })
	public void supprimerLigneSouhaitSuggestion(@BindingParam("ref") EaeEvolutionSouhaitDto souhait) {
		if (getEvolution().getSouhaitsSuggestions().contains(souhait)) {
			getEvolution().getSouhaitsSuggestions().remove(souhait);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChanged", "evolution" })
	public void ajouterLigneSouhaitSuggestion() {
		EaeEvolutionSouhaitDto dto = new EaeEvolutionSouhaitDto();
		if (getEvolution().getSouhaitsSuggestions() != null) {
			getEvolution().getSouhaitsSuggestions().add(dto);
		} else {
			List<EaeEvolutionSouhaitDto> liste = new ArrayList<>();
			liste.add(dto);
			getEvolution().setSouhaitsSuggestions(liste);
		}
		textChangedEvolution();
	}

	@Command
	@NotifyChange({ "hasTextChangedPlanAction", "planAction" })
	public void supprimerLigneMoyensAutres(@BindingParam("ref") EaeItemPlanActionDto moyensAutres) {
		if (getPlanAction().getListeMoyensAutres().contains(moyensAutres)) {
			getPlanAction().getListeMoyensAutres().remove(moyensAutres);
		}
		textChangedPlanAction();
	}

	@Command
	@NotifyChange({ "hasTextChangedPlanAction", "planAction" })
	public void ajouterLigneMoyensAutres() {
		getPlanAction().getListeMoyensAutres().add(new EaeItemPlanActionDto());
		textChangedPlanAction();
	}

	@Command
	@NotifyChange({ "hasTextChangedPlanAction", "planAction" })
	public void supprimerLigneMoyensFinanciers(@BindingParam("ref") EaeItemPlanActionDto moyensFinanciers) {
		if (getPlanAction().getListeMoyensFinanciers().contains(moyensFinanciers)) {
			getPlanAction().getListeMoyensFinanciers().remove(moyensFinanciers);
		}
		textChangedPlanAction();
	}

	@Command
	@NotifyChange({ "hasTextChangedPlanAction", "planAction" })
	public void ajouterLigneMoyensFinanciers() {
		getPlanAction().getListeMoyensFinanciers().add(new EaeItemPlanActionDto());
		textChangedPlanAction();
	}

	@Command
	@NotifyChange({ "hasTextChangedPlanAction", "planAction" })
	public void supprimerLigneMoyensMateriels(@BindingParam("ref") EaeItemPlanActionDto moyensMateriels) {
		if (getPlanAction().getListeMoyensMateriels().contains(moyensMateriels)) {
			getPlanAction().getListeMoyensMateriels().remove(moyensMateriels);
		}
		textChangedPlanAction();
	}

	@Command
	@NotifyChange({ "hasTextChangedPlanAction", "planAction" })
	public void ajouterLigneMoyensMateriels() {
		getPlanAction().getListeMoyensMateriels().add(new EaeItemPlanActionDto());
		textChangedPlanAction();
	}

	@Command
	@NotifyChange({ "hasTextChangedPlanAction", "planAction" })
	public void supprimerLigneObjectifIndiv(@BindingParam("ref") EaeItemPlanActionDto objectifIndiv) {
		if (getPlanAction().getListeObjectifsIndividuels().contains(objectifIndiv)) {
			getPlanAction().getListeObjectifsIndividuels().remove(objectifIndiv);
		}
		textChangedPlanAction();
	}

	@Command
	@NotifyChange({ "hasTextChangedPlanAction", "planAction" })
	public void ajouterLigneObjectifIndiv() {
		getPlanAction().getListeObjectifsIndividuels().add(new EaeItemPlanActionDto());
		textChangedPlanAction();
	}

	@Command
	@NotifyChange({ "hasTextChangedPlanAction", "planAction" })
	public void supprimerLigneObjectifPro(@BindingParam("ref") EaeObjectifProDto objectifPro) {
		if (getPlanAction().getObjectifsProfessionnels().contains(objectifPro)) {
			getPlanAction().getObjectifsProfessionnels().remove(objectifPro);
		}
		textChangedPlanAction();
	}

	@Command
	@NotifyChange({ "hasTextChangedPlanAction", "planAction" })
	public void ajouterLigneObjectifPro() {
		EaeObjectifProDto dto = new EaeObjectifProDto();
		if (getPlanAction().getObjectifsProfessionnels() != null) {
			getPlanAction().getObjectifsProfessionnels().add(dto);
		} else {
			List<EaeObjectifProDto> liste = new ArrayList<>();
			liste.add(dto);
			getPlanAction().setObjectifsProfessionnels(liste);
		}
		textChangedPlanAction();
	}

	@Command
	@NotifyChange({ "hasTextChangedResultat", "resultat" })
	public void supprimerLigneIndiv(@BindingParam("ref") EaeResultatDto objectifIndiv) {
		if (getResultat().getObjectifsIndividuels().contains(objectifIndiv)) {
			getResultat().getObjectifsIndividuels().remove(objectifIndiv);
		}
		textChangedResultat();
	}

	@Command
	@NotifyChange({ "hasTextChangedResultat", "resultat" })
	public void ajouterLigneIndiv() {
		EaeResultatDto dto = new EaeResultatDto();
		if (getResultat().getObjectifsIndividuels() != null) {
			getResultat().getObjectifsIndividuels().add(dto);
		} else {
			List<EaeResultatDto> liste = new ArrayList<>();
			liste.add(dto);
			getResultat().setObjectifsIndividuels(liste);
		}
		textChangedResultat();
	}

	@Command
	@NotifyChange({ "hasTextChangedResultat", "resultat" })
	public void supprimerLignePro(@BindingParam("ref") EaeResultatDto objectifPro) {
		if (getResultat().getObjectifsProfessionnels().contains(objectifPro)) {
			getResultat().getObjectifsProfessionnels().remove(objectifPro);
		}
		textChangedResultat();
	}

	@Command
	@NotifyChange({ "hasTextChangedResultat", "resultat" })
	public void ajouterLignePro() {
		EaeResultatDto dto = new EaeResultatDto();
		if (getResultat().getObjectifsProfessionnels() != null) {
			getResultat().getObjectifsProfessionnels().add(dto);
		} else {
			List<EaeResultatDto> liste = new ArrayList<>();
			liste.add(dto);
			getResultat().setObjectifsProfessionnels(liste);
		}
		textChangedResultat();
	}

	@GlobalCommand
	@NotifyChange({ "hasTextChangedAppreciation", "hasTextChangedAutoEvaluation", "hasTextChangedEvaluation", "hasTextChangedEvolution",
			"hasTextChangedFichePoste", "hasTextChangedIdentification", "hasTextChangedPlanAction", "hasTextChangedResultat", "identification",
			"resultat", "appreciationAnnee", "evaluation", "autoEvaluation", "planAction", "evolution" })
	public void annulerEngistreOnglet(@BindingParam("tab") Tab tab) {
		setHasTextChangedIdentification(false);
		setHasTextChangedAppreciation(false);
		setHasTextChangedAutoEvaluation(false);
		setHasTextChangedEvaluation(false);
		setHasTextChangedEvolution(false);
		setHasTextChangedFichePoste(false);
		setHasTextChangedPlanAction(false);
		setHasTextChangedResultat(false);
		setTabCourant(tab);
		getTabboxCourant().setSelectedTab(getTabCourant());
		// on recharge l'eae pour vider les eventuelles modifs
		initEae(getEaeCourant(), getModeSaisi());
	}

	@Command
	public void changeVue(@BindingParam("tabbox") Tabbox tabBox, @BindingParam("tab") Tab tab) {
		setTabboxCourant(tabBox);
		// on regarde si il y des choses non sauvegardées
		if (isHasTextChanged()) {
			tabBox.setSelectedTab(getTabCourant());
			List<ValidationMessage> vList = new ArrayList<ValidationMessage>();
			vList.add(new ValidationMessage(
					"L'onglet " + getTabCourant().getId() + " semble avoir été modifié, vous devriez l'enregistrer avant de continuer."));
			vList.add(new ValidationMessage("Si vous ne l'enregistrez pas, vous allez perdre vos données."));

			final HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("errors", vList);
			map.put("tab", tab);
			Executions.createComponents("/eae/messageEae.zul", null, map);

		} else {
			// on sauvegarde l'onglet
			setTabCourant(tab);
			tabBox.setSelectedTab(getTabCourant());
		}
	}

	private boolean isHasTextChanged() {
		return isHasTextChangedAppreciation() || isHasTextChangedAutoEvaluation() || isHasTextChangedEvaluation() || isHasTextChangedEvolution()
				|| isHasTextChangedFichePoste() || isHasTextChangedIdentification() || isHasTextChangedPlanAction() || isHasTextChangedResultat();
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution" })
	public void textChangedEvolution() {
		if (!isModification())
			setHasTextChangedEvolution(false);
		setHasTextChangedEvolution(true);
	}

	@Command
	@NotifyChange({ "hasTextChangedEvolution", "evolution" })
	public void textChangedEvolutionDelai() {
		if (!isModification())
			setHasTextChangedEvolution(false);
		setHasTextChangedEvolution(true);
		if (getEvolution().isChangementMetier() || getEvolution().isMobiliteGeo() || getEvolution().isMobiliteFonctionnelle()) {
			getEvolution().getDelaiEnvisage().setCourant("MOINS1AN");
		} else {
			getEvolution().getDelaiEnvisage().setCourant(null);
		}
	}

	@Command
	@NotifyChange({ "hasTextChangedEvaluation" })
	public void textChangedEvaluation() {
		if (!isModification())
			setHasTextChangedEvaluation(false);
		setHasTextChangedEvaluation(true);
	}

	@Command
	@NotifyChange({ "hasTextChangedAutoEvaluation" })
	public void textChangedAutoEvaluation() {
		if (!isModification())
			setHasTextChangedAutoEvaluation(false);
		setHasTextChangedAutoEvaluation(true);
	}

	@Command
	@NotifyChange({ "hasTextChangedAppreciation" })
	public void textChangedAppreciation() {
		if (!isModification())
			setHasTextChangedAppreciation(false);
		setHasTextChangedAppreciation(true);
	}

	@Command
	@NotifyChange({ "hasTextChangedIdentification" })
	public void textChangedIdentification() {
		if (!isModification())
			setHasTextChangedIdentification(false);
		setHasTextChangedIdentification(true);
	}

	@Command
	@NotifyChange({ "hasTextChangedPlanAction" })
	public void textChangedPlanAction() {
		if (!isModification())
			setHasTextChangedPlanAction(false);
		setHasTextChangedPlanAction(true);
	}

	@Command
	@NotifyChange({ "hasTextChangedResultat" })
	public void textChangedResultat() {
		if (!isModification())
			setHasTextChangedResultat(false);
		setHasTextChangedResultat(true);
	}

	public String getDateToString(Date date) {
		if (date == null)
			return "";
		return new SimpleDateFormat("dd/MM/yyyy").format(date);
	}

	public String getInfoResponsable(EaeFichePosteDto fichePoste) {
		if (fichePoste == null)
			return "";
		return fichePoste.getResponsableNom() + " " + fichePoste.getResponsablePrenom() + ", " + fichePoste.getResponsableFonction();
	}

	public String getEnteteN1() {
		return String.valueOf(getAnnee() - 2);
	}

	public String getEnteteN2() {
		return String.valueOf(getAnnee() - 3);
	}

	public String getEnteteN3() {
		return String.valueOf(getAnnee() - 4);
	}

	public String getInfoDureeEntretien(EaeEvaluationDto dto) {
		if (dto.getDureeEntretien() == null)
			return "";
		return getHeureMinute(dto.getDureeEntretien());
	}

	private static String getHeureMinute(int nombreMinute) {
		int heure = nombreMinute / 60;
		int minute = nombreMinute % 60;
		String res = "";
		if (heure > 0)
			res += heure + " h ";
		if (minute > 0)
			res += minute + " min";

		return res;
	}

	public String transformeDuree(Integer ancienneteEchelonJours) {
		String res = "";
		if (ancienneteEchelonJours == null)
			return res;

		Integer annee = ancienneteEchelonJours / 365;
		Integer mois = (ancienneteEchelonJours % 365) / 30;
		Integer jour = (ancienneteEchelonJours % 365) % 30;
		if (annee == 1) {
			res += annee + " an";
		} else if (annee > 1) {
			res += annee + " ans";

		}

		if (mois > 0) {
			if (res.equals(""))
				res += mois + " mois";
			else
				res += ", " + mois + " mois";
		}
		if (jour == 1) {
			if (res.equals(""))
				res += jour + " jour";
			else
				res += ", " + jour + " jour";
		} else if (jour > 1) {
			if (res.equals(""))
				res += jour + " jours";
			else
				res += ", " + jour + " jours";
		}

		return res;
	}

	public String concatAgent(String nom, String prenom) {
		return nom + " " + prenom;
	}

	public EaeListItemDto getEaeCourant() {
		return eaeCourant;
	}

	public void setEaeCourant(EaeListItemDto eaeCourant) {
		this.eaeCourant = eaeCourant;
	}

	public Tab getTabCourant() {
		return tabCourant;
	}

	public void setTabCourant(Tab tabCourant) {
		this.tabCourant = tabCourant;
	}

	public EaeIdentificationDto getIdentification() {
		return identification;
	}

	public void setIdentification(EaeIdentificationDto identification) {
		this.identification = identification;
	}

	public boolean isModification() {
		return isModification;
	}

	public void setModification(boolean isModification) {
		this.isModification = isModification;
	}

	public Tabbox getTabboxCourant() {
		return tabboxCourant;
	}

	public void setTabboxCourant(Tabbox tabboxCourant) {
		this.tabboxCourant = tabboxCourant;
	}

	public String getModeSaisi() {
		return modeSaisi;
	}

	public void setModeSaisi(String modeSaisi) {
		this.modeSaisi = modeSaisi;
	}

	public List<EaeFichePosteDto> getListeFichePoste() {
		return listeFichePoste;
	}

	public void setListeFichePoste(List<EaeFichePosteDto> listeFichePoste) {
		this.listeFichePoste = listeFichePoste;
	}

	public EaeFichePosteDto getFichePostePrimaire() {
		return fichePostePrimaire;
	}

	public void setFichePostePrimaire(EaeFichePosteDto fichePostePrimaire) {
		this.fichePostePrimaire = fichePostePrimaire;
	}

	public EaeFichePosteDto getFichePosteSecondaire() {
		return fichePosteSecondaire;
	}

	public void setFichePosteSecondaire(EaeFichePosteDto fichePosteSecondaire) {
		this.fichePosteSecondaire = fichePosteSecondaire;
	}

	public FichePosteDto getSirhWsFichePostePrimaire() {
		return sirhWsFichePostePrimaire;
	}

	public void setSirhWsFichePostePrimaire(FichePosteDto sirhWsFichePostePrimaire) {
		this.sirhWsFichePostePrimaire = sirhWsFichePostePrimaire;
	}

	public FichePosteDto getSirhWsFichePosteSecondaire() {
		return sirhWsFichePosteSecondaire;
	}

	public void setSirhWsFichePosteSecondaire(FichePosteDto sirhWsFichePosteSecondaire) {
		this.sirhWsFichePosteSecondaire = sirhWsFichePosteSecondaire;
	}

	public EaeResultatsDto getResultat() {
		return resultat;
	}

	public void setResultat(EaeResultatsDto resultat) {
		this.resultat = resultat;
	}

	public EaeAppreciationDto getAppreciationAnnee() {
		return appreciationAnnee;
	}

	public void setAppreciationAnnee(EaeAppreciationDto appreciationAnnee) {
		this.appreciationAnnee = appreciationAnnee;
	}

	public EaeAppreciationDto getAppreciationAnneePrec() {
		return appreciationAnneePrec;
	}

	public void setAppreciationAnneePrec(EaeAppreciationDto appreciationAnneePrec) {
		this.appreciationAnneePrec = appreciationAnneePrec;
	}

	public Integer getAnnee() {
		return annee;
	}

	public void setAnnee(Integer annee) {
		this.annee = annee;
	}

	public Integer getAnneePrec() {
		return anneePrec;
	}

	public void setAnneePrec(Integer anneePrec) {
		this.anneePrec = anneePrec;
	}

	public EaeEvaluationDto getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(EaeEvaluationDto evaluation) {
		this.evaluation = evaluation;
	}

	public EaeAutoEvaluationDto getAutoEvaluation() {
		return autoEvaluation;
	}

	public void setAutoEvaluation(EaeAutoEvaluationDto autoEvaluation) {
		this.autoEvaluation = autoEvaluation;
	}

	public EaePlanActionDto getPlanAction() {
		return planAction;
	}

	public void setPlanAction(EaePlanActionDto planAction) {
		this.planAction = planAction;
	}

	public EaeEvolutionDto getEvolution() {
		return evolution;
	}

	public void setEvolution(EaeEvolutionDto evolution) {
		this.evolution = evolution;
	}

	public List<Integer> getListePriorisationEvolution() {
		return listePriorisationEvolution;
	}

	public void setListePriorisationEvolution(List<Integer> listePriorisationEvolution) {
		this.listePriorisationEvolution = listePriorisationEvolution;
	}

	public boolean isHasTextChangedIdentification() {
		return hasTextChangedIdentification;
	}

	public void setHasTextChangedIdentification(boolean hasTextChangedIdentification) {
		this.hasTextChangedIdentification = hasTextChangedIdentification;
	}

	public boolean isHasTextChangedFichePoste() {
		return hasTextChangedFichePoste;
	}

	public void setHasTextChangedFichePoste(boolean hasTextChangedFichePoste) {
		this.hasTextChangedFichePoste = hasTextChangedFichePoste;
	}

	public boolean isHasTextChangedResultat() {
		return hasTextChangedResultat;
	}

	public void setHasTextChangedResultat(boolean hasTextChangedResultat) {
		this.hasTextChangedResultat = hasTextChangedResultat;
	}

	public boolean isHasTextChangedAppreciation() {
		return hasTextChangedAppreciation;
	}

	public void setHasTextChangedAppreciation(boolean hasTextChangedAppreciation) {
		this.hasTextChangedAppreciation = hasTextChangedAppreciation;
	}

	public boolean isHasTextChangedEvaluation() {
		return hasTextChangedEvaluation;
	}

	public void setHasTextChangedEvaluation(boolean hasTextChangedEvaluation) {
		this.hasTextChangedEvaluation = hasTextChangedEvaluation;
	}

	public boolean isHasTextChangedAutoEvaluation() {
		return hasTextChangedAutoEvaluation;
	}

	public void setHasTextChangedAutoEvaluation(boolean hasTextChangedAutoEvaluation) {
		this.hasTextChangedAutoEvaluation = hasTextChangedAutoEvaluation;
	}

	public boolean isHasTextChangedPlanAction() {
		return hasTextChangedPlanAction;
	}

	public void setHasTextChangedPlanAction(boolean hasTextChangedPlanAction) {
		this.hasTextChangedPlanAction = hasTextChangedPlanAction;
	}

	public boolean isHasTextChangedEvolution() {
		return hasTextChangedEvolution;
	}

	public void setHasTextChangedEvolution(boolean hasTextChangedEvolution) {
		this.hasTextChangedEvolution = hasTextChangedEvolution;
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

	public String getHeureDuree() {
		return heureDuree;
	}

	public void setHeureDuree(String heureDuree) {
		this.heureDuree = heureDuree;
	}

	public String getMinuteDuree() {
		return minuteDuree;
	}

	public void setMinuteDuree(String minuteDuree) {
		this.minuteDuree = minuteDuree;
	}

	public String getTitreResultatEvaluation(String statut) {
		String res = "Résultats dans le poste, Note";

		if (statut != null && (statut.equals("C") || statut.equals("CC"))) {
			res += ", revalorisation ou reclassification";
		}
		return res;
	}

	public boolean isContractuelConvention(String statut) {
		if (statut != null && (statut.equals("C") || statut.equals("CC"))) {
			return true;
		}
		return false;
	}

	public boolean isChangementClasse(String typeAvct) {
		// changement de classe = PROMO
		if (typeAvct != null && typeAvct.equals("PROMO")) {
			return true;
		}
		return false;
	}

	public boolean isRevalorisation(String typeAvct) {
		if (typeAvct != null && typeAvct.equals("REVA")) {
			return true;
		}
		return false;
	}

	public boolean isAvancementDifferencieCAP(String typeAvct, boolean cap) {
		if (typeAvct != null && typeAvct.equals("AD") && cap) {
			return true;
		}
		return false;
	}

	public boolean isAfficherTitreFonctionnaireEvaluation(String statut, String typeAvct, boolean cap) {
		if (statut != null && statut.equals("F")) {
			if (typeAvct != null && typeAvct.equals("PROMO")) {
				return true;
			} else if (typeAvct != null && typeAvct.equals("AD") && cap) {
				return true;
			}
		}
		return false;
	}

	public String getTitreFonctionnaireEvaluation(EaeEvaluationDto evaluation) {
		return "Changement d'échelon ou de classe pour les agents promouvable en " + evaluation.getAnneeAvancement();
	}

	@Command
	public void changeEcran(@BindingParam("page") String page, @BindingParam("ecran") Div div, @BindingParam("param") String param) {
		div.getChildren().clear();
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("div", div);
		args.put("param", param);

		Executions.createComponents(page + ".zul", div, args);
	}

	@Command
	public void openPopupFinalisation() {
		// create a window programmatically and use it as a modal dialog.
		Map<String, EaeListItemDto> args = new HashMap<String, EaeListItemDto>();
		args.put("eaeCourant", getEaeCourant());
		Window win = (Window) Executions.createComponents("/eae/onglet/popupFinalisation.zul", null, args);
		win.doModal();

	}

	public boolean isFinalisationPossible() {
		return eaeWsConsumer.canFinaliseEae(getEaeCourant().getIdEae(), currentUser.getAgent().getIdAgent());
	}

	public String getTitreBadFinalisation() {
		String res = "";
		boolean isPossible = eaeWsConsumer.canFinaliseEae(getEaeCourant().getIdEae(), currentUser.getAgent().getIdAgent());
		if (!isPossible) {
			String etat = EaeEtatEnum.getEtatFromCode(getEaeCourant().getEtat()) == null ? ""
					: EaeEtatEnum.getEtatFromCode(getEaeCourant().getEtat()).toString();
			res = "Impossible de finaliser l'Eae car son état est " + etat;
		}
		return res;
	}

	public void setFinalisationPossible(boolean finalisationPossible) {
		this.finalisationPossible = finalisationPossible;
	}

	public void setTitreBadFinalisation(String titreBadFinalisation) {
		this.titreBadFinalisation = titreBadFinalisation;
	}
}
