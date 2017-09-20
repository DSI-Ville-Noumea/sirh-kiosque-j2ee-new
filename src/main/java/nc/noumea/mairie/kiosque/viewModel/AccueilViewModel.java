package nc.noumea.mairie.kiosque.viewModel;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkmax.zul.Portallayout;
import org.zkoss.zul.Div;
import org.zkoss.zul.Panel;

import nc.noumea.mairie.kiosque.abs.dto.DemandeDto;
import nc.noumea.mairie.kiosque.abs.dto.RefEtatEnum;
import nc.noumea.mairie.kiosque.abs.dto.ResultListDemandeDto;
import nc.noumea.mairie.kiosque.dto.AccueilRhDto;
import nc.noumea.mairie.kiosque.dto.ReferentRhDto;
import nc.noumea.mairie.kiosque.dto.ReturnMessageDto;
import nc.noumea.mairie.kiosque.ptg.dto.ConsultPointageDto;
import nc.noumea.mairie.kiosque.ptg.dto.EtatPointageEnum;
import nc.noumea.mairie.ws.ISirhEaeWSConsumer;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AccueilViewModel extends AbstractViewModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1273275293239509746L;

	private Logger logger = LoggerFactory.getLogger(AccueilViewModel.class);

	@WireVariable
	private ISirhEaeWSConsumer eaeWsConsumer;

	private List<AccueilRhDto> listeTexteAccueil;

	private List<String> listeAlerteAccueil;

	private String refrentRh;

	private String nombreAbsenceAApprouver = "";

	private String nombreAbsenceAViser = "";

	private String nombrePointageAApprouver = "";

	private String nombreEAEaRealiser = "";

	@Wire
	private Portallayout portalLayout;

	@Listen("onPortalMove = #portalLayout")
	public void saveStatus() {
		int i = 0;
		for (Component portalChild : portalLayout.getChildren()) {
			List<String> portletIds = new ArrayList<String>();
			for (Component portlet : portalChild.getChildren())
				portletIds.add(portlet.getId());
			Executions.getCurrent().getSession().setAttribute("PortalChildren" + i++, portletIds);
		}
	}

	@Listen("onCreate = #portalLayout")
	public void initStatus() {

		List<? extends Component> panelchildren = portalLayout.getChildren();
		for (int i = 0; i < panelchildren.size(); i++) {
			@SuppressWarnings("unchecked")
			List<String> panelIds = (List<String>) Executions.getCurrent().getSession().getAttribute("PortalChildren" + i);
			if (panelIds != null) {
				for (String panelId : panelIds) {
					Panel newPanel = (Panel) portalLayout.getFellow(panelId);
					if (panelchildren.size() > 0)
						panelchildren.get(i).insertBefore(newPanel, panelchildren.get(0));
					else
						newPanel.setParent(panelchildren.get(i));

				}
			}
		}
	}

	@Init
	public void initAccueil() {

		// message d'accueil
		List<AccueilRhDto> listeTexte = sirhWsConsumer.getListeTexteAccueil();
		setListeTexteAccueil(new ArrayList<AccueilRhDto>());
		for (AccueilRhDto t : listeTexte) {
			t.setTexteAccueilKiosque(t.getTexteAccueilKiosque().replace("&quot;", "\""));
			getListeTexteAccueil().add(t);
		}

		// alertes d'accueil
		ReturnMessageDto alertes = sirhWsConsumer.getAlerteRHByAgent(getCurrentUser().getAgent().getIdAgent());
		setListeAlerteAccueil(new ArrayList<String>());
		for (String t : alertes.getInfos()) {
			getListeAlerteAccueil().add(t.replace("&quot;", "\""));
		}
		// setListeTexteAccueil(listeTexte);
		// refrent Rh de l'agent
		List<ReferentRhDto> listReferent = sirhWsConsumer.getListReferentRH(getCurrentUser().getAgent().getIdAgent());
		String ref = "";
		for (int i = 0; i < listReferent.size(); i++) {
			ReferentRhDto referent = listReferent.get(i);
			if (i == 0) {
				ref += getPrenomReferent(referent.getPrenomAgentReferent()) + " au " + referent.getNumeroTelephone();
			} else {
				ref += " ou " + getPrenomReferent(referent.getPrenomAgentReferent()) + " au " + referent.getNumeroTelephone();
			}
		}

		setRefrentRh(ref);

		if (null != getDroitsAbsence() && getDroitsAbsence().isApprouverModif()) {

			logger.debug("Agent " + getCurrentUser().getAgent().getIdAgent() + " a les droits de Approbateur Absence");
			
			List<Integer> etats = new ArrayList<Integer>();
			etats.add(RefEtatEnum.SAISIE.getCodeEtat());
			etats.add(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
			etats.add(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());

			ResultListDemandeDto result = absWsConsumer.getListeDemandes(getCurrentUser().getAgent().getIdAgent(), "NON_PRISES", null, null, null, etats.toString().replace("[", "").replace("]", "")
					.replace(" ", ""), null, null, null, null);
			Integer nbrAbs = 0;
			if(null != result) {
				for (DemandeDto dto : result.getListDemandesDto()) {
					if (dto.isModifierApprobation()) {
						nbrAbs++;
					}
				}
			}

			String nbrAbsStr = "";
			if (0 == nbrAbs) {
				nbrAbsStr = "Vous n'avez pas de demande d'absence à approuver.";
			} else if (1 == nbrAbs) {
				nbrAbsStr = "Vous avez " + nbrAbs + " demande d'absence à approuver.";
			} else {
				nbrAbsStr = "Vous avez " + nbrAbs + " demandes d'absence à approuver.";
			}
			setNombreAbsenceAApprouver(nbrAbsStr);
		}

		if (null != getDroitsAbsence() && getDroitsAbsence().isViserModif()) {

			logger.debug("Agent " + getCurrentUser().getAgent().getIdAgent() + " a les droits de viseur Absence");
			
			List<Integer> etats = new ArrayList<Integer>();
			etats.add(RefEtatEnum.SAISIE.getCodeEtat());

			ResultListDemandeDto result = absWsConsumer.getListeDemandes(getCurrentUser().getAgent().getIdAgent(), "NON_PRISES", null, null, null, etats.toString().replace("[", "").replace("]", "")
					.replace(" ", ""), null, null, null, null);
			Integer nbrAbs = 0;
			for (DemandeDto dto : result.getListDemandesDto()) {
				if (dto.isModifierVisa()) {
					nbrAbs++;
				}
			}

			String nbrAbsViserStr = "";
			if (0 == nbrAbs) {
				nbrAbsViserStr = "Vous n'avez pas de demande d'absence à viser.";
			} else if (1 == nbrAbs) {
				nbrAbsViserStr = "Vous avez " + nbrAbs + " demande d'absence à viser.";
			} else {
				nbrAbsViserStr = "Vous avez " + nbrAbs + " demandes d'absence à viser.";
			}
			setNombreAbsenceAViser(nbrAbsViserStr);
		}
		if (isDroitsEae()) {
			
			logger.debug("Agent " + getCurrentUser().getAgent().getIdAgent() + " a les droits EAE");
			
			String nbrEaeStr = "";
			
			Integer nbrEae = null;
			try {
				nbrEae = new Integer(eaeWsConsumer.countEaeARealiserUrl(getCurrentUser().getAgent().getIdAgent()));
			} catch (Exception e) {
				// l'appli SIRH-EAE-WS ne semble pas répondre
				logger.error("L'application SIRH-EAE-WS ne répond pas.");
				nbrEaeStr = "L'application SIRH-EAE-WS ne répond pas.";
			}
			
			if(null != nbrEae) {
				if (0 == nbrEae) {
					nbrEaeStr = "Vous n'avez pas de EAE à réaliser.";
				} else if (1 == nbrEae) {
					nbrEaeStr = "Vous avez " + nbrEae + " EAE à réaliser.";
				} else {
					nbrEaeStr = "Vous avez " + nbrEae + " EAEs à réaliser.";
				}
			}
			
			setNombreEAEaRealiser(nbrEaeStr);
		}
		if (null != getDroitsPointage() && getDroitsPointage().isApprobation()) {
			
			logger.debug("Agent " + getCurrentUser().getAgent().getIdAgent() + " a les droits d'approbation Pointage");
			
			Date toDate = new Date();
			DateTime fromDate = new DateTime(toDate);
			fromDate = fromDate.minusMonths(3);

			List<ConsultPointageDto> listPtg = ptgWsConsumer.getListePointages(getCurrentUser().getAgent().getIdAgent(), fromDate.toDate(), toDate, null, null, EtatPointageEnum.SAISI.getCodeEtat(),
					null, null);
			int nbrPtg = 0;
			if (listPtg != null) {
				for (ConsultPointageDto ptgDto : listPtg) {
					if (ptgDto.isApprobation())
						nbrPtg++;
				}
			}
			String nbrPtgStr = "";
			if (0 == nbrPtg) {
				nbrPtgStr = "Vous n'avez pas de pointage à approuver.";
			} else if (1 == nbrPtg) {
				nbrPtgStr = "Vous avez " + nbrPtg + " pointage à approuver.";
			} else {
				nbrPtgStr = "Vous avez " + nbrPtg + " pointages à approuver.";
			}
			setNombrePointageAApprouver(nbrPtgStr);
		}
	}

	private String getPrenomReferent(String prenom) {
		if (!prenom.equals("")) {
			String premierLettre = prenom.substring(0, 1).toUpperCase();
			String reste = prenom.substring(1, prenom.length()).toLowerCase();
			return premierLettre + reste;
		}
		return "";
	}

	@Command
	public void changeEcran(@BindingParam("page") String page, @BindingParam("ecran") Div div, @BindingParam("param") String param) {
		div.getChildren().clear();
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("div", div);
		args.put("param", param);

		Executions.createComponents(page + ".zul", div, args);
	}

	public List<AccueilRhDto> getListeTexteAccueil() {
		return listeTexteAccueil;
	}

	public void setListeTexteAccueil(List<AccueilRhDto> listeTexteAccueil) {
		this.listeTexteAccueil = listeTexteAccueil;
	}

	public String getRefrentRh() {
		return refrentRh;
	}

	public void setRefrentRh(String refrentRh) {
		this.refrentRh = refrentRh;
	}

	public String getNombreAbsenceAApprouver() {
		return nombreAbsenceAApprouver;
	}

	public void setNombreAbsenceAApprouver(String nombreAbsenceAApprouver) {
		this.nombreAbsenceAApprouver = nombreAbsenceAApprouver;
	}

	public String getNombrePointageAApprouver() {
		return nombrePointageAApprouver;
	}

	public void setNombrePointageAApprouver(String nombrePointageAApprouver) {
		this.nombrePointageAApprouver = nombrePointageAApprouver;
	}

	public String getNombreEAEaRealiser() {
		return nombreEAEaRealiser;
	}

	public void setNombreEAEaRealiser(String nombreEAEaRealiser) {
		this.nombreEAEaRealiser = nombreEAEaRealiser;
	}

	public String getNombreAbsenceAViser() {
		return nombreAbsenceAViser;
	}

	public void setNombreAbsenceAViser(String nombreAbsenceAViser) {
		this.nombreAbsenceAViser = nombreAbsenceAViser;
	}

	public List<String> getListeAlerteAccueil() {
		return listeAlerteAccueil;
	}

	public void setListeAlerteAccueil(List<String> listeAlerteAccueil) {
		this.listeAlerteAccueil = listeAlerteAccueil;
	}

}
