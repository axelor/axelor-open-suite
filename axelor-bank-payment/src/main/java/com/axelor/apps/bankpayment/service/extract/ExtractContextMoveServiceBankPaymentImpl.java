package com.axelor.apps.bankpayment.service.extract;

import com.axelor.apps.account.service.extract.ExtractContextMoveServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;
import java.util.HashMap;

public class ExtractContextMoveServiceBankPaymentImpl extends ExtractContextMoveServiceImpl {

  @Override
  public HashMap<String, Object> getMapFromMoveWizardGenerateReverseForm(Context context)
      throws AxelorException {
    HashMap<String, Object> assistantMap = super.getMapFromMoveWizardGenerateReverseForm(context);
    assistantMap.put(
        "isHiddenMoveLinesInBankReconciliation",
        (Boolean) context.getOrDefault("isHiddenMoveLinesInBankReconciliation", true));
    return assistantMap;
  }
}
