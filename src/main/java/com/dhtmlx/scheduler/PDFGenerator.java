package com.dhtmlx.scheduler;

/*
 * #%L
 * sirh-kiosque-j2ee
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Mairie de Nouméa
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
import java.net.URLDecoder;
import javax.servlet.http.*;

import com.dhtmlx.scheduler.PDFWriter;



@SuppressWarnings("serial")
public class PDFGenerator extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {
		String xml;
		xml = req.getParameter("mycoolxmlbody");
		xml = URLDecoder.decode(xml, "UTF-8");
		PDFWriter pdf = new PDFWriter();
		pdf.generate(xml, resp);
	}
}
