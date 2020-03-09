/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool;

import org.junit.Assert;
import org.junit.Test;

public class TestStringTool {

  @Test
  public void testToFirstLower() {

    String actual = "Test";
    String result = "test";

    Assert.assertEquals(StringTool.toFirstLower(actual), result);
  }

  @Test
  public void testToFirstUpper() {

    String actual = "test";
    String result = "Test";

    Assert.assertEquals(StringTool.toFirstUpper(actual), result);
  }

  @Test
  public void testFillString() {

    String actual = "test";
    String resultRight = "test    ";
    String resultLeft = "    test";

    Assert.assertEquals(StringTool.fillStringRight(actual, ' ', 8), resultRight);
    Assert.assertEquals(StringTool.fillStringRight(actual, ' ', 2), "te");

    Assert.assertEquals(StringTool.fillStringLeft(actual, ' ', 8), resultLeft);
    Assert.assertEquals(StringTool.fillStringLeft(actual, ' ', 2), "st");

    Assert.assertEquals(StringTool.fillStringLeft(resultRight, ' ', 4), "    ");
  }
}
