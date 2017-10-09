package nc.noumea.mairie.kiosque.abs.dto;

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
import java.util.ArrayList;
import java.util.List;

public class ResultListDemandeDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3286235189363458429L;

	private List<DemandeDto> listDemandesDto = new ArrayList<DemandeDto>();
	
	/**
	 * Indique si les resultats retournés sont tronqués/limités
	 */
	private boolean resultatsLimites;
	
	private String messageInfoResultatsLimites;
	
	/**
	 * Constructeur
	 */
	public ResultListDemandeDto() {
		
	}

	public List<DemandeDto> getListDemandesDto() {
		return listDemandesDto;
	}

	public void setListDemandesDto(List<DemandeDto> listDemandesDto) {
		this.listDemandesDto = listDemandesDto;
	}

	public boolean isResultatsLimites() {
		return resultatsLimites;
	}

	public void setResultatsLimites(boolean resultatsLimites) {
		this.resultatsLimites = resultatsLimites;
	}

	public String getMessageInfoResultatsLimites() {
		return messageInfoResultatsLimites;
	}

	public void setMessageInfoResultatsLimites(String messageInfoResultatsLimites) {
		this.messageInfoResultatsLimites = messageInfoResultatsLimites;
	}
	
}
