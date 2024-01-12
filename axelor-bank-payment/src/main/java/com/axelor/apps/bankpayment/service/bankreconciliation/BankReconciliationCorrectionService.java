package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.base.AxelorException;
import com.axelor.auth.db.User;
import java.time.LocalDateTime;

public interface BankReconciliationCorrectionService {

  boolean getIsCorrectButtonHidden(BankReconciliation bankReconciliation) throws AxelorException;

  String getCorrectedLabel(LocalDateTime correctedDateTime, User correctedUser)
      throws AxelorException;

  void correct(BankReconciliation bankReconciliation, User user);
}
