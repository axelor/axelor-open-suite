/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.supplychain.db.Mrp;
import org.junit.Assert;
import org.junit.Test;

public class TestMrpTool {

  @Test
  public void testComputeFullNameEveryField() {
    Mrp mrp = createMrp("Name", "MRP0002");
    Assert.assertEquals("MRP0002 - Name", MrpTool.computeFullName(mrp));
  }

  @Test
  public void testComputeFullNameNameNull() {
    Mrp mrp = createMrp(null, "MRP0002");
    Assert.assertEquals("MRP0002", MrpTool.computeFullName(mrp));
  }

  @Test
  public void testComputeFullNameNameEmpty() {
    Mrp mrp = createMrp("", "MRP0002");
    Assert.assertEquals("MRP0002", MrpTool.computeFullName(mrp));
  }

  @Test
  public void testComputeFullNameMrpSeqNull() {
    Mrp mrp = createMrp("Name", null);
    Assert.assertEquals("Name", MrpTool.computeFullName(mrp));
  }

  @Test
  public void testComputeFullNameMrpSeqEmpty() {
    Mrp mrp = createMrp("Name", "");
    Assert.assertEquals("Name", MrpTool.computeFullName(mrp));
  }

  @Test
  public void testComputeFullNameEverythingNull() {
    Mrp mrp = createMrp(null, null);
    Assert.assertNull(MrpTool.computeFullName(mrp));
  }

  @Test
  public void testComputeFullNameEverythingEmpty() {
    Mrp mrp = createMrp("", "");
    Assert.assertEquals("", MrpTool.computeFullName(mrp));
  }

  protected Mrp createMrp(String name, String mrpSeq) {
    Mrp mrp = new Mrp();
    mrp.setName(name);
    mrp.setMrpSeq(mrpSeq);
    return mrp;
  }
}
