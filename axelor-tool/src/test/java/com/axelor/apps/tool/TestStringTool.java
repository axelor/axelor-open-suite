/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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

import com.axelor.exception.db.TraceBack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

  @Test
  public void testGetIdListString() {

    List<Long> traceBackIds = Arrays.asList(null, 1l, 2l, null, 5l, null);
    List<TraceBack> traceBackList = new ArrayList<>();
    for (Long id : traceBackIds) {
      TraceBack traceBack = new TraceBack();
      traceBack.setId(id);
      traceBackList.add(traceBack);
    }

    String expected = "1,2,5";
    Assert.assertEquals(expected, StringTool.getIdListString(traceBackList));

    traceBackList = null;
    Assert.assertEquals("0", StringTool.getIdListString(traceBackList));

    traceBackList = new ArrayList<>();
    Assert.assertEquals("0", StringTool.getIdListString(traceBackList));
  }
}
