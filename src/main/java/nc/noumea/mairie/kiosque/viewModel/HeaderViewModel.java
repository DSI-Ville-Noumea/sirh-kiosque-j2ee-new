package nc.noumea.mairie.kiosque.viewModel;

/*-
 * #%L
 * sirh-kiosque-j2ee
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2017 Mairie de Nouméa
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

import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import nc.noumea.mairie.kiosque.core.authentification.SecurityUtil;
import nc.noumea.mairie.kiosque.profil.dto.ProfilAgentDto;
import nc.noumea.mairie.ws.IRadiWSConsumer;
import nc.noumea.mairie.ws.ISirhWSConsumer;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class HeaderViewModel implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@WireVariable
	private ISirhWSConsumer	sirhWsConsumer;
	@WireVariable
	private IRadiWSConsumer	radiWSConsumer;
	private ProfilAgentDto			currentUser;
	

	@Init
	public void init() throws Exception {

		Sessions.getCurrent().setAttribute("currentUser", null);
		currentUser = SecurityUtil.getUser(radiWSConsumer, sirhWsConsumer);
		if (currentUser != null) {

			Sessions.getCurrent().setAttribute("currentUser", currentUser);
		}
	}

	public String getPrenomAgent(String prenom) {
		if(prenom==null || prenom.isEmpty()){
			return "";
		}
		String premierLettre = prenom.substring(0, 1).toUpperCase();
		String reste = prenom.substring(1, prenom.length()).toLowerCase();
		return premierLettre + reste;
	}

	public ProfilAgentDto getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(ProfilAgentDto currentUser) {
		this.currentUser = currentUser;
	}
}
