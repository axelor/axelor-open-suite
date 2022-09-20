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
