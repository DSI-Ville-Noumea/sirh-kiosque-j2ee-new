package nc.noumea.mairie.kiosque.ptg.viewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Tab;

import nc.noumea.mairie.ads.dto.EntiteDto;
import nc.noumea.mairie.kiosque.dto.AgentDto;
import nc.noumea.mairie.kiosque.dto.AgentWithServiceDto;
import nc.noumea.mairie.kiosque.dto.ReturnMessageDto;
import nc.noumea.mairie.kiosque.ptg.dto.EtatPointageEnum;
import nc.noumea.mairie.kiosque.ptg.dto.RefEtatPointageDto;
import nc.noumea.mairie.kiosque.ptg.dto.TitreRepasDemandeDto;
import nc.noumea.mairie.kiosque.validation.ValidationMessage;
import nc.noumea.mairie.kiosque.viewModel.AbstractViewModel;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class GestionTitreRepasViewModel extends AbstractViewModel {

	/**
	 * 
	 */
	private static final long			serialVersionUID	= 8405659633984953740L;

	private Tab							tabCourant;
	// liste pour historique
	private List<TitreRepasDemandeDto>	listeTitreRepas;
	// liste pour la saisie
	private List<TitreRepasDemandeDto>	listeTitreRepasSaisie;

	/* POUR LES FILTRES */
	private List<EntiteDto>				listeServicesFiltre;
	private EntiteDto					serviceFiltre;
	private Date						dateDebutFiltre;
	private Date						dateFinFiltre;
	private String						commandeFiltre;
	private List<RefEtatPointageDto>	listeEtatTitreRepasFiltre;
	private RefEtatPointageDto			etatTitreRepasFiltre;
	private List<AgentDto>				listeAgentsFiltre;
	private AgentDto					agentFiltre;

	/* POUR LE HAUT DU TABLEAU */
	private String						filter;
	private String						tailleListe;

	private boolean						checkAll;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
	private SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat sdfSaisie = new SimpleDateFormat("dd/MM/yyyy à hh:mm");

	@Init
	public void initGestionTitreRepas() {
		// on recharge les états d'absences pour les filtres
		List<RefEtatPointageDto> filtreEtat = ptgWsConsumer.getEtatTitreRepasKiosque();
		setListeEtatTitreRepasFiltre(filtreEtat);
		setTailleListe("5");

		// on charge les service pour les filtres
		List<EntiteDto> filtreService = ptgWsConsumer.getServicesPointages(getCurrentUser().getAgent().getIdAgent());

		EntiteDto tousLesServices = new EntiteDto();
		tousLesServices.setSigle("TousLesServices");
		tousLesServices.setLabelCourt("Tous les services");
		List<EntiteDto> filtreAllService = new ArrayList<>();
		filtreAllService.add(tousLesServices);
		filtreAllService.addAll(filtreService);
		setListeServicesFiltre(filtreAllService);
	}

	@Command
	@NotifyChange({ "listeDemandes" })
	public void setTabDebut(@BindingParam("tab") Tab tab) {
		setTabCourant(tab);
	}
	
	private void failOnEmptyList() {
		final HashMap<String, Object> map = new HashMap<String, Object>();
		List<ValidationMessage> listErreur = new ArrayList<ValidationMessage>();
		listErreur.add(new ValidationMessage("Il n'y a aucun élément à exporter."));
		map.put("errors", listErreur);
		Executions.createComponents("/messages/returnMessage.zul", null, map);
	}

	/**
	 * Cet export se fait sans passer par ExcelExporter.java, 
	 * car seules les données visibles dans l'IHM se retrouvent dans l'excel si on exporte l'objet grid. (cf. redmine #43486)
	 * => Création d'un export spécifique
	 * 
	 * @throws IOException
	 */
	@Command
	public void exportExcel() throws IOException {
		if (listeTitreRepas == null || listeTitreRepas.size() == 0) {
			failOnEmptyList();
			return;
		}
		
		Workbook wb = new HSSFWorkbook();
		Sheet feuillePrincipale = wb.createSheet("Liste SPV");

		// Alimentation des en-têtes
		Row rowEntete = feuillePrincipale.createRow((short) 0);
		rowEntete.createCell(0).setCellValue("Agent");
		rowEntete.createCell(1).setCellValue("Mois");
		rowEntete.createCell(2).setCellValue("Commandé");
		rowEntete.createCell(3).setCellValue("Commentaires");
		rowEntete.createCell(4).setCellValue("Etat");
		rowEntete.createCell(5).setCellValue("Date de saisie");
		rowEntete.createCell(6).setCellValue("Opérateur");

		int numeroLigne = 1;
		
		// Alimentation des données
		for (TitreRepasDemandeDto tr : listeTitreRepas) {
			Row row = feuillePrincipale.createRow((short) numeroLigne);
			row.createCell(0).setCellValue(concatAgentNomatr(tr.getAgent()));
			row.createCell(1).setCellValue(sdf.format(tr.getDateMonth()));
			row.createCell(2).setCellValue(booleanToString(tr.getCommande()));
			row.createCell(3).setCellValue(tr.getCommentaire());
			row.createCell(4).setCellValue(etatToString(tr.getIdRefEtat()));
			row.createCell(5).setCellValue(sdfSaisie.format(tr.getDateSaisie()));
			row.createCell(6).setCellValue(concatAgent(tr.getOperateur()));

			numeroLigne++;
		}

		// Aucolumns
		for (int i = 0; i <= 8; i++) {
			feuillePrincipale.autoSizeColumn(i);
		}

		File tempFile = File.createTempFile("tempFile", ".xls");
		FileOutputStream fileStream = new FileOutputStream(tempFile);
		wb.write(fileStream);
		FileInputStream fileInputStream = new FileInputStream(tempFile);

		// Création et sauvegarde du fichier
		String nomFichier = "gestionTitreRepas_"+sdfFileName.format(new Date()) +".xls";
		Filedownload.save(IOUtils.toByteArray(fileInputStream), null, nomFichier);
	}

	@Command
	@NotifyChange({ "listeTitreRepasSaisie", "listeAgentsFiltre", "checkAll" })
	public void chargeTabSaisie() {

		setListeTitreRepasSaisie(null);
		setCheckAll(false);
		if (getServiceFiltre() != null) {

			if (null == getAgentFiltre()) {
				// on charge les agents pour les filtres
				List<AgentDto> filtreAgent = ptgWsConsumer.getAgentsPointages(getCurrentUser().getAgent().getIdAgent(),
						getServiceFiltre().getIdEntite());
				setListeAgentsFiltre(filtreAgent);

				// on recupere les demandes deja saisies
				List<TitreRepasDemandeDto> resultDejaSaisie = ptgWsConsumer.getListTitreRepas(getCurrentUser().getAgent().getIdAgent(), null, null,
						getServiceFiltre() == null ? null : getServiceFiltre().getIdEntite(),
						getAgentFiltre() == null ? null : getAgentFiltre().getIdAgent(), null, getDatePremierJourOfMonthSuivant(new Date()),getCommandeFiltre() == null ? null : getCommandeFiltre().equals("oui") ? true : false);
				List<AgentDto> listExist = new ArrayList<AgentDto>();
				for (TitreRepasDemandeDto dto : resultDejaSaisie) {
					listExist.add(dto.getAgent());
				}

				List<TitreRepasDemandeDto> result = new ArrayList<TitreRepasDemandeDto>();
				for (AgentDto ag : getListeAgentsFiltre()) {
					if (listExist.contains(ag)) {
						List<TitreRepasDemandeDto> resultAgent = ptgWsConsumer.getListTitreRepas(getCurrentUser().getAgent().getIdAgent(), null, null,
								getServiceFiltre() == null ? null : getServiceFiltre().getIdEntite(), ag.getIdAgent(), null,
								getDatePremierJourOfMonthSuivant(new Date()),getCommandeFiltre() == null ? null : getCommandeFiltre().equals("oui") ? true : false);
						if (resultAgent != null && resultAgent.size() == 1) {
							result.add(resultAgent.get(0));
						} else {
							TitreRepasDemandeDto dtoSaisie = new TitreRepasDemandeDto();
							dtoSaisie.setAgent(new AgentWithServiceDto(ag, null, null));
							dtoSaisie.setCommande(false);
							dtoSaisie.setIdRefEtat(EtatPointageEnum.SAISI.getCodeEtat());
							result.add(dtoSaisie);
						}
					} else {
						TitreRepasDemandeDto dtoSaisie = new TitreRepasDemandeDto();
						dtoSaisie.setAgent(new AgentWithServiceDto(ag, null, null));
						dtoSaisie.setCommande(false);
						dtoSaisie.setIdRefEtat(EtatPointageEnum.SAISI.getCodeEtat());
						result.add(dtoSaisie);
					}
				}
				setListeTitreRepasSaisie(result);
			} else {
				List<TitreRepasDemandeDto> result = new ArrayList<TitreRepasDemandeDto>();

				List<TitreRepasDemandeDto> resultAgent = ptgWsConsumer.getListTitreRepas(getCurrentUser().getAgent().getIdAgent(), null, null,
						getServiceFiltre() == null ? null : getServiceFiltre().getIdEntite(), getAgentFiltre().getIdAgent(), null,
						getDatePremierJourOfMonthSuivant(new Date()),getCommandeFiltre() == null ? null : getCommandeFiltre().equals("oui") ? true : false);
				if (resultAgent != null && resultAgent.size() == 1) {
					result.add(resultAgent.get(0));
				} else {
					TitreRepasDemandeDto dtoSaisie = new TitreRepasDemandeDto();
					dtoSaisie.setAgent(new AgentWithServiceDto(getAgentFiltre(), null, null));
					dtoSaisie.setCommande(false);
					dtoSaisie.setIdRefEtat(EtatPointageEnum.SAISI.getCodeEtat());
					result.add(dtoSaisie);
				}
				setListeTitreRepasSaisie(result);
			}
		}
	}

	@Command
	@NotifyChange({ "listeTitreRepasSaisie" })
	public void saveListeTitreRepasSaisie() {
		ReturnMessageDto result = ptgWsConsumer.setTitreRepas(getCurrentUser().getAgent().getIdAgent(), getListeTitreRepasSaisie());
		// on recharge les infos
		chargeTabSaisie();

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

	@Command
	@NotifyChange({ "listeTitreRepasSaisie" })
	public void doCheckedAll(@BindingParam("ref") List<TitreRepasDemandeDto> listDto, @BindingParam("box") Checkbox box) {
		for (TitreRepasDemandeDto dto : getListeTitreRepasSaisie()) {
			if (box.isChecked()) {
				dto.setCommande(true);
			} else {
				dto.setCommande(false);
			}
		}
	}

	private Date getDatePremierJourOfMonthSuivant(Date dateMonth) {

		DateTime date = new DateTime(dateMonth).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
				.plusMonths(1);

		return date.toDate();
	}

	@Command
	@NotifyChange({ "*" })
	public void filtrer() {
		List<TitreRepasDemandeDto> result = ptgWsConsumer.getListTitreRepas(getCurrentUser().getAgent().getIdAgent(), getDateDebutFiltre(),
				getDateFinFiltre(), getServiceFiltre() == null ? null : getServiceFiltre().getIdEntite(),
				getAgentFiltre() == null ? null : getAgentFiltre().getIdAgent(),
				getEtatTitreRepasFiltre() == null ? null : getEtatTitreRepasFiltre().getIdRefEtat(), null,getCommandeFiltre() == null ? null : getCommandeFiltre().equals("oui") ? true : false);
		setListeTitreRepas(result);
	}
	
	@Command
	@NotifyChange({ "listeTitreRepas", "listeTitreRepasSaisie" })
	public void doSearch() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		List<TitreRepasDemandeDto> list = new ArrayList<TitreRepasDemandeDto>();
		if (getFilter() != null && !"".equals(getFilter()) && getListeTitreRepas() != null) {
			for (TitreRepasDemandeDto item : getListeTitreRepas()) {
				if (sdf.format(item.getDateMonth()).contains(getFilter().toLowerCase())) {
					if (!list.contains(item))
						list.add(item);
				}
				if (item.getAgent().getNom().toLowerCase().contains(getFilter().toLowerCase())) {
					if (!list.contains(item))
						list.add(item);
				}
				if (item.getAgent().getPrenom().toLowerCase().contains(getFilter().toLowerCase())) {
					if (!list.contains(item))
						list.add(item);
				}
				if (item.getOperateur().getNom().toLowerCase().contains(getFilter().toLowerCase())) {
					if (!list.contains(item))
						list.add(item);
				}
				if (item.getOperateur().getPrenom().toLowerCase().contains(getFilter().toLowerCase())) {
					if (!list.contains(item))
						list.add(item);
				}
			}
			setListeTitreRepas(list);
		} else {
			filtrer();
		}
	}

	public String getPhraseTitreRepas() {
		return "Saisie des titres repas pour le mois " + getMonth(new DateTime().plusMonths(1).getMonthOfYear());
	}

	private String getMonth(int monthOfYear) {
		String monthString = null;
		switch (monthOfYear) {
			case 1:
				monthString = "de janvier";
				break;
			case 2:
				monthString = "de février";
				break;
			case 3:
				monthString = "de mars";
				break;
			case 4:
				monthString = "d'avril";
				break;
			case 5:
				monthString = "de mai";
				break;
			case 6:
				monthString = "de juin";
				break;
			case 7:
				monthString = "de juillet";
				break;
			case 8:
				monthString = "d'aôut";
				break;
			case 9:
				monthString = "de septembre";
				break;
			case 10:
				monthString = "d'octobre";
				break;
			case 11:
				monthString = "de novembre";
				break;
			case 12:
				monthString = "de décembre";
				break;
		}
		return monthString;
	}

	@Command
	@NotifyChange({ "dateDebutFiltre","commandeFiltre", "serviceFiltre", "dateFinFiltre", "agentFiltre", "etatTitreRepasFiltre", "listeTitreRepasSaisie" })
	public void viderFiltre() {
		setDateDebutFiltre(null);
		setDateFinFiltre(null);
		setServiceFiltre(null);
		setAgentFiltre(null);
		setListeAgentsFiltre(null);
		setEtatTitreRepasFiltre(null);
		setListeTitreRepasSaisie(null);
		setCommandeFiltre(null);
	}

	@Command
	@NotifyChange({ "listeAgentsFiltre" })
	public void afficheListeAgent() {
		// on charge les agents pour les filtres
		List<AgentDto> filtreAgent = ptgWsConsumer.getAgentsPointages(getCurrentUser().getAgent().getIdAgent(), getServiceFiltre().getIdEntite());
		setListeAgentsFiltre(filtreAgent);
	}

	public List<TitreRepasDemandeDto> getHistoriqueTitreRepas(TitreRepasDemandeDto dto) {
		List<TitreRepasDemandeDto> result = ptgWsConsumer.getHistoriqueTitreRepas(dto.getIdTrDemande());
		return result;
	}

	public String concatAgent(AgentDto ag) {
		return ag == null ? "" : ag.getNom() + " " + ag.getPrenom();
	}

	public String concatAgentNomatr(AgentDto ag) {
		String nomatr = ag.getIdAgent().toString().substring(3, ag.getIdAgent().toString().length());
		return ag.getNom() + " " + ag.getPrenom() + " (" + nomatr + ")";
	}

	public String dateSaisieToString(Date date) {
		SimpleDateFormat sdfJour = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat sdfHeure = new SimpleDateFormat("HH:mm");
		return sdfJour.format(date) + " à " + sdfHeure.format(date);
	}

	public String etatToString(Integer idRefEtat) {
		return EtatPointageEnum.getEtatPointageEnum(idRefEtat).getLibEtat();
	}

	public String booleanToString(boolean commande) {
		return commande ? "oui" : "non";
	}

	@Command
	@NotifyChange({ "*" })
	public void changeVue(@BindingParam("tab") Tab tab) {
		viderFiltre();
		// on sauvegarde l'onglet
		setTabCourant(tab);
	}

	public Tab getTabCourant() {
		return tabCourant;
	}

	public void setTabCourant(Tab tabCourant) {
		this.tabCourant = tabCourant;
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

	public List<RefEtatPointageDto> getListeEtatTitreRepasFiltre() {
		return listeEtatTitreRepasFiltre;
	}

	public void setListeEtatTitreRepasFiltre(List<RefEtatPointageDto> listeEtatTitreRepasFiltre) {
		this.listeEtatTitreRepasFiltre = listeEtatTitreRepasFiltre;
	}

	public RefEtatPointageDto getEtatTitreRepasFiltre() {
		return etatTitreRepasFiltre;
	}

	public void setEtatTitreRepasFiltre(RefEtatPointageDto etatTitreRepasFiltre) {
		this.etatTitreRepasFiltre = etatTitreRepasFiltre;
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

	public List<TitreRepasDemandeDto> getListeTitreRepas() {
		return listeTitreRepas;
	}

	public void setListeTitreRepas(List<TitreRepasDemandeDto> listeTitreRepas) {
		this.listeTitreRepas = listeTitreRepas;
	}

	public List<EntiteDto> getListeServicesFiltre() {
		return listeServicesFiltre;
	}

	public void setListeServicesFiltre(List<EntiteDto> listeServicesFiltre) {
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
		this.listeAgentsFiltre = listeAgentsFiltre;
	}

	public AgentDto getAgentFiltre() {
		return agentFiltre;
	}

	public void setAgentFiltre(AgentDto agentFiltre) {
		this.agentFiltre = agentFiltre;
	}

	public List<TitreRepasDemandeDto> getListeTitreRepasSaisie() {
		return listeTitreRepasSaisie;
	}

	public void setListeTitreRepasSaisie(List<TitreRepasDemandeDto> listeTitreRepasSaisie) {
		this.listeTitreRepasSaisie = listeTitreRepasSaisie;
	}

	public boolean isCheckAll() {
		return checkAll;
	}

	public void setCheckAll(boolean checkAll) {
		this.checkAll = checkAll;
	}

	public String getCommandeFiltre() {
		return commandeFiltre;
	}

	public void setCommandeFiltre(String commandeFiltre) {
		this.commandeFiltre = commandeFiltre;
	}

}
