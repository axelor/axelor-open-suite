package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.service.CurrencyScaleServiceBankPayment;
import com.axelor.inject.Beans;
import java.util.Map;

public class BankStatementLineManagementRepository extends BankStatementLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long bankStatementLineId = (Long) json.get("id");
    BankStatementLine bankStatementLine =
        Beans.get(BankStatementLineRepository.class).find(bankStatementLineId);

    json.put(
        "$currencyNumberOfDecimals",
        Beans.get(CurrencyScaleServiceBankPayment.class).getScale(bankStatementLine));

    return super.populate(json, context);
  }
}
