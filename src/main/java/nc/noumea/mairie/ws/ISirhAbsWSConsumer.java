package nc.noumea.mairie.ws;

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

import nc.noumea.mairie.kiosque.abs.dto.AccessRightsAbsDto;
import nc.noumea.mairie.kiosque.abs.dto.CompteurDto;
import nc.noumea.mairie.kiosque.abs.dto.DemandeDto;
import nc.noumea.mairie.kiosque.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.kiosque.abs.dto.FiltreSoldeDto;
import nc.noumea.mairie.kiosque.abs.dto.InputterDto;
import nc.noumea.mairie.kiosque.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.kiosque.abs.dto.MotifDto;
import nc.noumea.mairie.kiosque.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.kiosque.abs.dto.RefEtatAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.kiosque.abs.dto.ServiceDto;
import nc.noumea.mairie.kiosque.abs.dto.SoldeDto;
import nc.noumea.mairie.kiosque.abs.dto.ViseursDto;
import nc.noumea.mairie.kiosque.dto.AgentDto;
import nc.noumea.mairie.kiosque.dto.ReturnMessageDto;

public interface ISirhAbsWSConsumer {
	/* COMMUN */

	List<ServiceDto> getServicesAbsences(Integer idAgent);

	List<AgentDto> getAgentsAbsences(Integer idAgent, String codeService);

	/* SOLDE */

	SoldeDto getAgentSolde(Integer idAgent, FiltreSoldeDto filtreDto);

	/* FILTRES */

	List<RefTypeAbsenceDto> getRefTypeAbsenceKiosque(Integer idAgent, Integer idRefGroupeAbsence);

	List<RefTypeAbsenceDto> getAllRefTypeAbsence();

	List<RefEtatAbsenceDto> getEtatAbsenceKiosque(String onglet);

	List<OrganisationSyndicaleDto> getListOrganisationSyndicale();

	List<RefGroupeAbsenceDto> getRefGroupeAbsence();

	/* DEMANDES AGENTS */

	List<DemandeDto> getDemandesAgent(Integer idAgent, String onglet, Date fromDate, Date toDate, Date dateDemande,
			Integer idRefEtat, Integer idRefType, Integer idRefGroupeAbsence);

	ReturnMessageDto saveDemandeAbsence(Integer idAgent, DemandeDto dto);

	ReturnMessageDto deleteDemandeAbsence(Integer idAgent, Integer idDemande);

	ReturnMessageDto changerEtatDemandeAbsence(Integer idAgent, DemandeEtatChangeDto dto);

	byte[] imprimerDemande(Integer idAgent, Integer idDemande);

	/* DROITS */

	AccessRightsAbsDto getDroitsAbsenceAgent(Integer idAgent);

	List<AgentDto> getAgentsApprobateur(Integer idAgent);

	InputterDto getOperateursDelegataireApprobateur(Integer idAgent);

	ViseursDto getViseursApprobateur(Integer idAgent);

	ReturnMessageDto saveAgentsApprobateur(Integer idAgent, List<AgentDto> listSelect);

	ReturnMessageDto saveOperateursDelegataireApprobateur(Integer idAgent, InputterDto dto);

	ReturnMessageDto saveViseursApprobateur(Integer idAgent, ViseursDto dto);

	List<AgentDto> getAgentsOperateursOrViseur(Integer idAgentApprobateur, Integer idAgentOperateurOrViseur);

	ReturnMessageDto saveAgentsOperateursOrViseur(Integer idAgentApprobateur, Integer idAgentOperateurOrViseur,
			List<AgentDto> listSelect);

	/* COMPTEURS */

	List<RefTypeAbsenceDto> getRefGroupeAbsenceCompteur();

	List<MotifCompteurDto> getListeMotifsCompteur(Integer idRefTypeAbsence);

	ReturnMessageDto saveCompteurRecup(Integer idAgent, CompteurDto compteurACreer);

	ReturnMessageDto saveCompteurReposComp(Integer idAgent, CompteurDto compteurACreer);

	/* GESTION DEMANDES */

	List<DemandeDto> getListeDemandes(Integer idAgent, String onglet, Date fromDate, Date toDate, Date dateDemande,
			Integer idRefEtat, Integer idRefType, Integer idAgentRecherche);

	// pour les motifs de refus en auto complétion
	List<MotifDto> getListeMotifsRefus();

}
