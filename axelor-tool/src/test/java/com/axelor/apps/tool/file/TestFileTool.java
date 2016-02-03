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
package com.axelor.apps.tool.file;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class TestFileTool {

	@Test
	public void create() throws IOException {
		
		String destinationFolder = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "tata/titi/toto";
		String fileName = "toto.txt";
		Assert.assertTrue(FileTool.create(destinationFolder, fileName).createNewFile());
		
	}
	
	@Test
	public void create2() throws IOException {
		
		String fileName = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "tata2/titi2/toto2/toto.txt";
		Assert.assertTrue(FileTool.create(fileName).createNewFile());
		
	}
}
