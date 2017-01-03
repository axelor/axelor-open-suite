/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.ebics.service;

public class EbicsService {
	
	public String makeDN(String name, String email, String country, String organization)
	{
		StringBuffer		buffer;
		
		buffer = new StringBuffer();
		buffer.append("CN=" + name);
		
		if (country != null) {
			buffer.append(", " + "C=" + country.toUpperCase());
		}
		if (organization != null) {
			buffer.append(", " + "O=" + organization);
		}
		if (email != null) {
			buffer.append(", " + "E=" + email);
		}
		
		return buffer.toString();
	}

}
