<%--
  #%L
  sirh-kiosque-j2ee
  $Id:$
  $HeadURL:$
  %%
  Copyright (C) 2014 Mairie de Nouméa
  %%
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/gpl-3.0.html>.
  #L%
  --%>
<%@page contentType="text/plain"%>
<%@page import="java.net.InetAddress"%>
sirh.kiosque.j2ee.version=${version}<br/>
sirh.kiosque.j2ee.hostaddress=<%=InetAddress.getLocalHost().getHostAddress() %><br/>
sirh.kiosque.j2ee.canonicalhostname=<%=InetAddress.getLocalHost().getCanonicalHostName() %><br/>
sirh.kiosque.j2ee.hostname=<%=InetAddress.getLocalHost().getHostName() %><br/>
sirh.kiosque.j2ee.tomcat.version=<%= application.getServerInfo() %><br/>
sirh.kiosque.j2ee.tomcat.catalina_base=<%= System.getProperty("catalina.base") %><br/>
<% 
HttpSession theSession = request.getSession( false );

// print out the session id
if( theSession != null ) {
  //pw.println( "<BR>Session Id: " + theSession.getId() );
  synchronized( theSession ) {
    // invalidating a session destroys it
    theSession.invalidate();
    //pw.println( "<BR>Session destroyed" );
  }
}
%>
