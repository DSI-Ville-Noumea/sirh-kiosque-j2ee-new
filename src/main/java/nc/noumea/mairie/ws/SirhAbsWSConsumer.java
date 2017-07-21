package nc.noumea.mairie.ws;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sun.jersey.api.client.ClientResponse;

import flexjson.JSONSerializer;
import nc.noumea.mairie.ads.dto.EntiteDto;
import nc.noumea.mairie.kiosque.abs.dto.AccessRightsAbsDto;
import nc.noumea.mairie.kiosque.abs.dto.ActeursDto;
import nc.noumea.mairie.kiosque.abs.dto.AgentJoursFeriesGardeDto;
import nc.noumea.mairie.kiosque.abs.dto.CompteurDto;
import nc.noumea.mairie.kiosque.abs.dto.ControleMedicalDto;
import nc.noumea.mairie.kiosque.abs.dto.DemandeDto;
import nc.noumea.mairie.kiosque.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.kiosque.abs.dto.FiltreSoldeDto;
import nc.noumea.mairie.kiosque.abs.dto.HistoriqueSoldeDto;
import nc.noumea.mairie.kiosque.abs.dto.InputterDto;
import nc.noumea.mairie.kiosque.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.kiosque.abs.dto.MotifRefusDto;
import nc.noumea.mairie.kiosque.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.kiosque.abs.dto.PieceJointeDto;
import nc.noumea.mairie.kiosque.abs.dto.RefEtatAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeDto;
import nc.noumea.mairie.kiosque.abs.dto.SaisieGardeDto;
import nc.noumea.mairie.kiosque.abs.dto.SoldeDto;
import nc.noumea.mairie.kiosque.abs.dto.ViseursDto;
import nc.noumea.mairie.kiosque.dto.AgentDto;
import nc.noumea.mairie.kiosque.dto.AgentWithServiceDto;
import nc.noumea.mairie.kiosque.dto.ReturnMessageAbsDto;
import nc.noumea.mairie.kiosque.dto.ReturnMessageDto;
import nc.noumea.mairie.kiosque.transformer.MSDateTransformer;

@Service("absWsConsumer")
public class SirhAbsWSConsumer extends BaseWsConsumer implements ISirhAbsWSConsumer {

	@Autowired
	@Qualifier("sirhAbsWsBaseUrl")
	private String sirhAbsWsBaseUrl;

	/* Droits */
	private static final String sirhDroitsAgentUrl = "droits/listeDroitsAgent";
	private static final String sirhAgentApprobateurUrl = "droits/agentsApprouves";
	private static final String sirhOperateursDelegataireApprobateurUrl = "droits/inputter";
	private static final String sirhViseursApprobateurUrl = "droits/viseur";
	private static final String sirhAgentsOperateurUrl = "droits/agentsSaisisByOperateur";
	private static final String sirhAgentsViseurUrl = "droits/agentsSaisisByViseur";
	private static final String sirhAbsListeActeursUrl = "droits/listeActeurs";

	/* Solde */
	private static final String sirhAgentSoldeUrl = "solde/soldeAgent";
	private static final String sirhHistoriqueCompteurAgentUrl = "solde/historiqueSolde";

	/* Demandes */
	private static final String sirhChangerEtatDemandesAgentUrl = "demandes/changerEtats";
	private static final String sirhDeleteDemandesAgentUrl = "demandes/deleteDemande";
	private static final String sirhSaveDemandesAgentUrl = "demandes/demande";
	private static final String sirhDemandesAgentUrl = "demandes/listeDemandesAgent";
	private static final String sirhPrintDemandesAgentUrl = "edition/downloadTitreDemande";
	private static final String sirhListeDemandesUrl = "demandes/listeDemandes";
	private static final String sirhPersistDemandeControleMedicalUrl = "demandes/persistDemandeControleMedical";
	private static final String sirhGetDemandeControleMedicalUrl = "demandes/getDemandeControleMedical";
	private static final String	sirhAbsDemandesATForAgent							= "demandes/listeATReferenceForAgent";
	
	// utilise pour le planning afin de passer la liste des agents dans l appel
	// au WS
	private static final String sirhListeDemandesPlanningKiosqueUrl = "demandes/listeDemandesPlanningKiosque";
	private static final String sirhListeMotifsRefusUrl = "motif/getListeMotif";
	private static final String sirhHistoriqueAbsenceUrl = "demandes/historique";
	private static final String sirhDureeCongeAnnuelUrl = "demandes/dureeDemandeCongeAnnuel";
	private static final String sirhDemandeUrl = "demandes/demande";
	private static final String sirhSaveDemandesAgentWithStreamUrl = "demandes/savePieceJointeWithStream";

	/* Filtres */
	private static final String sirhTypeAbsenceKiosqueUrl = "filtres/getTypeAbsenceKiosque";
	private static final String sirhTypeAbsenceForFilterKiosqueUrl = "filtres/getTypeAbsenceForFilterKiosque";
	private static final String sirhEtatAbsenceKiosqueUrl = "filtres/getEtats";
	private static final String sirhGroupeAbsenceUrl = "filtres/getGroupesAbsence";
	private static final String sirhGroupeAbsenceForAgentUrl = "filtres/getGroupesAbsenceForAgent";
	private static final String sirhListOrganisationUrl = "organisation/listOrganisationActif";
	private static final String sirhTypeAbsenceCompteurKiosqueUrl = "filtres/getTypeAbsenceCompteurKiosque";
	private static final String sirhServicesKiosqueUrl = "filtres/services";
	private static final String sirhServicesOperateurKiosqueUrl = "filtres/servicesOperateur";
	private static final String sirhAgentsKiosqueUrl = "filtres/agents";
	private static final String sirhAgentsOperateurKiosqueUrl = "filtres/agentsOperateur";
	private static final String sirhAllTypeAbsenceUrl = "filtres/getAllTypes";

	/* Compteurs */
	private static final String sirhMotifsCompteurKiosqueUrl = "motifCompteur/getListeMotifCompteur";
	private static final String sirhsaveCompteurRecupUrl = "recuperations/addManual";
	private static final String sirhsaveCompteurReposCompUrl = "reposcomps/addManual";

	/* saisies jours repos */
	private static final String sirhGetListAgentsWithJoursFeriesEnGardeUrl = "joursFeriesGarde/getListAgentsWithJoursFeriesEnGarde";
	private static final String sirhSetListAgentsWithJoursFeriesEnGardeUrl = "joursFeriesGarde/setListAgentsWithJoursFeriesEnGarde";

	/* Maladies */
	private static final String sirhListeSiegeLesionUrl = "filtres/getAllTypeSiegeLesion";
	private static final String sirhListeMaladieProUrl = "filtres/getAllTypeMaladiePro";

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	/**
	 * #37756
	 */
	@Override
	public ReturnMessageDto savePJWithInputStream(Integer idAgent, Integer idAgentOperateur, Integer idDemande,
			PieceJointeDto pj) throws IOException {

		String url = String.format(sirhAbsWsBaseUrl + sirhSaveDemandesAgentWithStreamUrl);

		HashMap<String, String> params = new HashMap<>();

		params.put("idAgent", idAgent.toString());
		params.put("idAgentOperateur", idAgentOperateur.toString());
		params.put("idDemande", idDemande.toString());
		params.put("typeFile", pj.getTypeFile());

		ClientResponse res = 
		createAndFirePostRequestWithInputStream(params, url, pj.getFileInputStream(), pj.getTypeFile());
		return readResponse(ReturnMessageDto.class, res, url);
	}
	
	public SoldeDto getAgentSolde(Integer idAgent, FiltreSoldeDto filtreDto) {

		String url = String.format(sirhAbsWsBaseUrl + sirhAgentSoldeUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		String json = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(filtreDto);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponse(SoldeDto.class, res, url);
	}

	@Override
	public List<DemandeDto> getDemandesAgent(Integer idAgent, String onglet, Date fromDate, Date toDate,
			Date dateDemande, String listIdRefEtat, Integer idRefType, Integer idRefGroupeAbsence) {

		String url = String.format(sirhAbsWsBaseUrl + sirhDemandesAgentUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		params.put("ongletDemande", onglet);
		if (fromDate != null)
			params.put("from", sdf.format(fromDate));
		if (toDate != null)
			params.put("to", sdf.format(toDate));
		if (dateDemande != null)
			params.put("dateDemande", sdf.format(dateDemande));
		if (listIdRefEtat != null)
			params.put("etat", listIdRefEtat);
		if (idRefType != null)
			params.put("type", idRefType.toString());
		if (idRefGroupeAbsence != null)
			params.put("groupe", idRefGroupeAbsence.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(DemandeDto.class, res, url);
	}

	@Override
	public List<RefTypeAbsenceDto> getRefTypeAbsenceKiosque(Integer idRefGroupeAbsence, Integer idAgent) {
		String url = String.format(sirhAbsWsBaseUrl + sirhTypeAbsenceKiosqueUrl);
		HashMap<String, String> params = new HashMap<>();
		if (idRefGroupeAbsence != null)
			params.put("idRefGroupeAbsence", idRefGroupeAbsence.toString());
		if (idAgent != null)
			params.put("idAgent", idAgent.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(RefTypeAbsenceDto.class, res, url);
	}

	@Override
	public List<RefTypeAbsenceDto> getRefTypeAbsenceForFilterKiosque(Integer idRefGroupeAbsence, Integer idAgent) {
		String url = String.format(sirhAbsWsBaseUrl + sirhTypeAbsenceForFilterKiosqueUrl);
		HashMap<String, String> params = new HashMap<>();
		if (idRefGroupeAbsence != null)
			params.put("idRefGroupeAbsence", idRefGroupeAbsence.toString());
		if (idAgent != null)
			params.put("idAgent", idAgent.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(RefTypeAbsenceDto.class, res, url);
	}

	@Override
	public List<RefGroupeAbsenceDto> getRefGroupeAbsence() {
		String url = String.format(sirhAbsWsBaseUrl + sirhGroupeAbsenceUrl);
		HashMap<String, String> params = new HashMap<>();

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(RefGroupeAbsenceDto.class, res, url);
	}

	@Override
	public List<RefGroupeAbsenceDto> getRefGroupeAbsenceForAgent() {
		String url = String.format(sirhAbsWsBaseUrl + sirhGroupeAbsenceForAgentUrl);
		HashMap<String, String> params = new HashMap<>();

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(RefGroupeAbsenceDto.class, res, url);
	}

	// #37756 modification de ReturnMessageAbsDto
	@Override
	public ReturnMessageAbsDto saveDemandeAbsence(Integer idAgent, DemandeDto dto) {
		String url = String.format(sirhAbsWsBaseUrl + sirhSaveDemandesAgentUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		String json = new JSONSerializer().exclude("*.class").exclude("*.civilite").exclude("*.signature")
				.exclude("*.position").exclude("*.selectedDroitAbs").exclude("piecesJointes.fileInputStream").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(dto);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponse(ReturnMessageAbsDto.class, res, url);
	}

	@Override
	public List<OrganisationSyndicaleDto> getListOrganisationSyndicale(Integer idAgent, Integer idRefTypeAbsence) {
		String url = String.format(sirhAbsWsBaseUrl + sirhListOrganisationUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		params.put("idRefTypeAbsence", idRefTypeAbsence.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(OrganisationSyndicaleDto.class, res, url);
	}

	@Override
	public List<DemandeDto> getListeATReferenceForAgent(Integer idAgent) {
		String url = String.format(sirhAbsWsBaseUrl + sirhAbsDemandesATForAgent);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(DemandeDto.class, res, url);
	}

	@Override
	public List<RefEtatAbsenceDto> getEtatAbsenceKiosque(String onglet) {
		String url = String.format(sirhAbsWsBaseUrl + sirhEtatAbsenceKiosqueUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("ongletDemande", onglet);

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(RefEtatAbsenceDto.class, res, url);
	}

	@Override
	public ReturnMessageDto deleteDemandeAbsence(Integer idAgent, Integer idDemande) {
		String url = String.format(sirhAbsWsBaseUrl + sirhDeleteDemandesAgentUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		params.put("idDemande", idDemande.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponse(ReturnMessageDto.class, res, url);
	}

	@Override
	public ReturnMessageDto changerEtatDemandeAbsence(Integer idAgent, DemandeEtatChangeDto dto) {
		String url = String.format(sirhAbsWsBaseUrl + sirhChangerEtatDemandesAgentUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		String json = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(dto);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponse(ReturnMessageDto.class, res, url);
	}

	@Override
	public byte[] imprimerDemande(Integer idAgent, Integer idDemande) {
		String url = String.format(sirhAbsWsBaseUrl + sirhPrintDemandesAgentUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		params.put("idDemande", idDemande.toString());
		ClientResponse res = createAndFireRequest(params, url, false, null);

		return readResponseWithFile(res, url);
	}

	@Override
	public AccessRightsAbsDto getDroitsAbsenceAgent(Integer idAgent) {
		String url = String.format(sirhAbsWsBaseUrl + sirhDroitsAgentUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponse(AccessRightsAbsDto.class, res, url);
	}

	@Override
	public List<AgentDto> getAgentsApprobateur(Integer idAgent) {
		String url = String.format(sirhAbsWsBaseUrl + sirhAgentApprobateurUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(AgentDto.class, res, url);
	}

	@Override
	public ReturnMessageDto saveAgentsApprobateur(Integer idAgent, List<AgentDto> listSelect, Integer idAgentConnecte) {
		String url = String.format(sirhAbsWsBaseUrl + sirhAgentApprobateurUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		params.put("idAgentConnecte", idAgentConnecte.toString());

		String json = new JSONSerializer().exclude("*.class").exclude("*.civilite").exclude("*.selectedDroitAbs")
				.transform(new MSDateTransformer(), Date.class).deepSerialize(listSelect);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponse(ReturnMessageDto.class, res, url);
	}

	@Override
	public InputterDto getOperateursDelegataireApprobateur(Integer idAgent) {
		String url = String.format(sirhAbsWsBaseUrl + sirhOperateursDelegataireApprobateurUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponse(InputterDto.class, res, url);
	}

	@Override
	public ViseursDto getViseursApprobateur(Integer idAgent) {
		String url = String.format(sirhAbsWsBaseUrl + sirhViseursApprobateurUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponse(ViseursDto.class, res, url);
	}

	@Override
	public ReturnMessageDto saveOperateursDelegataireApprobateur(Integer idAgent, InputterDto dto,
			Integer idAgentConnecte) {
		String url = String.format(sirhAbsWsBaseUrl + sirhOperateursDelegataireApprobateurUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		params.put("idAgentConnecte", idAgentConnecte.toString());

		String json = new JSONSerializer().exclude("*.class").exclude("*.civilite").exclude("*.selectedDroitAbs")
				.transform(new MSDateTransformer(), Date.class).deepSerialize(dto);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponse(ReturnMessageDto.class, res, url);
	}

	@Override
	public ReturnMessageDto saveViseursApprobateur(Integer idAgent, ViseursDto dto, Integer idAgentConnecte) {
		String url = String.format(sirhAbsWsBaseUrl + sirhViseursApprobateurUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		params.put("idAgentConnecte", idAgentConnecte.toString());

		String json = new JSONSerializer().exclude("*.class").exclude("*.civilite").exclude("*.selectedDroitAbs")
				.transform(new MSDateTransformer(), Date.class).deepSerialize(dto);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponse(ReturnMessageDto.class, res, url);
	}

	@Override
	public List<AgentDto> getAgentsOperateur(Integer idAgentApprobateur, Integer idAgentOperateur) {
		String url = String.format(sirhAbsWsBaseUrl + sirhAgentsOperateurUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgentApprobateur.toString());
		params.put("idOperateur", idAgentOperateur.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(AgentDto.class, res, url);
	}

	@Override
	public ReturnMessageDto saveAgentsOperateur(Integer idAgentApprobateur, Integer idAgentOperateur,
			List<AgentDto> listSelect, Integer idAgentConnecte) {
		String url = String.format(sirhAbsWsBaseUrl + sirhAgentsOperateurUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgentApprobateur.toString());
		params.put("idOperateur", idAgentOperateur.toString());
		params.put("idAgentConnecte", idAgentConnecte.toString());

		String json = new JSONSerializer().exclude("*.class").exclude("*.civilite").exclude("*.selectedDroitAbs")
				.transform(new MSDateTransformer(), Date.class).deepSerialize(listSelect);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponse(ReturnMessageDto.class, res, url);
	}

	@Override
	public List<AgentDto> getAgentsViseur(Integer idAgentApprobateur, Integer idAgentViseur) {
		String url = String.format(sirhAbsWsBaseUrl + sirhAgentsViseurUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgentApprobateur.toString());
		params.put("idViseur", idAgentViseur.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(AgentDto.class, res, url);
	}

	@Override
	public ReturnMessageDto saveAgentsViseur(Integer idAgentApprobateur, Integer idAgentViseur,
			List<AgentDto> listSelect, Integer idAgentConnecte) {
		String url = String.format(sirhAbsWsBaseUrl + sirhAgentsViseurUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgentApprobateur.toString());
		params.put("idViseur", idAgentViseur.toString());
		params.put("idAgentConnecte", idAgentConnecte.toString());

		String json = new JSONSerializer().exclude("*.class").exclude("*.civilite").exclude("*.selectedDroitAbs")
				.transform(new MSDateTransformer(), Date.class).deepSerialize(listSelect);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponse(ReturnMessageDto.class, res, url);
	}

	@Override
	public List<RefTypeAbsenceDto> getRefGroupeAbsenceCompteur() {
		String url = String.format(sirhAbsWsBaseUrl + sirhTypeAbsenceCompteurKiosqueUrl);
		HashMap<String, String> params = new HashMap<>();

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(RefTypeAbsenceDto.class, res, url);
	}

	@Override
	public List<MotifCompteurDto> getListeMotifsCompteur(Integer idRefTypeAbsence) {
		String url = String.format(sirhAbsWsBaseUrl + sirhMotifsCompteurKiosqueUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idRefType", idRefTypeAbsence.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(MotifCompteurDto.class, res, url);
	}

	@Override
	public ReturnMessageDto saveCompteurRecup(Integer idAgent, CompteurDto compteurACreer) {
		String url = String.format(sirhAbsWsBaseUrl + sirhsaveCompteurRecupUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		String json = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(compteurACreer);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponse(ReturnMessageDto.class, res, url);
	}

	@Override
	public ReturnMessageDto saveCompteurReposComp(Integer idAgent, CompteurDto compteurACreer) {
		String url = String.format(sirhAbsWsBaseUrl + sirhsaveCompteurReposCompUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		String json = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(compteurACreer);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponse(ReturnMessageDto.class, res, url);
	}

	@Override
	public List<DemandeDto> getListeDemandes(Integer idAgent, String onglet, Date fromDate, Date toDate,
			Date dateDemande, String listIdRefEtat, Integer idRefType, Integer idRefGroupeAbsence,
			Integer idAgentRecherche, Integer idServiceADS) {

		String url = String.format(sirhAbsWsBaseUrl + sirhListeDemandesUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		params.put("ongletDemande", onglet);
		if (fromDate != null)
			params.put("from", sdf.format(fromDate));
		if (toDate != null)
			params.put("to", sdf.format(toDate));
		if (dateDemande != null)
			params.put("dateDemande", sdf.format(dateDemande));
		if (listIdRefEtat != null)
			params.put("etat", listIdRefEtat);
		if (idRefType != null)
			params.put("type", idRefType.toString());
		if (idRefGroupeAbsence != null)
			params.put("groupe", idRefGroupeAbsence.toString());
		if (idAgentRecherche != null)
			params.put("idAgentRecherche", idAgentRecherche.toString());
		if (idServiceADS != null)
			params.put("idServiceADS", idServiceADS.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(DemandeDto.class, res, url);
	}

	@Override
	public List<DemandeDto> getListeDemandesForPlanning(Date fromDate, Date toDate, String listIdRefEtat,
			Integer idRefType, Integer idRefGroupeAbsence, List<AgentWithServiceDto> listIdsAgent) {

		String url = String.format(sirhAbsWsBaseUrl + sirhListeDemandesPlanningKiosqueUrl);
		HashMap<String, String> params = new HashMap<String, String>();
		if (fromDate != null)
			params.put("from", sdf.format(fromDate));
		if (toDate != null)
			params.put("to", sdf.format(toDate));
		if (listIdRefEtat != null)
			params.put("etat", listIdRefEtat);
		if (idRefType != null)
			params.put("type", idRefType.toString());
		if (idRefGroupeAbsence != null)
			params.put("groupe", idRefGroupeAbsence.toString());

		String csvId = "";
		for (AgentWithServiceDto dto : listIdsAgent) {
			csvId += dto.getIdAgent().toString() + ",";
		}
		if (!"".equals(csvId)) {
			csvId = csvId.substring(0, csvId.length() - 1);
		}
		params.put("idAgents", csvId);

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(DemandeDto.class, res, url);
	}

	@Override
	public List<EntiteDto> getServicesAbsences(Integer idAgent) {
		String url = String.format(sirhAbsWsBaseUrl + sirhServicesKiosqueUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(EntiteDto.class, res, url);
	}

	@Override
	public List<AgentDto> getAgentsAbsences(Integer idAgent, Integer idServiceADS) {
		String url = String.format(sirhAbsWsBaseUrl + sirhAgentsKiosqueUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		if (null != idServiceADS)
			params.put("idServiceADS", idServiceADS.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(AgentDto.class, res, url);
	}

	@Override
	public List<RefTypeAbsenceDto> getAllRefTypeAbsence() {
		String url = String.format(sirhAbsWsBaseUrl + sirhAllTypeAbsenceUrl);
		HashMap<String, String> params = new HashMap<>();

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(RefTypeAbsenceDto.class, res, url);
	}

	@Override
	public List<MotifRefusDto> getListeMotifsRefus() {
		String url = String.format(sirhAbsWsBaseUrl + sirhListeMotifsRefusUrl);
		HashMap<String, String> params = new HashMap<>();

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(MotifRefusDto.class, res, url);
	}

	@Override
	public List<DemandeDto> getHistoriqueAbsence(Integer idAgent, Integer idDemande) {
		String url = String.format(sirhAbsWsBaseUrl + sirhHistoriqueAbsenceUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		params.put("idDemande", idDemande.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(DemandeDto.class, res, url);
	}

	@Override
	public List<HistoriqueSoldeDto> getHistoriqueCompteurAgent(Integer idAgent, Integer idTypeAbsence,
			FiltreSoldeDto dto) {
		String url = String.format(sirhAbsWsBaseUrl + sirhHistoriqueCompteurAgentUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		params.put("codeRefTypeAbsence", idTypeAbsence.toString());
		params.put("isSIRH", Boolean.FALSE.toString());

		String json = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(dto);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponseAsList(HistoriqueSoldeDto.class, res, url);
	}

	@Override
	public DemandeDto getDureeCongeAnnuel(DemandeDto demandeDto) {
		String url = String.format(sirhAbsWsBaseUrl + sirhDureeCongeAnnuelUrl);
		HashMap<String, String> params = new HashMap<>();

		String json = new JSONSerializer().exclude("*.class").exclude("*.civilite").exclude("*.signature")
				.exclude("*.position").exclude("*.selectedDroitAbs").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(demandeDto);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponse(DemandeDto.class, res, url);
	}

	@Override
	public SaisieGardeDto getListAgentsWithJoursFeriesEnGarde(Integer idAgent, Integer idServiceADS, Date dateDebut,
			Date dateFin) {

		String url = String.format(sirhAbsWsBaseUrl + sirhGetListAgentsWithJoursFeriesEnGardeUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		params.put("dateDebut", sdf.format(dateDebut));
		params.put("dateFin", sdf.format(dateFin));

		if (null != idServiceADS)
			params.put("idServiceADS", idServiceADS.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponse(SaisieGardeDto.class, res, url);
	}

	@Override
	public ReturnMessageDto setListAgentsWithJoursFeriesEnGarde(Integer idAgent, Date dateDebut, Date dateFin,
			List<AgentJoursFeriesGardeDto> listDto) {

		String url = String.format(sirhAbsWsBaseUrl + sirhSetListAgentsWithJoursFeriesEnGardeUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		params.put("dateDebut", sdf.format(dateDebut));
		params.put("dateFin", sdf.format(dateFin));

		String json = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(listDto);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponse(ReturnMessageDto.class, res, url);
	}

	@Override
	public ActeursDto getListeActeurs(Integer idAgent) {
		String url = String.format(sirhAbsWsBaseUrl + sirhAbsListeActeursUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponse(ActeursDto.class, res, url);
	}

	@Override
	public DemandeDto getDemande(Integer idAgent, Integer idDemande) {
		String url = String.format(sirhAbsWsBaseUrl + sirhDemandeUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());
		params.put("idDemande", idDemande.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponse(DemandeDto.class, res, url);
	}

	@Override
	public List<EntiteDto> getServicesAbsencesOperateur(Integer idAgent) {
		String url = String.format(sirhAbsWsBaseUrl + sirhServicesOperateurKiosqueUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(EntiteDto.class, res, url);
	}

	@Override
	public List<AgentDto> getAgentsAbsencesOperateur(Integer idAgent, Integer idServiceADS) {
		String url = String.format(sirhAbsWsBaseUrl + sirhAgentsOperateurKiosqueUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idAgent", idAgent.toString());

		if (null != idServiceADS)
			params.put("idServiceADS", idServiceADS.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(AgentDto.class, res, url);
	}

	@Override
	public List<RefTypeDto> getListSiegeLesion() {
		String url = String.format(sirhAbsWsBaseUrl + sirhListeSiegeLesionUrl);
		HashMap<String, String> params = new HashMap<>();

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(RefTypeDto.class, res, url);
	}

	@Override
	public List<RefTypeDto> getListMaladiePro() {
		String url = String.format(sirhAbsWsBaseUrl + sirhListeMaladieProUrl);
		HashMap<String, String> params = new HashMap<>();

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponseAsList(RefTypeDto.class, res, url);
	}

	@Override
	public ReturnMessageDto persistControleMedical(ControleMedicalDto dto) {
		String url = String.format(sirhAbsWsBaseUrl + sirhPersistDemandeControleMedicalUrl);
		HashMap<String, String> params = new HashMap<>();

		String json = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(dto);

		ClientResponse res = createAndFirePostRequest(params, url, json);
		return readResponse(ReturnMessageDto.class, res, url);
	}

	@Override
	public ControleMedicalDto getControleMedicalByDemande(Integer idDemandeMaladie) {
		String url = String.format(sirhAbsWsBaseUrl + sirhGetDemandeControleMedicalUrl);
		HashMap<String, String> params = new HashMap<>();
		params.put("idDemandeMaladie", idDemandeMaladie.toString());

		ClientResponse res = createAndFireGetRequest(params, url);
		return readResponse(ControleMedicalDto.class, res, url);
	}

}
