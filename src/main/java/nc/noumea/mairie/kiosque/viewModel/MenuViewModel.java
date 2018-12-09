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
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Div;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class MenuViewModel extends AbstractViewModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7929474744616954278L;

	private boolean droitsModulePointage;

	@WireVariable
	private EnvironnementService environnementService;

	private boolean afficheRecette;
	
	private boolean absWsOk = true;

	@Init
	public void initMenu() throws ParseException {

		if (environnementService.isRecette()) {
			setAfficheRecette(true);
		} else {
			setAfficheRecette(false);
		}
		
		// Blocage de la saisie des pointages et absences après de la MEP de GTA
		if (!environnementService.saisieAbsAndPtgOpen()) {
			absWsOk = false;
			droitsModulePointage = false;
		} else {
			if(null == getDroitsAbsence()) {
				absWsOk = false;
			}
			/* Pour les pointages */
			setDroitsModulePointage(getDroitsPointage() == null ? false : getDroitsPointage().isApprobation() || getDroitsPointage().isFiches() || getDroitsPointage().isGestionDroitsAcces()
					|| getDroitsPointage().isSaisie() || getDroitsPointage().isVisualisation() || getDroitsPointage().isTitreRepas()|| getDroitsPointage().isTitreRepasAgent() );
		}
	}

	public boolean ouvreGestionTitreRepas() {
		return (null != getDroitsPointage() && (getDroitsPointage().isApprobation() || getDroitsPointage().isVisualisation()));
	}

	@Command
	@GlobalCommand
	public void changeEcran(@BindingParam("page") String page, @BindingParam("ecran") Div div) {
		div.getChildren().clear();
		Map<String, Div> args = new HashMap<String, Div>();
		args.put("div", div);
		Executions.createComponents(page + ".zul", div, args);
	}

	public boolean isDroitsModulePointage() {
		return droitsModulePointage;
	}

	public void setDroitsModulePointage(boolean droitsModulePointage) {
		this.droitsModulePointage = droitsModulePointage;
	}

	public boolean isAfficheRecette() {
		return afficheRecette;
	}

	public void setAfficheRecette(boolean afficheRecette) {
		this.afficheRecette = afficheRecette;
	}

	public EnvironnementService getEnvironnementService() {
		return environnementService;
	}

	public void setEnvironnementService(EnvironnementService environnementService) {
		this.environnementService = environnementService;
	}

	public boolean isAbsWsOk() {
		return absWsOk;
	}

	public void setAbsWsOk(boolean absWsOk) {
		this.absWsOk = absWsOk;
	}
}
