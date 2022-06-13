package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.MoveLine;
import java.math.BigDecimal;

public interface MoveLineInvoiceTermService {
  public void generateDefaultInvoiceTerm(MoveLine moveLine);

  void computeInvoiceTerms(MoveLine moveLine, BigDecimal oldTotal);
}
