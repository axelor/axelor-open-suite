package com.axelor.apps.supplychain.service;

import com.axelor.apps.supplychain.db.Mrp;

public class MrpTool {

  public static String computeFullName(Mrp mrp) {
    if (mrp.getMrpSeq() == null || "".equals(mrp.getMrpSeq())) {
      return mrp.getName();
    }
    String fullName = mrp.getMrpSeq();
    if (mrp.getName() != null && !"".equals(mrp.getName())) {
      fullName = fullName + " - " + mrp.getName();
    }
    return fullName;
  }
}
