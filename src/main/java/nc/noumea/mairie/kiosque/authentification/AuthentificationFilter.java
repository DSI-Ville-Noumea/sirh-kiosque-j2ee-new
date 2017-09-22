package nc.noumea.mairie.kiosque.authentification;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nc.noumea.mairie.kiosque.dto.LightUserDto;
import nc.noumea.mairie.kiosque.profil.dto.ProfilAgentDto;
import nc.noumea.mairie.ws.IRadiWSConsumer;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

@Component
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AuthentificationFilter implements Filter {

	public static final String ACCES_CONNEXION = "/connexion";
	public static final String ATT_SESSION_USER = "sessionUtilisateur";

	public static final List<String> PAGES_STATIQUES = Arrays.asList("/401.jsp", "/404.jsp", "/incident.jsp",
			"/maintenance.jsp", "/version.jsp");

	private Logger logger = LoggerFactory.getLogger(AuthentificationFilter.class);

	private IRadiWSConsumer radiWSConsumer;

	private ISirhWSConsumer sirhWsConsumer;

	public void init(FilterConfig config) throws ServletException {
		ServletContext servletContext = config.getServletContext();
		WebApplicationContext webApplicationContext = WebApplicationContextUtils
				.getWebApplicationContext(servletContext);

		AutowireCapableBeanFactory autowireCapableBeanFactory = webApplicationContext.getAutowireCapableBeanFactory();

		radiWSConsumer = (IRadiWSConsumer) autowireCapableBeanFactory.getBean("radiWSConsumer");
		sirhWsConsumer = (ISirhWSConsumer) autowireCapableBeanFactory.getBean("sirhWsConsumer");
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
			ServletException {

		/* Cast des objets request et response */
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession hSess = ((HttpServletRequest) request).getSession();

		// on laisse passer pour le rproxy et ainsi permettre de deployer l
		// application sur le 2e noeud tomcat
		if (PAGES_STATIQUES.contains(request.getServletPath())) {
			chain.doFilter(request, response);
			return;
		}

		if (null != hSess.getAttribute("logout")) {
			if (!request.getRequestURI().contains("zkau") && !request.getRequestURI().contains("login.zul")
					&& !request.getRequestURI().contains("css")) {
				
				// dans le cas ou la personne a clique sur ce deconnecte et ne ferme pas le navigateur
				logger.debug("User disconnect");
				hSess.setAttribute("logout", "logout");
				request.getRequestDispatcher("login.zul").forward(request, response);
				return;
			}
			chain.doFilter(request, response);
			return;
		}

		if (null != hSess.getAttribute("currentUser")) {
			chain.doFilter(request, response);
			return;
		}

		if ((null == request.getHeader("x-krb_remote_user") || "".equals(request.getHeader("x-krb_remote_user").trim()))
				&& !request.getHeader("host").contains("localhost")) {
			logger.debug("x-krb_remote_user is NULL");
			// hSess.invalidate();
			// request.logout();
			// response.sendError(HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED,
			// "You are logged out.");
			// #15803
			hSess.setAttribute("logout", "logout");
			request.getRequestDispatcher("login.zul").forward(request, response);
			return;
		}

		String remoteUser = request.getHeader("x-krb_remote_user");

		if (null == remoteUser && request.getHeader("host").contains("localhost")) {
			//remoteUser = "laumi57";
//			remoteUser = "ngusa79"; // drox sabrina 
			//remoteUser = "boust75"; // stephanie
			// remoteUser = "carda69";  // David CARDONA  Approbateur Chef du Service de l’accueil du public et de l’Etat civil (SAPEC)
			 // remoteUser = "panca71";   // operateur DPM
			 //remoteUser = "desno82"; // Direction de l'Espace Public 	Service du poste : 	Pôle Administratif et Budgetaire
			remoteUser = "luile88"; // Section Personnel des Ecoles
//			remoteUser = "habro64"; // administratif et comptable
		}
		
		remoteUser = convertRemoteUser(remoteUser);

		LightUserDto userDto = radiWSConsumer.getAgentCompteADByLogin(remoteUser);
		if (null == userDto) {
			logger.debug("User not exist in Radi WS with RemoteUser : " + remoteUser);
//			request.logout();

			hSess.setAttribute("logout", "logout");
			request.getRequestDispatcher("login.zul").forward(request, response);
			return;
		}

		if (0 == userDto.getEmployeeNumber()) {
			logger.debug("User not exist in Radi WS with RemoteUser : " + remoteUser);
			
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"Vous n'êtes pas un agent de la mairie, vous n'êtes pas autorisé à accéder à cette application.");
			return;
		}

		ProfilAgentDto profilAgent = null;
		try {
			profilAgent = sirhWsConsumer.getEtatCivil(userDto.getEmployeeNumber());
		} catch (Exception e) {
			// le SIRH-WS ne semble pas repondre
			logger.debug("L'application SIRH-WS ne semble pas répondre.");
			
			request.logout();

			hSess.setAttribute("logout", "logout");
			request.getRequestDispatcher("login.zul").forward(request, response);
			return;
		}

		if (null == profilAgent) {
			logger.debug("ProfilAgent not exist in SIRH WS with EmployeeNumber : " + userDto.getEmployeeNumber());
			
			request.logout();

			hSess.setAttribute("logout", "logout");
			request.getRequestDispatcher("login.zul").forward(request, response);
			return;
		}

		hSess.setAttribute("currentUser", profilAgent);
		logger.debug("Authentification du user ok : " + remoteUser);

		chain.doFilter(request, response);
		return;
	}

	public void destroy() {
	}
	
	protected String convertRemoteUser(String remoteUser) {
		if(null != remoteUser
				&& remoteUser.contains("@")) {
			remoteUser = remoteUser.substring(0, remoteUser.indexOf("@"));
		}
		
		return remoteUser;
	}
}
