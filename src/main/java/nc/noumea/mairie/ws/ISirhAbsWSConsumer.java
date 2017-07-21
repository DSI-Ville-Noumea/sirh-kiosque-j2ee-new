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

import java.util.Date;
import java.util.List;

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

public interface ISirhAbsWSConsumer {
	/* COMMUN */

	List<EntiteDto> getServicesAbsences(Integer idAgent);

	List<AgentDto> getAgentsAbsences(Integer idAgent, Integer idServiceADS);

	DemandeDto getDureeCongeAnnuel(DemandeDto demandeDto);

	/* SOLDE */

	SoldeDto getAgentSolde(Integer idAgent, FiltreSoldeDto filtreDto);

	List<HistoriqueSoldeDto> getHistoriqueCompteurAgent(Integer idAgent, Integer idTypeAbsence, FiltreSoldeDto dto);

	/* FILTRES */

	List<RefTypeAbsenceDto> getRefTypeAbsenceKiosque(Integer idRefGroupeAbsence, Integer idAgent);

	List<RefTypeAbsenceDto> getRefTypeAbsenceForFilterKiosque(Integer idRefGroupeAbsence, Integer idAgent);

	List<RefTypeAbsenceDto> getAllRefTypeAbsence();

	List<RefEtatAbsenceDto> getEtatAbsenceKiosque(String onglet);

	List<OrganisationSyndicaleDto> getListOrganisationSyndicale(Integer idAgent, Integer idRefTypeAbsence);

	List<RefGroupeAbsenceDto> getRefGroupeAbsence();

	/* DEMANDES AGENTS */

	List<DemandeDto> getDemandesAgent(Integer idAgent, String onglet, Date fromDate, Date toDate, Date dateDemande,
			String listIdRefEtat, Integer idRefType, Integer idRefGroupeAbsence);

	ReturnMessageAbsDto saveDemandeAbsence(Integer idAgent, DemandeDto dto);

	ReturnMessageDto deleteDemandeAbsence(Integer idAgent, Integer idDemande);

	ReturnMessageDto changerEtatDemandeAbsence(Integer idAgent, DemandeEtatChangeDto dto);

	ReturnMessageDto persistControleMedical(ControleMedicalDto dto);

	ControleMedicalDto getControleMedicalByDemande(Integer demandeMaladieId);

	byte[] imprimerDemande(Integer idAgent, Integer idDemande);

	ReturnMessageDto savePJWithInputStream(Integer idAgent, Integer idAgentOperateur, Integer idDemande,
			PieceJointeDto pj) throws IOException;
  
	List<DemandeDto> getListeATReferenceForAgent(Integer idAgent);

	/* DROITS */

	AccessRightsAbsDto getDroitsAbsenceAgent(Integer idAgent);

	List<AgentDto> getAgentsApprobateur(Integer idAgent);

	InputterDto getOperateursDelegataireApprobateur(Integer idAgent);

	ViseursDto getViseursApprobateur(Integer idAgent);

	ReturnMessageDto saveAgentsApprobateur(Integer idAgent, List<AgentDto> listSelect,Integer idAgentConnecte);

	ReturnMessageDto saveOperateursDelegataireApprobateur(Integer idAgent, InputterDto dto,Integer idAgentConnecte);

	ReturnMessageDto saveViseursApprobateur(Integer idAgent, ViseursDto dto,Integer idAgentConnecte);

	List<AgentDto> getAgentsOperateur(Integer idAgentApprobateur, Integer idAgentOperateur);

	ReturnMessageDto saveAgentsOperateur(Integer idAgentApprobateur, Integer idAgentOperateur, List<AgentDto> listSelect,Integer idAgentConnecte);

	List<AgentDto> getAgentsViseur(Integer idAgentApprobateur, Integer idAgentViseur);

	ReturnMessageDto saveAgentsViseur(Integer idAgentApprobateur, Integer idAgentViseur, List<AgentDto> listSelect,Integer idAgentConnecte);

	/* COMPTEURS */

	List<RefTypeAbsenceDto> getRefGroupeAbsenceCompteur();

	List<MotifCompteurDto> getListeMotifsCompteur(Integer idRefTypeAbsence);

	ReturnMessageDto saveCompteurRecup(Integer idAgent, CompteurDto compteurACreer);

	ReturnMessageDto saveCompteurReposComp(Integer idAgent, CompteurDto compteurACreer);

	List<EntiteDto> getServicesAbsencesOperateur(Integer idAgent);

	List<AgentDto> getAgentsAbsencesOperateur(Integer idAgent, Integer idServiceADS);

	/* GESTION DEMANDES */

	List<DemandeDto> getListeDemandes(Integer idAgent, String onglet, Date fromDate, Date toDate, Date dateDemande,
			String listIdRefEtat, Integer idRefType, Integer idRefGroupeAbsence, Integer idAgentRecherche,
			Integer idServiceADS);

	List<DemandeDto> getHistoriqueAbsence(Integer idAgent, Integer idDemande);

	// pour les motifs de refus en auto complétion
	List<MotifRefusDto> getListeMotifsRefus();

	/* SAISIES JOURS DE GARDE */
	SaisieGardeDto getListAgentsWithJoursFeriesEnGarde(Integer idAgent, Integer idServiceADS, Date dateDebut, Date dateFin);

	ReturnMessageDto setListAgentsWithJoursFeriesEnGarde(Integer idAgent, Date dateDebut, Date dateFin,
			List<AgentJoursFeriesGardeDto> listDto);

	ActeursDto getListeActeurs(Integer idAgent);

	DemandeDto getDemande(Integer idAgent, Integer idDemande);

	List<DemandeDto> getListeDemandesForPlanning(Date fromDate, Date toDate, String listIdRefEtat, Integer idRefType,
			Integer idRefGroupeAbsence, List<AgentWithServiceDto> listIdsAgent);

	List<RefTypeDto> getListMaladiePro();

	List<RefTypeDto> getListSiegeLesion();

	List<RefGroupeAbsenceDto> getRefGroupeAbsenceForAgent();

}
