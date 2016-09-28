package nc.noumea.mairie.kiosque.travail.viewModel;

/*
 * #%L
 * sirh-kiosque-j2ee
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2016 Mairie de Nouméa
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

import nc.noumea.mairie.kiosque.eae.dto.EaeFinalisationDto;
import nc.noumea.mairie.kiosque.profil.dto.ProfilAgentDto;
import nc.noumea.mairie.ws.AlfrescoCMISService;
import nc.noumea.mairie.ws.ISirhEaeWSConsumer;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EaeViewModel {

	private List<EaeFinalisationDto>	listeUrlEae;

	private ProfilAgentDto				currentUser;

	@WireVariable
	private ISirhEaeWSConsumer			eaeWsConsumer;

	@Init
	public void iniEaeAgent() throws Exception {
		currentUser = (ProfilAgentDto) Sessions.getCurrent().getAttribute("currentUser");
		List<EaeFinalisationDto> res = eaeWsConsumer.getEeaControle(currentUser.getAgent().getIdAgent());

		// on ne garde que les 3 dernieres EAEs
		if (res != null && res.size() > 3) {
			setListeUrlEae(res.subList(0, 3));
		} else {
			setListeUrlEae(res);
		}
	}

	public List<EaeFinalisationDto> getListeUrlEae() {
		return listeUrlEae;
	}

	public void setListeUrlEae(List<EaeFinalisationDto> listeUrlEae) {
		this.listeUrlEae = listeUrlEae;
	}


	public String getUrlFromAlfresco(EaeFinalisationDto dto) {
		if (dto == null || dto.getIdDocument() == null) {
			return "";
		}
		return AlfrescoCMISService.getUrlOfDocument(dto.getIdDocument());
	}
}
