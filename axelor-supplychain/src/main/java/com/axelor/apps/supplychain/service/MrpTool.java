package com.axelor.apps.supplychain.service;

import com.axelor.apps.supplychain.db.Mrp;

/** Utility class for computing MRP fields. */
public class MrpTool {

  private MrpTool() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Compute full name of a given MRP using {@link Mrp#mrpSeq} and {@link Mrp#name}. This method
   * manages the case where these fields are null or empty.
   *
   * @param mrp a MRP
   * @return the computed full name
   * @throws NullPointerException if the given MRP is null
   */
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
