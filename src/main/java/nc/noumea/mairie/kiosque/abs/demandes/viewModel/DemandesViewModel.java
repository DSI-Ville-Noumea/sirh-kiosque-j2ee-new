package nc.noumea.mairie.kiosque.abs.demandes.viewModel;

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

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import nc.noumea.mairie.ads.dto.EntiteDto;
import nc.noumea.mairie.kiosque.abs.dto.DemandeDto;
import nc.noumea.mairie.kiosque.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.kiosque.abs.dto.RefEtatAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.RefEtatEnum;
import nc.noumea.mairie.kiosque.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.kiosque.abs.planning.CustomEventsManager;
import nc.noumea.mairie.kiosque.abs.planning.vo.CustomDHXPlanner;
import nc.noumea.mairie.kiosque.dto.AgentDto;
import nc.noumea.mairie.kiosque.dto.AgentWithServiceDto;
import nc.noumea.mairie.kiosque.dto.ReturnMessageDto;
import nc.noumea.mairie.kiosque.export.ExcelExporter;
import nc.noumea.mairie.kiosque.export.PdfExporter;
import nc.noumea.mairie.kiosque.validation.ValidationMessage;
import nc.noumea.mairie.kiosque.viewModel.AbstractViewModel;

import org.apache.catalina.session.StandardSessionFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zkmax.zul.Chosenbox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Window;

import com.dhtmlx.planner.DHXSkin;

//@Controller utile pour le planning
@Controller
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class DemandesViewModel extends AbstractViewModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5338643536442576795L;
	
	private static final String TAB_PLANNING = "PLANNING";

	private Logger logger = LoggerFactory.getLogger(DemandesViewModel.class);

	private List<DemandeDto> listeDemandes;

	private Tab tabCourant;

	/* POUR LES FILTRES */
	private List<RefGroupeAbsenceDto> listeGroupeAbsenceFiltre;
	private RefGroupeAbsenceDto groupeAbsenceFiltre;
	private List<RefTypeAbsenceDto> listeTypeAbsenceFiltre;
	private RefTypeAbsenceDto typeAbsenceFiltre;
	private List<RefEtatAbsenceDto> listeEtatAbsenceFiltre;
	private List<EntiteDto> listeServicesFiltre;
	private EntiteDto serviceFiltre;
	private List<AgentDto> listeAgentsFiltre;
	private AgentDto agentFiltre;
	private Date dateDebutFiltre;
	private Date dateFinFiltre;
	private Date dateDemandeFiltre;

	/* POUR LE HAUT DU TABLEAU */
	private String filter;
	private String tailleListe;

	private List<RefEtatAbsenceDto> listeEtatsSelectionnes;

	private List<AgentWithServiceDto> listAgentsEquipe;

	@Init
	public void initDemandes(@BindingParam("aApprouver") Boolean aApprouver) {

		// on recharge les groupes d'absences pour les filtres
		List<RefGroupeAbsenceDto> filtreGroupeFamille = absWsConsumer.getRefGroupeAbsence();
		setListeGroupeAbsenceFiltre(filtreGroupeFamille);

		// on charge les services pour les filtres
		List<EntiteDto> filtreService = absWsConsumer.getServicesAbsences(getCurrentUser().getAgent().getIdAgent());
		setListeServicesFiltre(filtreService);
		// pour les agents, on ne remplit pas la liste, elle le sera avec le
		// choix du service
		setListeAgentsFiltre(null);
		// on recharge les états d'absences pour les filtres
		List<RefEtatAbsenceDto> filtreEtat = absWsConsumer.getEtatAbsenceKiosque("NON_PRISES");
		setListeEtatAbsenceFiltre(filtreEtat);
		setTailleListe("10");

		// #15024 en provenance des portlets d accueil
		String param = (String) Executions.getCurrent().getArg().get("param");
		if (null != param && "aApprouver".equals(param)) {
			setListeEtatsSelectionnes(Arrays.asList(getListeEtatAbsenceFiltre().get(1), getListeEtatAbsenceFiltre().get(2), getListeEtatAbsenceFiltre().get(3)));
		} else if (null != param && "aViser".equals(param)) {
			setListeEtatsSelectionnes(Arrays.asList(getListeEtatAbsenceFiltre().get(1)));
		}
	}

	@Command
	public void visuSoldeAgent(@BindingParam("ref") DemandeDto demande) {
		// create a window programmatically and use it as a modal dialog.
		Map<String, DemandeDto> args = new HashMap<String, DemandeDto>();
		args.put("demandeCourant", demande);
		Window win = (Window) Executions.createComponents("/absences/demandes/demandesSoldeAgent.zul", null, args);
		win.doModal();
	}

	@Command
	@NotifyChange({ "listeAgentsFiltre" })
	public void chargeAgent() {
		// on charge les agents pour les filtres
		List<AgentDto> filtreAgent = absWsConsumer.getAgentsAbsences(getCurrentUser().getAgent().getIdAgent(), getServiceFiltre().getIdEntite());
		setListeAgentsFiltre(filtreAgent);
	}

	public String concatAgent(String nom, String prenom) {
		return nom + " " + prenom;
	}

	@Command
	@NotifyChange({ "listeDemandes" })
	public void doSearch() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		List<DemandeDto> list = new ArrayList<DemandeDto>();
		if (getFilter() != null && !"".equals(getFilter()) && getListeDemandes() != null) {
			for (DemandeDto item : getListeDemandes()) {
				if (item.getLibelleTypeDemande().toLowerCase().contains(getFilter().toLowerCase())) {
					if (!list.contains(item))
						list.add(item);
				}
				if (item.getAgentWithServiceDto().getNom().toLowerCase().contains(getFilter().toLowerCase())) {
					if (!list.contains(item))
						list.add(item);
				}
				if (item.getAgentWithServiceDto().getPrenom().toLowerCase().contains(getFilter().toLowerCase())) {
					if (!list.contains(item))
						list.add(item);
				}
				if (sdf.format(item.getDateDebut()).contains(getFilter().toLowerCase())) {
					if (!list.contains(item))
						list.add(item);
				}
			}
			setListeDemandes(list);
		} else {
			filtrer(null);
		}
	}

	@Command
	@NotifyChange({ "listeTypeAbsenceFiltre", "typeAbsenceFiltre" })
	public void alimenteTypeFamilleAbsence() {
		// #39896 : l'approbateur peut filtrer sur toutes les maladies et congés exceptionnels.
		List<RefTypeAbsenceDto> filtreFamilleAbsence = absWsConsumer.getRefTypeAbsenceForFilterKiosque(getGroupeAbsenceFiltre().getIdRefGroupeAbsence(), getAgentFiltre() == null ? null : getAgentFiltre()
				.getIdAgent());
		if (filtreFamilleAbsence.size() == 1) {
			setListeTypeAbsenceFiltre(null);
			setTypeAbsenceFiltre(null);
		} else {
			// #39525 : Ajout d'une "case vide" au début du filtre
			filtreFamilleAbsence.add(0, new RefTypeAbsenceDto());
			setListeTypeAbsenceFiltre(filtreFamilleAbsence);
			setTypeAbsenceFiltre(null);
		}
	}

	@Command
	@NotifyChange({ "listeDemandes", "listeEtatAbsenceFiltre", "listeEtatsSelectionnes" })
	public void changeVue(@BindingParam("tab") Tab tab) {
		setListeDemandes(null);
		// on recharge les états d'absences pour les filtres
		List<RefEtatAbsenceDto> filtreEtat = absWsConsumer.getEtatAbsenceKiosque(tab.getId());
		setListeEtatAbsenceFiltre(filtreEtat);
		// on sauvegarde l'onglet
		setTabCourant(tab);
		setListeEtatsSelectionnes(null);
		filtrer(null);
	}

	@Command
	@NotifyChange({ "listeDemandes" })
	public void setTabDebut(@BindingParam("tab") Tab tab) {
		setTabCourant(tab);
		filtrer(null);
	}

	@Command
	@NotifyChange({ "listeDemandes" })
	public void filtrer(@BindingParam("ref") Chosenbox boxEtat) {

		List<Integer> etats = new ArrayList<Integer>();
		for (RefEtatAbsenceDto etat : getListeEtatsSelectionnes()) {
			etats.add(etat.getIdRefEtat());
		}

		List<DemandeDto> result = absWsConsumer.getListeDemandes(getCurrentUser().getAgent().getIdAgent(), getTabCourant().getId(), getDateDebutFiltre(), getDateFinFiltre(), getDateDemandeFiltre(),
				etats.size() == 0 ? null : etats.toString().replace("[", "").replace("]", "").replace(" ", ""), getTypeAbsenceFiltre() == null ? null : getTypeAbsenceFiltre().getIdRefTypeAbsence(),
				getGroupeAbsenceFiltre() == null ? null : getGroupeAbsenceFiltre().getIdRefGroupeAbsence(), getAgentFiltre() == null ? null : getAgentFiltre().getIdAgent(),
				getServiceFiltre() == null ? null : getServiceFiltre().getIdEntite());
		setListeDemandes(result);

		// #12159 construction du planning
		if (TAB_PLANNING.equals(getTabCourant().getId())) {
			org.apache.catalina.session.StandardSessionFacade s = (StandardSessionFacade) Executions.getCurrent().getSession().getNativeSession();
			s.setAttribute("listeDemandes", result);

			if (null != getAgentFiltre() && null != getAgentFiltre().getIdAgent()) {
				List<AgentWithServiceDto> listAgentsWithServiceDto = new ArrayList<AgentWithServiceDto>();
				AgentWithServiceDto agentsWithServiceDto = new AgentWithServiceDto(getAgentFiltre(), getServiceFiltre().getIdEntite(), getServiceFiltre().getLabel());
				listAgentsWithServiceDto.add(agentsWithServiceDto);

				s.setAttribute("listeAgents", listAgentsWithServiceDto);
			} else {
				if(null == getListAgentsEquipe()) {
					List<AgentWithServiceDto> listAgentsWithServiceDto = new ArrayList<AgentWithServiceDto>();
					for (EntiteDto service : getListeServicesFiltre()) {
						if (null != service.getIdEntite()) {
							List<AgentDto> listeAgents = absWsConsumer.getAgentsAbsences(getCurrentUser().getAgent().getIdAgent(), service.getIdEntite());
	
							if (null != listeAgents) {
								for (AgentDto agent : listeAgents) {
									AgentWithServiceDto agentsWithServiceDto = new AgentWithServiceDto(agent, service.getIdEntite(), service.getLabel());
									listAgentsWithServiceDto.add(agentsWithServiceDto);
								}
							}
						}
					}
	
					setListAgentsEquipe(listAgentsWithServiceDto);	
				}
				
				s.setAttribute("listeAgents", getListAgentsEquipe());
			}

			doAfterCompose(getTabCourant().getFellow("tb").getFellow("tabpanelplanning").getFellow("includeplanning").getFellow("windowplanning"));
		}
	}

	@GlobalCommand
	@NotifyChange({ "listeDemandes" })
	public void refreshListeDemande() {
		filtrer(null);
	}

	@Command
	@NotifyChange({ "listeDemandes" })
	public void viserDemandeFavorable(@BindingParam("ref") DemandeDto demande) {
		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdDemande(demande.getIdDemande());
		dto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		dto.setDateAvis(new Date());

		ReturnMessageDto result = absWsConsumer.changerEtatDemandeAbsence(getCurrentUser().getAgent().getIdAgent(), dto);

		if (result.getErrors().size() > 0 || result.getInfos().size() > 0) {
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
		}

		filtrer(null);
	}

	@Command
	@NotifyChange({ "listeDemandes" })
	public void viserAllDemandeFavorable(@BindingParam("ref") Grid tab) {
		List<DemandeDto> listeDemandeAffichee = getListeDemandes().subList(
				tab.getActivePage() * Integer.valueOf(getTailleListe()),
				(tab.getActivePage() * Integer.valueOf(getTailleListe()) + Integer.valueOf(getTailleListe())) > getListeDemandes().size() ? getListeDemandes().size() : (tab.getActivePage()
						* Integer.valueOf(getTailleListe()) + Integer.valueOf(getTailleListe())));

		List<ValidationMessage> listErreur = new ArrayList<ValidationMessage>();
		List<ValidationMessage> listInfo = new ArrayList<ValidationMessage>();
		for (DemandeDto demande : listeDemandeAffichee) {
			if (demande.getIdRefEtat() == RefEtatEnum.SAISIE.getCodeEtat() || demande.getIdRefEtat() == RefEtatEnum.VISEE_FAVORABLE.getCodeEtat()
					|| demande.getIdRefEtat() == RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat()) {

				DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
				dto.setIdDemande(demande.getIdDemande());
				dto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
				dto.setDateAvis(new Date());

				ReturnMessageDto result = absWsConsumer.changerEtatDemandeAbsence(getCurrentUser().getAgent().getIdAgent(), dto);

				if (result.getErrors().size() > 0 || result.getInfos().size() > 0) {
					for (String error : result.getErrors()) {
						ValidationMessage vm = new ValidationMessage(error);
						listErreur.add(vm);
					}
					for (String info : result.getInfos()) {
						ValidationMessage vm = new ValidationMessage(info);
						listInfo.add(vm);
					}
				}
			}
		}

		if (listErreur.size() > 0 || listInfo.size() > 0) {
			final HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("errors", listErreur);
			map.put("infos", listInfo);
			Executions.createComponents("/messages/returnMessage.zul", null, map);
		}

		filtrer(null);
	}

	@Command
	public void viserDemandeDefavorable(@BindingParam("ref") DemandeDto demande) {
		// create a window programmatically and use it as a modal dialog.
		Map<String, DemandeDto> args = new HashMap<String, DemandeDto>();
		args.put("demandeCourant", demande);
		Window win = (Window) Executions.createComponents("/absences/demandes/viserDemande.zul", null, args);
		win.doModal();
	}

	@Command
	public void openLegende() {
		// create a window programmatically and use it as a modal dialog.
		Window win = (Window) Executions.createComponents("/absences/legendPlanning.zul", null, null);
		win.doModal();
	}

	@Command
	public void openPiecesJointes(@BindingParam("ref") DemandeDto demande) {
		// create a window programmatically and use it as a modal dialog.
		Map<String, DemandeDto> args = new HashMap<String, DemandeDto>();
		args.put("demandeCourant", demande);
		Window win = (Window) Executions.createComponents("/absences/piecesJointes.zul", null, args);
		win.doModal();
	}

	@Command
	@NotifyChange({ "listeDemandes" })
	public void approuverDemande(@BindingParam("ref") DemandeDto demande) {

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdDemande(demande.getIdDemande());
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setDateAvis(new Date());

		ReturnMessageDto result = absWsConsumer.changerEtatDemandeAbsence(getCurrentUser().getAgent().getIdAgent(), dto);

		if (result.getErrors().size() > 0 || result.getInfos().size() > 0) {
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
		}

		filtrer(null);
	}

	@Command
	@NotifyChange({ "listeDemandes" })
	public void approuverAllDemande(@BindingParam("ref") Grid tab) {
		List<DemandeDto> listeDemandeAffichee = getListeDemandes().subList(
				tab.getActivePage() * Integer.valueOf(getTailleListe()),
				(tab.getActivePage() * Integer.valueOf(getTailleListe()) + Integer.valueOf(getTailleListe())) > getListeDemandes().size() ? getListeDemandes().size() : (tab.getActivePage()
						* Integer.valueOf(getTailleListe()) + Integer.valueOf(getTailleListe())));
		List<ValidationMessage> listErreur = new ArrayList<ValidationMessage>();
		List<ValidationMessage> listInfo = new ArrayList<ValidationMessage>();
		for (DemandeDto demande : listeDemandeAffichee) {
			if (demande.getIdRefEtat() == RefEtatEnum.SAISIE.getCodeEtat() || demande.getIdRefEtat() == RefEtatEnum.VISEE_FAVORABLE.getCodeEtat()
					|| demande.getIdRefEtat() == RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat() || demande.getIdRefEtat() == RefEtatEnum.APPROUVEE.getCodeEtat()
					|| demande.getIdRefEtat() == RefEtatEnum.REFUSEE.getCodeEtat()) {
				DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
				dto.setIdDemande(demande.getIdDemande());
				dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
				dto.setDateAvis(new Date());

				ReturnMessageDto result = absWsConsumer.changerEtatDemandeAbsence(getCurrentUser().getAgent().getIdAgent(), dto);

				if (result.getErrors().size() > 0 || result.getInfos().size() > 0) {
					for (String error : result.getErrors()) {
						ValidationMessage vm = new ValidationMessage(error);
						listErreur.add(vm);
					}
					for (String info : result.getInfos()) {
						ValidationMessage vm = new ValidationMessage(info);
						listInfo.add(vm);
					}
				}
			}
		}

		if (listErreur.size() > 0 || listInfo.size() > 0) {
			final HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("errors", listErreur);
			map.put("infos", listInfo);
			Executions.createComponents("/messages/returnMessage.zul", null, map);
		}

		filtrer(null);
	}

	@Command
	public void desapprouverDemande(@BindingParam("ref") DemandeDto demande) {
		// create a window programmatically and use it as a modal dialog.
		Map<String, DemandeDto> args = new HashMap<String, DemandeDto>();
		args.put("demandeCourant", demande);
		Window win = (Window) Executions.createComponents("/absences/demandes/approuverDemande.zul", null, args);
		win.doModal();
	}

	@Command
	public void demanderControleMedical(@BindingParam("ref") DemandeDto demande) {
		// create a window programmatically and use it as a modal dialog.
		Map<String, DemandeDto> args = new HashMap<String, DemandeDto>();
		args.put("demandeCourant", demande);
		Window win = (Window) Executions.createComponents("/absences/demandes/demanderControleMedical.zul", null, args);
		win.doModal();
	}

	@Command
	public void ajouterDemande() {
		// create a window programmatically and use it as a modal dialog.
		Window win = (Window) Executions.createComponents("/absences/demandes/ajoutDemande.zul", null, null);
		win.doModal();
	}

	@Command
	public void imprimerDemande(@BindingParam("ref") DemandeDto demande) {
		// on imprime la demande
		byte[] resp = absWsConsumer.imprimerDemande(getCurrentUser().getAgent().getIdAgent(), demande.getIdDemande());
		Filedownload.save(resp, "application/pdf", "titreAbsence");
	}

	@Command
	public void annulerDemande(@BindingParam("ref") DemandeDto demande) {
		// create a window programmatically and use it as a modal dialog.
		Map<String, DemandeDto> args = new HashMap<String, DemandeDto>();
		args.put("demandeCourant", demande);
		Window win = (Window) Executions.createComponents("/absences/annulerDemande.zul", null, args);
		win.doModal();
	}

	@Command
	public void modifierDemande(@BindingParam("ref") DemandeDto demande) {
		// create a window programmatically and use it as a modal dialog.
		Map<String, DemandeDto> args = new HashMap<String, DemandeDto>();
		args.put("demandeCourant", demande);
		Window win = (Window) Executions.createComponents("/absences/modifierDemande.zul", null, args);
		win.doModal();
	}

	@Command
	public void supprimerDemande(@BindingParam("ref") DemandeDto demande) {
		// create a window programmatically and use it as a modal dialog.
		Map<String, DemandeDto> args = new HashMap<String, DemandeDto>();
		args.put("demandeCourant", demande);
		Window win = (Window) Executions.createComponents("/absences/supprimerDemande.zul", null, args);
		win.doModal();
	}

	public List<DemandeDto> getHistoriqueAbsence(DemandeDto abs) {
		List<DemandeDto> result = absWsConsumer.getHistoriqueAbsence(getCurrentUser().getAgent().getIdAgent(), abs.getIdDemande());
		return result;
	}

	@Command
	@NotifyChange({ "groupeAbsenceFiltre", "typeAbsenceFiltre", "listeTypeAbsenceFiltre", "dateDebutFiltre", "dateFinFiltre", "dateDemandeFiltre", "agentFiltre", "listeAgentsFiltre", "serviceFiltre",
			"listeEtatAbsenceFiltre" })
	public void viderFiltre() {
		setGroupeAbsenceFiltre(null);
		setAgentFiltre(null);
		setListeAgentsFiltre(null);
		setServiceFiltre(null);
		setTypeAbsenceFiltre(null);
		setListeTypeAbsenceFiltre(null);
		setDateDebutFiltre(null);
		setDateFinFiltre(null);
		setDateDemandeFiltre(null);
		// on recharge les états d'absences pour les filtres
		List<RefEtatAbsenceDto> filtreEtat = absWsConsumer.getEtatAbsenceKiosque(getTabCourant().getId());
		setListeEtatAbsenceFiltre(filtreEtat);

		setListeEtatsSelectionnes(new ArrayList<RefEtatAbsenceDto>());
	}

	@Command
	public void exportPDF(@BindingParam("ref") Grid grid) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		PdfExporter exporter = new PdfExporter();
		exporter.export(grid, out);

		AMedia amedia = new AMedia("gestionDemandes.pdf", "pdf", "application/pdf", out.toByteArray());
		Filedownload.save(amedia);
		out.close();
	}

	@Command
	public void exportExcel(@BindingParam("ref") Grid grid) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		ExcelExporter exporter = new ExcelExporter();
		exporter.export(grid, out);

		AMedia amedia = new AMedia("gestionDemandes.xlsx", "xls", "application/file", out.toByteArray());
		Filedownload.save(amedia);
		out.close();
	}

	public String getEtatToString(Integer idRefEtat) {
		return RefEtatEnum.getRefEtatEnum(idRefEtat).getLibEtat();
	}

	public String dateSaisieToString(Date dateSaisie) {
		SimpleDateFormat sdfJour = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfHeure = new SimpleDateFormat("HH:mm");
		return sdfJour.format(dateSaisie) + " à " + sdfHeure.format(dateSaisie);
	}

	public String getDateDebutToString(DemandeDto dto) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String res = sdf.format(dto.getDateDebut());
		if (dto.getTypeSaisi() != null && dto.getTypeSaisi().getUniteDecompte().equals("jours")) {
			if (dto.isDateDebutAM()) {
				res += " - M";
			} else if (dto.isDateDebutPM()) {
				res += " - A";
			}
			return res;
		}
		if (dto.getTypeSaisiCongeAnnuel() != null) {
			if (dto.isDateDebutAM()) {
				res += " - M";
			} else if (dto.isDateDebutPM()) {
				res += " - A";
			}
			return res;
		}
		// #15623 restitution massive de CA
		if (0 == dto.getIdTypeDemande()) {
			if (dto.isDateDebutAM()) {
				res += " - M";
			} else if (dto.isDateDebutPM()) {
				res += " - A";
			}
			return res;
		}
		SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
		return res + " - " + sf.format(dto.getDateDebut());
	}

	public String getDateFinToString(DemandeDto dto) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String res = sdf.format(dto.getDateFin());
		if (dto.getTypeSaisi() != null && dto.getTypeSaisi().getUniteDecompte().equals("jours")) {
			if (dto.isDateFinAM()) {
				res += " - M";
			} else if (dto.isDateFinPM()) {
				res += " - A";
			}
			return res;
		}
		if (dto.getTypeSaisiCongeAnnuel() != null) {
			if (dto.isDateFinAM()) {
				res += " - M";
			} else if (dto.isDateFinPM()) {
				res += " - A";
			}
			return res;
		}
		// #15623 restitution massive de CA
		if (0 == dto.getIdTypeDemande()) {
			if (dto.isDateFinAM()) {
				res += " - M";
			} else if (dto.isDateFinPM()) {
				res += " - A";
			}
			return res;
		}
		SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
		return res + " - " + sf.format(dto.getDateFin());
	}

	public String getDureeToString(DemandeDto dto) {
		if (dto.getTypeSaisi() != null) {
			if (dto.getTypeSaisi().getUniteDecompte().equals("jours")) {
				//#32281 : si on est MALADIE AT, alors dans durée, on affiche le nombre de jours ITT
				if (dto.getTypeSaisi() != null && dto.getTypeSaisi().isNombreITT()) {
					return dto.getNombreITT() + " j";
				}
				return dto.getDuree() + " j";
			} else {
				return getHeureMinute(dto.getDuree().intValue());
			}
		}
		if (dto.getTypeSaisiCongeAnnuel() != null) {
			return dto.getDuree() + " j" + (dto.isSamediOffert() ? " +S" : "");
		}
		// #15623 restitution massive de CA
		if (0 == dto.getIdTypeDemande()) {
			return dto.getDuree() + " j";
		}
		return "";
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

	// ///////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////// #12159 PARTIE PLANNING
	// ////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void doAfterCompose(Component comp) {
		try {
			comp.getFellow("divChild").detach();
		} catch (Exception e) {
			// dans le cas ou la Div "divChild" n'existe pas encore
			// car creer dans planner.render()
		}
		org.apache.catalina.session.StandardSessionFacade s = (StandardSessionFacade) Executions.getCurrent().getSession().getNativeSession();
		@SuppressWarnings("unchecked")
		List<AgentWithServiceDto> listeAgents = (List<AgentWithServiceDto>) s.getAttribute("listeAgents");
		s.removeAttribute("listeAgents");
		if(null != listeAgents) {
			CustomDHXPlanner planner = new CustomDHXPlanner("./codebase/", DHXSkin.TERRACE, "eventsGestionDemandes", listeAgents);
			try {
				Executions.createComponentsDirectly(planner.render(), null, comp.getFellow("div"), null);
			} catch (Exception e) {
				logger.debug("Une erreur est survenue dans la creation du planning : " + e.getMessage());
			}
		}
	}

	@RequestMapping("/eventsGestionDemandes")
	@ResponseBody
	public String events(HttpServletRequest request) {

		@SuppressWarnings("unchecked")
		List<DemandeDto> listDemandes = (List<DemandeDto>) request.getSession().getAttribute("listeDemandes");
		@SuppressWarnings("unchecked")
		List<AgentWithServiceDto> listeAgents = (List<AgentWithServiceDto>) request.getSession().getAttribute("listeAgents");

		CustomEventsManager evs = new CustomEventsManager(request, listDemandes, listeAgents);
		return evs.run();
	}

	// ///////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// FIN PARTIE PLANNING
	// ////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////

	public Tab getTabCourant() {
		return tabCourant;
	}

	public void setTabCourant(Tab tabCourant) {
		this.tabCourant = tabCourant;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getTailleListe() {
		return tailleListe;
	}

	public void setTailleListe(String tailleListe) {
		this.tailleListe = tailleListe;
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

	public List<RefEtatAbsenceDto> getListeEtatAbsenceFiltre() {
		return listeEtatAbsenceFiltre;
	}

	public void setListeEtatAbsenceFiltre(List<RefEtatAbsenceDto> listeEtatAbsenceFiltre) {
		this.listeEtatAbsenceFiltre = listeEtatAbsenceFiltre;
	}

	public Date getDateDebutFiltre() {
		return dateDebutFiltre;
	}

	public void setDateDebutFiltre(Date dateDebutFiltre) {
		this.dateDebutFiltre = dateDebutFiltre;
	}

	public Date getDateFinFiltre() {
		return dateFinFiltre;
	}

	public void setDateFinFiltre(Date dateFinFiltre) {
		this.dateFinFiltre = dateFinFiltre;
	}

	public Date getDateDemandeFiltre() {
		return dateDemandeFiltre;
	}

	public void setDateDemandeFiltre(Date dateDemandeFiltre) {
		this.dateDemandeFiltre = dateDemandeFiltre;
	}

	public List<DemandeDto> getListeDemandes() {
		return listeDemandes;
	}

	public void setListeDemandes(List<DemandeDto> listeDemandes) {
		this.listeDemandes = listeDemandes;
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

	public List<RefGroupeAbsenceDto> getListeGroupeAbsenceFiltre() {
		return listeGroupeAbsenceFiltre;
	}

	public void setListeGroupeAbsenceFiltre(List<RefGroupeAbsenceDto> listeGroupeAbsenceFiltre) {
		this.listeGroupeAbsenceFiltre = listeGroupeAbsenceFiltre;
	}

	public RefGroupeAbsenceDto getGroupeAbsenceFiltre() {
		return groupeAbsenceFiltre;
	}

	public void setGroupeAbsenceFiltre(RefGroupeAbsenceDto groupeAbsenceFiltre) {
		this.groupeAbsenceFiltre = groupeAbsenceFiltre;
	}

	public List<RefEtatAbsenceDto> getListeEtatsSelectionnes() {
		if (null == listeEtatsSelectionnes) {
			return new ArrayList<RefEtatAbsenceDto>();
		}
		return listeEtatsSelectionnes;
	}

	public void setListeEtatsSelectionnes(List<RefEtatAbsenceDto> listeEtatsSelectionnes) {
		this.listeEtatsSelectionnes = listeEtatsSelectionnes;
	}

	public List<AgentWithServiceDto> getListAgentsEquipe() {
		return listAgentsEquipe;
	}

	public void setListAgentsEquipe(List<AgentWithServiceDto> listAgentsEquipe) {
		this.listAgentsEquipe = listAgentsEquipe;
	}

	public boolean affichePouceVertApprobateur(DemandeDto dto) {
		return dto.isModifierApprobation() && dto.getIdRefEtat() != RefEtatEnum.APPROUVEE.getCodeEtat();
	}

	public boolean affichePouceRougeApprobateur(DemandeDto dto) {
		return dto.isModifierApprobation() && dto.getIdRefEtat() != RefEtatEnum.REFUSEE.getCodeEtat();
	}

	public boolean affichePouceVertViseur(DemandeDto dto) {
		return dto.isModifierVisa() && dto.getIdRefEtat() != RefEtatEnum.VISEE_FAVORABLE.getCodeEtat();
	}

	public boolean affichePouceRougeViseur(DemandeDto dto) {
		return dto.isModifierVisa() && dto.getIdRefEtat() != RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat();
	}

	/**
	 * On affiche le bouton 'Controle médical' pour toutes les absences de type maladie.
	 */
	public boolean afficheControleMedical(DemandeDto dto) {
		return dto.getGroupeAbsence().getIdRefGroupeAbsence().equals(RefTypeGroupeAbsenceEnum.MALADIES.getValue());
	}

}
