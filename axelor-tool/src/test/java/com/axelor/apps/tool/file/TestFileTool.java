/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class TestFileTool {

  @Test
  public void create() throws IOException {

    String destinationFolder =
        System.getProperty("java.io.tmpdir")
            + File.separator
            + "tata"
            + File.separator
            + "titi"
            + File.separator
            + "toto";
    String fileName = "toto.txt";

    File file = FileTool.create(destinationFolder, fileName);
    file.deleteOnExit();

    Assert.assertTrue(file.createNewFile());
  }

  @Test
  public void create2() throws IOException {

    String fileName =
        System.getProperty("java.io.tmpdir")
            + File.separator
            + "tata2"
            + File.separator
            + "titi2"
            + File.separator
            + "toto2"
            + File.separator
            + "toto.txt";
    File file = FileTool.create(fileName);
    file.deleteOnExit();

    Assert.assertTrue(file.createNewFile());
  }
}
