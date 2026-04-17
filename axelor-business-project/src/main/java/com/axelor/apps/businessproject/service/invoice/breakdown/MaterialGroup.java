package com.axelor.apps.businessproject.service.invoice.breakdown;

import com.axelor.apps.account.db.InvoiceLine;
import javax.annotation.Nullable;

/**
 * Pairs an expense originated invoice line (material) with its optional charged fee line.
 *
 * <p>In the breakdown, an expense originated item and its fee are to be displayed together, as the
 * fee line sits immediately below its material line.
 */
public class MaterialGroup {

  private final InvoiceLine materialLine;
  private final InvoiceLine feeLine;

  public MaterialGroup(InvoiceLine materialLine, InvoiceLine feeLine) {
    this.materialLine = materialLine;
    this.feeLine = feeLine;
  }

  public MaterialGroup(InvoiceLine materialLine) {
    this.materialLine = materialLine;
    this.feeLine = null;
  }

  public InvoiceLine getMaterialLine() {
    return materialLine;
  }

  /** Charged fee for this material Null if no fee was applied */
  @Nullable
  public InvoiceLine getFeeLine() {
    return feeLine;
  }

  public boolean hasFee() {
    return feeLine != null;
  }
}
