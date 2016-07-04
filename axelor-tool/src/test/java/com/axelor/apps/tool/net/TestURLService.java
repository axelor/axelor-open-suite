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
package com.axelor.apps.tool.net;

import org.junit.Assert;
import org.junit.Test;


public class TestURLService {
	
	@Test
	public void testNotExist() {
		
		Assert.assertNull(URLService.notExist("http://www.google.com"));
		Assert.assertEquals("Problème de format de l'URL", URLService.notExist("www.google.com"));
		Assert.assertEquals("Ce document n'existe pas", URLService.notExist("http://www.testtrgfgfdg.com/"));
	}
	
}
