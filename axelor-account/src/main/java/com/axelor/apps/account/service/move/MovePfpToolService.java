package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;

public interface MovePfpToolService {
  Integer checkOtherInvoiceTerms(Move move);

  void fillMovePfpValidateStatus(Move move);
}
