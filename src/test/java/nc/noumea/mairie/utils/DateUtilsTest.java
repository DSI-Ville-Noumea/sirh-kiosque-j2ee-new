package nc.noumea.mairie.utils;

/*
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

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;

public class DateUtilsTest {

	SimpleDateFormat	sdf	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Test
	public void getCurrentDateMoinsXMois() {
		
		DateTime dateTime = DateTime.now().minusMonths(2);

		DateUtils dateUtils = new DateUtils();
		Date result = dateUtils.getCurrentDateMoinsXMois(2);

		assertEquals(sdf.format(result), sdf.format(dateTime.toDate()));
	}
}
