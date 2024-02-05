package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.service.CurrencyScaleServiceBankPayment;
import com.axelor.inject.Beans;
import java.util.Map;

public class BankReconciliationLineManagementRepository extends BankReconciliationLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long bankReconciliationLineId = (Long) json.get("id");
    BankReconciliationLine bankReconciliationLine =
        Beans.get(BankReconciliationLineRepository.class).find(bankReconciliationLineId);

    json.put(
        "$currencyNumberOfDecimals",
        Beans.get(CurrencyScaleServiceBankPayment.class).getScale(bankReconciliationLine));

    return super.populate(json, context);
  }
}
