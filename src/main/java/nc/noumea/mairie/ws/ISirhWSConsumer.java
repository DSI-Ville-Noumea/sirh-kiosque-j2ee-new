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


import java.util.List;

import nc.noumea.mairie.kiosque.dto.AgentDto;
import nc.noumea.mairie.kiosque.dto.AgentWithServiceDto;
import nc.noumea.mairie.kiosque.dto.ReferentRhDto;
import nc.noumea.mairie.kiosque.profil.dto.ProfilAgentDto;
import nc.noumea.mairie.kiosque.travail.dto.EstChefDto;
import nc.noumea.mairie.kiosque.travail.dto.FichePosteDto;
import nc.noumea.mairie.kiosque.travail.dto.ServiceTreeDto;

public interface ISirhWSConsumer {

	ProfilAgentDto getEtatCivil(Integer idAgent);

	FichePosteDto getFichePoste(Integer idAgent);

	AgentWithServiceDto getSuperieurHierarchique(Integer idAgent);

	List<ServiceTreeDto> getArbreServiceAgent(Integer idAgent);

	List<AgentWithServiceDto> getAgentEquipe(Integer idAgent, String codeService);

	byte[] imprimerFDP(Integer idFichePoste);

	EstChefDto isAgentChef(Integer idAgent);

	List<AgentWithServiceDto> getListeAgentsMairie();

	boolean estHabiliteEAE(Integer idAgent);

	List<ReferentRhDto> getListeReferentRH();

	AgentWithServiceDto getAgent(Integer idAgentReferent);

	List<AgentDto> getAgentsSubordonnes(Integer idAgent);

}
