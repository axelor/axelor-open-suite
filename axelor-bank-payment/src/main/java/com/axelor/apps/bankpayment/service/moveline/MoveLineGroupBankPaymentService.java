package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import java.util.Map;

public interface MoveLineGroupBankPaymentService {
  Map<String, Object> getBankReconciledAmountOnChangeValuesMap(MoveLine moveLine)
      throws AxelorException;
}
