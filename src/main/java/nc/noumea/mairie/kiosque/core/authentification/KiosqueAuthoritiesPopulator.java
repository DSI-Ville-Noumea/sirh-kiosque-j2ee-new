package nc.noumea.mairie.kiosque.core.authentification;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Service;

import nc.noumea.mairie.kiosque.dto.LightUserDto;
import nc.noumea.mairie.kiosque.profil.dto.ProfilAgentDto;
import nc.noumea.mairie.ws.IRadiWSConsumer;
import nc.noumea.mairie.ws.ISirhWSConsumer;

/**
 * Overriden Authorities Populator for Ldap authentication with spring security
 * This class loads roles from the database i/o using LDAP groups
 */
@Service
public class KiosqueAuthoritiesPopulator implements LdapAuthoritiesPopulator {

	private Logger			LOGGER	= LoggerFactory.getLogger(KiosqueAuthoritiesPopulator.class);

	@Autowired
	private IRadiWSConsumer	radiWSConsumer;

	@Autowired
	private ISirhWSConsumer	sirhWsConsumer;

	@Override
	public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations dirContextOperations, String s) {

		List<GrantedAuthority> roles = new ArrayList<>();

		LightUserDto userDto = radiWSConsumer.getAgentCompteADByLogin(s);
		if (null == userDto) {
			LOGGER.debug("User not exist in Radi WS with username : " + s);
			return roles;
		}

		if (0 == userDto.getEmployeeNumber()) {
			LOGGER.debug("User not exist in Radi WS with username : " + s);
			return roles;
		}

		ProfilAgentDto profilAgent = null;
		try {
			profilAgent = sirhWsConsumer.getEtatCivil(userDto.getEmployeeNumber());
		} catch (Exception e) {
			// le SIRH-WS ne semble pas repondre
			LOGGER.debug("L'application SIRH-WS ne semble pas répondre.");
			return roles;
		}

		if (null == profilAgent || profilAgent.getAgent() == null || profilAgent.getAgent().getIdAgent() == null) {
			LOGGER.debug("ProfilAgent not exist in SIRH WS with EmployeeNumber : " + userDto.getEmployeeNumber());
			return roles;
		}

		// si toutes les conditions sont remplies alors on donne le role USER
		roles.add(new SimpleGrantedAuthority("USER"));

		return roles;
	}

}
