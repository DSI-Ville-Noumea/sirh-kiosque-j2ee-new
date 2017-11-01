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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import nc.noumea.mairie.kiosque.dto.LightUserDto;
import nc.noumea.mairie.kiosque.profil.dto.ProfilAgentDto;
import nc.noumea.mairie.ws.IRadiWSConsumer;
import nc.noumea.mairie.ws.ISirhWSConsumer;

/**
 * Implémentation de AuthenticationProvider pour l'authentification à
 * l'application
 * 
 */
public class KiosqueAuthenticationProvider implements AuthenticationProvider {

	private static final Logger		LOGGER				= LoggerFactory.getLogger(KiosqueAuthenticationProvider.class);

	/** Message par défaut */
	private static final String		DEFAULT_MESSAGE		= "Connection à l'application Kiosque RH impossible";

	/** Authentication provider */
	private AuthenticationProvider	provider;

	/** Message d'erreur si échec d'authentification par le provider */
	private String					messageProvider		= DEFAULT_MESSAGE;

	/** Message d'erreur si échec d'authentification à l'application */
	private String					messageKiosqueConf	= DEFAULT_MESSAGE;

	// @formatter:off
	@Autowired
	private IRadiWSConsumer			radiWSConsumer;

	@Autowired
	private ISirhWSConsumer			sirhWsConsumer;
	// @formatter:on

	/**
	 * Override la méthode authenticate
	 * 
	 * @param authentication
	 *            Authentication
	 * @throws AuthenticationException
	 *             Exception d'authentification
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		Authentication authenticationResult = null;

		if (provider != null)
			try {
				authenticationResult = provider.authenticate(authentication);
			} catch (BadCredentialsException e) {
				LOGGER.error("Error lors de l'authentifation", e);
				throw new BadCredentialsException(messageProvider);
			}

		String username = authentication.getName();
		String password = (String) authentication.getCredentials();
		List<GrantedAuthority> roles = new ArrayList<>();

		LightUserDto userDto = radiWSConsumer.getAgentCompteADByLogin(username);
		if (null == userDto) {
			LOGGER.debug("User not exist in Radi WS with RemoteUser : " + username);
			throw new BadCredentialsException(messageProvider);
		}

		if (0 == userDto.getEmployeeNumber()) {
			LOGGER.debug("User not exist in Radi WS with RemoteUser : " + username);
			throw new BadCredentialsException(messageProvider);
		}

		ProfilAgentDto profilAgent = null;
		try {
			profilAgent = sirhWsConsumer.getEtatCivil(userDto.getEmployeeNumber());
		} catch (Exception e) {
			// le SIRH-WS ne semble pas repondre
			LOGGER.debug("L'application SIRH-WS ne semble pas répondre.");
			throw new BadCredentialsException(messageProvider);
		}

		if (null == profilAgent || profilAgent.getAgent() == null || profilAgent.getAgent().getIdAgent() == null) {
			LOGGER.debug("ProfilAgent not exist in SIRH WS with EmployeeNumber : " + userDto.getEmployeeNumber());
			throw new BadCredentialsException(messageProvider);
		}

		return (provider == null) ? new UsernamePasswordAuthenticationToken(username, password, roles) : authenticationResult;
	}

	/**
	 * Override de la méthode supports
	 * 
	 * @param authentication
	 *            Authentication
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return (provider == null) ? true : provider.supports(authentication);
	}

	/**
	 * get Provider
	 * 
	 * @return provider
	 */
	public AuthenticationProvider getProvider() {
		return provider;
	}

	/**
	 * set Provider
	 * 
	 * @param provider
	 *            Provider to set
	 */
	public void setProvider(AuthenticationProvider provider) {
		this.provider = provider;
	}

	/**
	 * get MessageProvider
	 * 
	 * @return message
	 */
	public String getMessageProvider() {
		return messageProvider;
	}

	/**
	 * set MessageProvider
	 * 
	 * @param messageProvider
	 *            message to set
	 */
	public void setMessageProvider(String messageProvider) {
		this.messageProvider = messageProvider;
	}

	public String getMessageKiosqueConf() {
		return messageKiosqueConf;
	}

	public void setMessageKiosqueConf(String messageKiosqueConf) {
		this.messageKiosqueConf = messageKiosqueConf;
	}

}
