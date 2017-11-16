/* SecurityUtil.java

{{IS_NOTE
	Purpose:

	Description:

	History:
		Sep 17, 2009 10:48:23 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.
Partial Copyright 2004, 2005, 2006 Acegi Technology Pty Limited

{{IS_RIGHT
	This program is distributed under GPL Version 3.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.

	This implementation is based on codes of the Spring Security taglibs
	written by Ben Alex, Thomas Champagne, Francois Beausoleil, et al.

	As what Apache License required, we have to include following license
	statement.

	-----

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

}}IS_RIGHT
 */
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import nc.noumea.mairie.kiosque.dto.LightUserDto;
import nc.noumea.mairie.kiosque.profil.dto.ProfilAgentDto;
import nc.noumea.mairie.ws.IRadiWSConsumer;
import nc.noumea.mairie.ws.ISirhWSConsumer;

/**
 * Utility class for ZK spring security.
 *
 * @author henrichen
 */
@SuppressWarnings("rawtypes")
public class SecurityUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtil.class);

	/**
	 * Return the current Authentication object.
	 * 
	 * @throws Exception
	 */
	public static ProfilAgentDto getUser(IRadiWSConsumer radiWSConsumer, ISirhWSConsumer sirhWsConsumer) throws Exception {
		final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
				try {
					Object p = auth.getPrincipal();

					if (p instanceof UserDetails) {
						LightUserDto userDto = radiWSConsumer.getAgentCompteADByLogin(((UserDetails) p).getUsername());
						ProfilAgentDto profilAgent = sirhWsConsumer.getEtatCivil(userDto.getEmployeeNumber());
						return profilAgent;
					}

				} catch (RuntimeException e) {
					LOGGER.error(e.getMessage(), e);
					throw e;
				}
		}
		return null;
	}

	/**
	 * Return true if the authenticated principal is granted NONE of the roles
	 * specified in authorities.
	 *
	 * @param authorities
	 *            A comma separated list of roles which the user must have been
	 *            granted NONE.
	 * @return true if the authenticated principal is granted authorities of
	 *         NONE the specified roles.
	 */
	public static boolean isNoneGranted(String authorities) {
		if (null == authorities || "".equals(authorities)) {
			return false;
		}
		final Collection<? extends GrantedAuthority> granted = getPrincipalAuthorities();

		final Set grantedCopy = retainAll(granted, parseAuthorities(authorities));
		return grantedCopy.isEmpty();
	}

	/**
	 * Return true if the authenticated principal is granted ALL of the roles
	 * specified in authorities.
	 *
	 * @param authorities
	 *            A comma separated list of roles which the user must have been
	 *            granted ALL.
	 * @return true true if the authenticated principal is granted authorities
	 *         of ALL the specified roles.
	 */
	public static boolean isAllGranted(String authorities) {
		if (null == authorities || "".equals(authorities)) {
			return false;
		}

		final Collection<? extends GrantedAuthority> granted = getPrincipalAuthorities();
		return granted.containsAll(parseAuthorities(authorities));
	}

	/**
	 * Return true if the authenticated principal is granted ANY of the roles
	 * specified in authorities.
	 *
	 * @param authorities
	 *            A comma separated list of roles which the user must have been
	 *            granted ANY.
	 * @return true true if the authenticated principal is granted authorities
	 *         of ALL the specified roles.
	 */
	public static boolean isAnyGranted(String authorities) {
		if (null == authorities || "".equals(authorities)) {
			return false;
		}
		final Collection<? extends GrantedAuthority> granted = getPrincipalAuthorities();
		final Set grantedCopy = retainAll(granted, parseAuthorities(authorities));
		return !grantedCopy.isEmpty();
	}

	private static Collection<? extends GrantedAuthority> getPrincipalAuthorities() {
		Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
		if (null == currentUser) {
			return Collections.emptyList();
		}
		if ((null == currentUser.getAuthorities()) || (currentUser.getAuthorities().isEmpty())) {
			return Collections.emptyList();
		}
		return currentUser.getAuthorities();
	}

	private static Collection<GrantedAuthority> parseAuthorities(String authorizationsString) {
		final ArrayList<GrantedAuthority> required = new ArrayList<>();
		final String[] roles = authorizationsString.split(",");

		for (int i = 0; i < roles.length; i++) {
			String role = roles[i].trim();
			required.add(new SimpleGrantedAuthority(role));
		}
		return required;
	}

	/**
	 * Find the common authorities between the current authentication's
	 * {@link GrantedAuthority} and the ones that have been specified in the
	 * tag's ifAny, ifNot or ifAllGranted attributes.
	 * <p>
	 * We need to manually iterate over both collections, because the granted
	 * authorities might not implement {@link Object#equals(Object)} and
	 * {@link Object#hashCode()} in the same way as
	 * {@link SimpleGrantedAuthority}, thereby invalidating
	 * {@link Collection#retainAll(java.util.Collection)} results.
	 * </p>
	 * <p>
	 * <strong>CAVEAT</strong>: This method <strong>will not</strong> work if
	 * the granted authorities returns a <code>null</code> string as the return
	 * value of
	 * {@link org.springframework.security.GrantedAuthority.getAuthority()}.
	 * </p>
	 * <p>
	 * Reported by rawdave, on Fri Feb 04, 2005 2:11 pm in the Spring Security
	 * forum.
	 * </p>
	 *
	 * @param granted
	 *            The authorities granted by the authentication. May be any
	 *            implementation of {@link GrantedAuthority} that does
	 *            <strong>not</strong> return <code>null</code> from
	 *            {@link org.springframework.security.GrantedAuthority.getAuthority()}.
	 * @param required
	 *            A {@link Set} of {@link SimpleGrantedAuthority}s that have
	 *            been built using ifAny, ifAll or ifNotGranted.
	 * @return A set containing only the common authorities between
	 *         <var>granted</var> and <var>required</var>.
	 */
	private static Set retainAll(final Collection<? extends GrantedAuthority> granted, final Collection<? extends GrantedAuthority> required) {
		Set<String> grantedRoles = toRoles(granted);
		Set<String> requiredRoles = toRoles(required);
		grantedRoles.retainAll(requiredRoles);

		return toAuthorities(grantedRoles, granted);
	}

	/**
	 * @param authorities
	 * @return
	 */
	private static Set<String> toRoles(Collection<? extends GrantedAuthority> authorities) {
		final Set<String> target = new HashSet<>();
		for (GrantedAuthority au : authorities) {

			if (null == au.getAuthority()) {
				throw new IllegalArgumentException(
						"Cannot process GrantedAuthority objects which return null from getAuthority() - attempting to process " + au.toString());
			}

			target.add(au.getAuthority());
		}

		return target;
	}

	/**
	 * @param grantedRoles
	 * @param granted
	 * @return
	 */
	private static Set<GrantedAuthority> toAuthorities(Set<String> grantedRoles, Collection<? extends GrantedAuthority> granted) {
		Set<GrantedAuthority> target = new HashSet<>();

		for (String role : grantedRoles) {
			for (GrantedAuthority authority : granted) {

				if (authority.getAuthority().equals(role)) {
					target.add(authority);
					break;
				}
			}
		}
		return target;
	}

	/**
	 * test if current user principal contains all given authorities
	 *
	 * @param authorities
	 *            the roles will be checked
	 */
	public static void assertAll(String... authorities) {

		if (null == authorities || authorities.length == 0) {
			return;
		}

		final ArrayList<GrantedAuthority> required = new ArrayList<>();
		for (String auth : authorities) {
			required.add(new SimpleGrantedAuthority(auth));
		}

		final Collection<? extends GrantedAuthority> granted = getPrincipalAuthorities();
		if (!granted.containsAll(required)) {
			Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
			throw new AccessDeniedException(
					"The current principal doesn't has enough authority. Authentication: " + (currentUser == null ? "" : currentUser.getName()));
		}
	}

}
