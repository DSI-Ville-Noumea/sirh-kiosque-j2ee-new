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



import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import nc.noumea.mairie.kiosque.dto.LightUserDto;
import nc.noumea.mairie.kiosque.profil.dto.ProfilAgentDto;
import nc.noumea.mairie.ws.IRadiWSConsumer;
import nc.noumea.mairie.ws.ISirhWSConsumer;

public class MockAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	private Logger			LOGGER	= LoggerFactory.getLogger(MockAuthenticationProvider.class);

	@Autowired
	private IRadiWSConsumer	radiWSConsumer;

	@Autowired
	private ISirhWSConsumer	sirhWsConsumer;

	private KiosqueAuthoritiesPopulator	kiosqueAuthoritiesPopulator;

	private UserDetails					user;

	public UserDetails getUser() {
		return user;
	}

	public MockAuthenticationProvider(KiosqueAuthoritiesPopulator authoritiesPopulator) {
		kiosqueAuthoritiesPopulator = authoritiesPopulator;
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken)
			throws AuthenticationException {

	}

	@Override
	protected UserDetails retrieveUser(String s, final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {

		
		LightUserDto userDto = radiWSConsumer.getAgentCompteADByLogin(s);
		if (null == userDto) {
			LOGGER.debug("User not exist in Radi WS with username : " + s);
			throw new BadCredentialsException("Connection à l'application Kiosque RH impossible");
		}

		if (0 == userDto.getEmployeeNumber()) {
			LOGGER.debug("User not exist in Radi WS with username : " + s);
			throw new BadCredentialsException("Connection à l'application Kiosque RH impossible");
		}

		ProfilAgentDto profilAgent = null;
		try {
			profilAgent = sirhWsConsumer.getEtatCivil(userDto.getEmployeeNumber());
		} catch (Exception e) {
			// le SIRH-WS ne semble pas repondre
			LOGGER.debug("L'application SIRH-WS ne semble pas répondre.");
			throw new BadCredentialsException("Connection à l'application Kiosque RH impossible");
		}

		if (null == profilAgent || profilAgent.getAgent() == null || profilAgent.getAgent().getIdAgent() == null) {
			LOGGER.debug("ProfilAgent not exist in SIRH WS with EmployeeNumber : " + userDto.getEmployeeNumber());
			throw new BadCredentialsException("Connection à l'application Kiosque RH impossible");
		}

		user = new UserDetails() {
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return kiosqueAuthoritiesPopulator.getGrantedAuthorities(null, usernamePasswordAuthenticationToken.getName());
			}

			@Override
			public String getPassword() {
				return null;
			}

			@Override
			public String getUsername() {
				return usernamePasswordAuthenticationToken.getName();
			}

			@Override
			public boolean isAccountNonExpired() {
				return true;
			}

			@Override
			public boolean isAccountNonLocked() {
				return true;
			}

			@Override
			public boolean isCredentialsNonExpired() {
				return true;
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		};

		return user;
	}
}
