package nc.noumea.mairie.kiosque.abs.agent.viewModel;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkmax.zul.Chosenbox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Window;

import com.dhtmlx.planner.DHXSkin;

import nc.noumea.mairie.ads.dto.EntiteDto;
import nc.noumea.mairie.kiosque.abs.dto.ActeursDto;
import nc.noumea.mairie.kiosque.abs.dto.ApprobateurDto;
import nc.noumea.mairie.kiosque.abs.dto.DemandeDto;
import nc.noumea.mairie.kiosque.abs.dto.RefEtatAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.RefEtatEnum;
import nc.noumea.mairie.kiosque.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.ResultListDemandeDto;
import nc.noumea.mairie.kiosque.abs.planning.CustomEventsManager;
import nc.noumea.mairie.kiosque.abs.planning.vo.CustomDHXPlanner;
import nc.noumea.mairie.kiosque.dto.AgentWithServiceDto;
import nc.noumea.mairie.kiosque.export.ExcelExporter;
import nc.noumea.mairie.kiosque.export.PdfExporter;
import nc.noumea.mairie.kiosque.profil.dto.ProfilAgentDto;
import nc.noumea.mairie.kiosque.travail.dto.EstChefDto;
import nc.noumea.mairie.utils.DateUtils;
import nc.noumea.mairie.ws.IAdsWSConsumer;
import nc.noumea.mairie.ws.ISirhAbsWSConsumer;
import nc.noumea.mairie.ws.ISirhWSConsumer;

// @Controller utile pour le planning
@Controller
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class DemandesAgentViewModel extends GenericForwardComposer<Component> {

	/**
	 * 
	 */
	private static final long			serialVersionUID	= 1319705744557882740L;

	private Logger						logger				= LoggerFactory.getLogger(DemandesAgentViewModel.class);

	@WireVariable
	private ISirhAbsWSConsumer			absWsConsumer;

	@WireVariable
	private ISirhWSConsumer				sirhWsConsumer;

	@WireVariable
	private IAdsWSConsumer				adsWsConsumer;

	private List<DemandeDto>			listeDemandes;

	private Tab							tabCourant;

	/* POUR LES FILTRES */
	private List<RefGroupeAbsenceDto>	listeGroupeAbsenceFiltre;
	private RefGroupeAbsenceDto			groupeAbsenceFiltre;
	private List<RefTypeAbsenceDto>		listeTypeAbsenceFiltre;
	private RefTypeAbsenceDto			typeAbsenceFiltre;
	private List<RefEtatAbsenceDto>		listeEtatAbsenceFiltre;

	private Date						dateDebutFiltre;
	private Date						dateFinFiltre;
	private Date						dateDemandeFiltre;

	/* POUR LE HAUT DU TABLEAU */
	private String						filter;
	private String						tailleListe;

	private ProfilAgentDto				currentUser;

	private ActeursDto					acteursDto;

	private List<AgentWithServiceDto>	listAgentsEquipe;
	
	@WireVariable
	private DateUtils 					dateUtils;

	@Init
	public void initDemandesAgent() {

		currentUser = (ProfilAgentDto) Sessions.getCurrent().getAttribute("currentUser");

		// init date debut
		setDateDebutFiltre(dateUtils.getCurrentDateMoins2MoisParDefaut());
		
		// on recharge les groupes d'absences pour les filtres
		List<RefGroupeAbsenceDto> filtreGroupeFamille = absWsConsumer.getRefGroupeAbsence();
		setListeGroupeAbsenceFiltre(filtreGroupeFamille);

		// on recharge les états d'absences pour les filtres
		List<RefEtatAbsenceDto> filtreEtat = absWsConsumer.getEtatAbsenceKiosque("NON_PRISES");
		setListeEtatAbsenceFiltre(filtreEtat);
		setTailleListe("5");
		// #14844 liste des acteurs
		try {
			setActeursDto(absWsConsumer.getListeActeurs(currentUser.getAgent().getIdAgent()));
		} catch (Exception e) {
			setActeursDto(new ActeursDto());
		}

		// son equipe
		// l agent lui-meme
		AgentWithServiceDto agent = sirhWsConsumer.getAgentEntite(currentUser.getAgent().getIdAgent(), new Date());
		if (null != agent) {
			getListAgentsEquipe().add(agent);
		}
		// son superieur hierarchique
		AgentWithServiceDto superieur = sirhWsConsumer.getSuperieurHierarchique(currentUser.getAgent().getIdAgent());
		if (null != superieur) {
			superieur = sirhWsConsumer.getAgentEntite(superieur.getIdAgent(), new Date());
		}
		if (null != superieur) {
			getListAgentsEquipe().add(superieur);
		}
		EstChefDto dto = sirhWsConsumer.isAgentChef(currentUser.getAgent().getIdAgent());
		// si l'agent est chef
		// ses equipes
		if (dto.isEstResponsable()) {
			EntiteDto tree = adsWsConsumer.getEntiteWithChildrenByIdEntite(currentUser.getIdServiceAds());
			addAgentsEquipeFromThree(tree);
		} else {
			// sinon l equipe de l agent
			List<AgentWithServiceDto> agentsEquipe = sirhWsConsumer.getAgentEquipe(currentUser.getAgent().getIdAgent(), null);

			for (AgentWithServiceDto ag : agentsEquipe) {
				ag.setIdServiceADS(agent.getIdServiceADS());
				ag.setService(agent.getService());
				getListAgentsEquipe().add(ag);
			}
		}

		// planning #12159
		org.apache.catalina.session.StandardSessionFacade s = (StandardSessionFacade) Executions.getCurrent().getSession().getNativeSession();
		s.setAttribute("listeAgentsEquipe", getListAgentsEquipe());
	}

	private void addAgentsEquipeFromThree(EntiteDto tree) {

		for (AgentWithServiceDto agent : sirhWsConsumer.getAgentEquipe(currentUser.getAgent().getIdAgent(), tree.getIdEntite())) {
			getListAgentsEquipe().add(agent);
		}

		addAgentsEquipeFromThreeRecursive(tree);
	}

	private void addAgentsEquipeFromThreeRecursive(EntiteDto entite) {

		for (EntiteDto entiteEnfant : entite.getEnfants()) {
			for (AgentWithServiceDto agent : sirhWsConsumer.getAgentEquipe(currentUser.getAgent().getIdAgent(), entiteEnfant.getIdEntite())) {
				getListAgentsEquipe().add(agent);
			}
			addAgentsEquipeFromThreeRecursive(entiteEnfant);
		}
	}

	@Command
	@NotifyChange({ "listeTypeAbsenceFiltre", "typeAbsenceFiltre" })
	public void alimenteTypeFamilleAbsence() {
		List<RefTypeAbsenceDto> filtreFamilleAbsence = absWsConsumer.getRefTypeAbsenceKiosque(getGroupeAbsenceFiltre().getIdRefGroupeAbsence(),
				currentUser.getAgent().getIdAgent());
		if (filtreFamilleAbsence.size() == 1) {
			setListeTypeAbsenceFiltre(null);
			setTypeAbsenceFiltre(null);
		} else {
			setListeTypeAbsenceFiltre(filtreFamilleAbsence);
			setTypeAbsenceFiltre(null);
		}
	}

	@Command
	@NotifyChange({ "listeDemandes", "listeEtatAbsenceFiltre" })
	public void changeVue(@BindingParam("tab") Tab tab) {
		setListeDemandes(null);
		List<RefEtatAbsenceDto> filtreEtat = new ArrayList<RefEtatAbsenceDto>();
		// on recharge les états d'absences pour les filtres
		filtreEtat = absWsConsumer.getEtatAbsenceKiosque(tab.getId());

		setListeEtatAbsenceFiltre(filtreEtat);
		// on sauvegarde l'onglet
		setTabCourant(tab);
		filtrer(null, null);
	}

	@Command
	@NotifyChange({ "listeDemandes" })
	public void setTabDebut(@BindingParam("tab") Tab tab) {
		setTabCourant(tab);
		filtrer(null, null);
	}

	@Command
	@NotifyChange({ "*" })
	public void filtrer(@BindingParam("ref") Chosenbox boxEtat, @BindingParam("win") Window window) {
		List<Integer> etats = new ArrayList<Integer>();
		if (boxEtat != null) {
			for (Object etat : boxEtat.getSelectedObjects()) {
				etats.add(((RefEtatAbsenceDto) etat).getIdRefEtat());
			}
		}

		// #12159 construction du planning
		if ("PLANNING".equals(getTabCourant().getId())) {
			// #20359 ne pas retourner les etats annule, rejete, ...
			if (etats.isEmpty()) {
				for (RefEtatAbsenceDto etat : getListeEtatAbsenceFiltre()) {
					etats.add(etat.getIdRefEtat());
				}
			}

			List<DemandeDto> result = absWsConsumer.getListeDemandesForPlanning(getDateDebutFiltre(), getDateFinFiltre(),
					etats.size() == 0 ? null : etats.toString().replace("[", "").replace("]", "").replace(" ", ""),
					getTypeAbsenceFiltre() == null ? null : getTypeAbsenceFiltre().getIdRefTypeAbsence(),
					getGroupeAbsenceFiltre() == null ? null : getGroupeAbsenceFiltre().getIdRefGroupeAbsence(), getListAgentsEquipe());
			setListeDemandes(result);

			org.apache.catalina.session.StandardSessionFacade s = (StandardSessionFacade) Executions.getCurrent().getSession().getNativeSession();

			s.setAttribute("listeDemandes", result);
			s.setAttribute("listeAgentsEquipe", getListAgentsEquipe());
			doAfterCompose(getTabCourant().getFellow("tb").getFellow("tabpanelplanning").getFellow("includeplanning").getFellow("windowplanning"));
		} else {
			ResultListDemandeDto result = absWsConsumer.getDemandesAgent(currentUser.getAgent().getIdAgent(), getTabCourant().getId(),
					getDateDebutFiltre(), getDateFinFiltre(), getDateDemandeFiltre(),
					etats.size() == 0 ? null : etats.toString().replace("[", "").replace("]", "").replace(" ", ""),
					getTypeAbsenceFiltre() == null ? null : getTypeAbsenceFiltre().getIdRefTypeAbsence(),
					getGroupeAbsenceFiltre() == null ? null : getGroupeAbsenceFiltre().getIdRefGroupeAbsence());
			setListeDemandes(null != result ? result.getListDemandesDto() : new ArrayList<DemandeDto>());
		}
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
				if (sdf.format(item.getDateDebut()).contains(getFilter().toLowerCase())) {
					if (!list.contains(item))
						list.add(item);
				}
			}
			setListeDemandes(list);
		} else {
			filtrer(null, null);
		}
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
	@NotifyChange({ "groupeAbsenceFiltre", "listeTypeAbsenceFiltre", "typeAbsenceFiltre", "dateDebutFiltre", "dateFinFiltre", "dateDemandeFiltre",
			"listeEtatAbsenceFiltre" })
	public void viderFiltre() {
		setListeTypeAbsenceFiltre(null);
		setGroupeAbsenceFiltre(null);
		setTypeAbsenceFiltre(null);
		setDateDebutFiltre(null);
		setDateFinFiltre(null);
		setDateDemandeFiltre(null);
		// on recharge les états d'absences pour les filtres
		List<RefEtatAbsenceDto> filtreEtat = absWsConsumer.getEtatAbsenceKiosque(getTabCourant().getId());
		setListeEtatAbsenceFiltre(filtreEtat);
	}

	public List<DemandeDto> getHistoriqueAbsence(DemandeDto abs) {
		List<DemandeDto> result = absWsConsumer.getHistoriqueAbsence(currentUser.getAgent().getIdAgent(), abs.getIdDemande());
		return result;
	}

	@Command
	public void ajouterDemande() {
		// create a window programmatically and use it as a modal dialog.
		Window win = (Window) Executions.createComponents("/absences/agent/ajoutDemandeAgent.zul", null, null);
		win.doModal();
	}

	@Command
	public void openLegende() {
		// create a window programmatically and use it as a modal dialog.
		Window win = (Window) Executions.createComponents("/absences/legendPlanning.zul", null, null);
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

	@Command
	public void annulerDemande(@BindingParam("ref") DemandeDto demande) {
		// create a window programmatically and use it as a modal dialog.
		Map<String, DemandeDto> args = new HashMap<String, DemandeDto>();
		args.put("demandeCourant", demande);
		Window win = (Window) Executions.createComponents("/absences/annulerDemande.zul", null, args);
		win.doModal();
	}

	@Command
	public void imprimerDemande(@BindingParam("ref") DemandeDto demande) {
		// on imprime la demande
		byte[] resp = absWsConsumer.imprimerDemande(currentUser.getAgent().getIdAgent(), demande.getIdDemande());
		Filedownload.save(resp, "application/pdf", "titreAbsence");
	}

	@GlobalCommand
	@NotifyChange({ "listeDemandes" })
	public void refreshListeDemande() {
		filtrer(null, null);
	}

	@Command
	public void exportPDF(@BindingParam("ref") Grid grid) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		PdfExporter exporter = new PdfExporter();
		exporter.export(grid, out);

		AMedia amedia = new AMedia("mesDemandes.pdf", "pdf", "application/pdf", out.toByteArray());
		Filedownload.save(amedia);
		out.close();
	}

	@Command
	public void exportExcel(@BindingParam("ref") Grid grid) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		ExcelExporter exporter = new ExcelExporter();
		exporter.export(grid, out);

		AMedia amedia = new AMedia("mesDemandes.xlsx", "xls", "application/file", out.toByteArray());
		Filedownload.save(amedia);
		out.close();
	}

	public String dateSaisieToString(Date dateSaisie) {
		SimpleDateFormat sdfJour = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfHeure = new SimpleDateFormat("HH:mm");
		return sdfJour.format(dateSaisie) + " à " + sdfHeure.format(dateSaisie);
	}

	public String concatAgent(String nom, String prenom) {
		return nom + " " + prenom;
	}

	public String concatAgentApprobateur(ApprobateurDto approbateur) {
		String approb = approbateur.getApprobateur().getNom() + " " + approbateur.getApprobateur().getPrenom();
		// on regarde si il y a un delegataire
		if (approbateur.getDelegataire() != null) {
			approb += " (Délégataire : " + approbateur.getDelegataire().getNom() + " " + approbateur.getDelegataire().getPrenom() + ")";
		}
		return approb;
	}

	public String concatDelegataire(String nom, String prenom) {
		if ((null == nom || "".equals(nom)) && (null == prenom || "".equals(prenom))) {
			return "Délégataire : aucun";
		} else {
			return "Délégataire : " + nom + " " + prenom;
		}
	}

	public String getEtatToString(Integer idRefEtat) {
		return RefEtatEnum.getRefEtatEnum(idRefEtat).getLibEtat();
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
				// #32281 : si on est MALADIE AT, alors dans durée, on affiche
				// le nombre de jours ITT
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
		List<AgentWithServiceDto> listeAgentsEquipe = (List<AgentWithServiceDto>) s.getAttribute("listeAgentsEquipe");

		CustomDHXPlanner planner = new CustomDHXPlanner("./codebase/", DHXSkin.TERRACE, "eventsDemandesAgent", listeAgentsEquipe);
		try {
			Executions.createComponentsDirectly(planner.render(), null, comp.getFellow("div"), null);
		} catch (Exception e) {
			logger.debug("Une erreur est survenue dans la creation du planning : " + e.getMessage());
		}
	}

	@RequestMapping("/eventsDemandesAgent")
	@ResponseBody
	public String events(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		List<DemandeDto> listDemandes = (List<DemandeDto>) request.getSession().getAttribute("listeDemandes");

		@SuppressWarnings("unchecked")
		List<AgentWithServiceDto> listeAgentsEquipe = (List<AgentWithServiceDto>) request.getSession().getAttribute("listeAgentsEquipe");

		CustomEventsManager evs = new CustomEventsManager(request, listDemandes, listeAgentsEquipe);
		return evs.run();
	}

	// ///////////////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// FIN PARTIE PLANNING
	// ////////////////////////////////////
	// ///////////////////////////////////////////////////////////////////////////////////////

	public List<DemandeDto> getListeDemandes() {
		return listeDemandes;
	}

	public void setListeDemandes(List<DemandeDto> listeDemandes) {
		this.listeDemandes = listeDemandes;
	}

	public RefTypeAbsenceDto getTypeAbsenceFiltre() {
		return typeAbsenceFiltre;
	}

	public void setTypeAbsenceFiltre(RefTypeAbsenceDto typeAbsenceFiltre) {
		this.typeAbsenceFiltre = typeAbsenceFiltre;
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

	public List<RefTypeAbsenceDto> getListeTypeAbsenceFiltre() {
		return listeTypeAbsenceFiltre;
	}

	public void setListeTypeAbsenceFiltre(List<RefTypeAbsenceDto> listeTypeAbsenceFiltre) {
		this.listeTypeAbsenceFiltre = listeTypeAbsenceFiltre;
	}

	public List<RefEtatAbsenceDto> getListeEtatAbsenceFiltre() {
		return listeEtatAbsenceFiltre;
	}

	public void setListeEtatAbsenceFiltre(List<RefEtatAbsenceDto> listeEtatAbsenceFiltre) {
		this.listeEtatAbsenceFiltre = listeEtatAbsenceFiltre;
	}

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

	@NotifyChange
	public void setTailleListe(String tailleListe) {
		this.tailleListe = tailleListe;
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

	public ActeursDto getActeursDto() {
		return acteursDto;
	}

	public void setActeursDto(ActeursDto acteursDto) {
		this.acteursDto = acteursDto;
	}

	public List<AgentWithServiceDto> getListAgentsEquipe() {
		if (null == listAgentsEquipe) {
			listAgentsEquipe = new ArrayList<AgentWithServiceDto>();
		}
		return listAgentsEquipe;
	}

	public void setListAgentsEquipe(List<AgentWithServiceDto> listAgentsEquipe) {
		this.listAgentsEquipe = listAgentsEquipe;
	}

}
