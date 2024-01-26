package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.bankpayment.db.BankPaymentConfig;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.service.BankReconciliationToolService;
import com.axelor.apps.bankpayment.service.config.BankPaymentConfigService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class BankReconciliationQueryServiceImpl implements BankReconciliationQueryService {

  protected BankPaymentConfigService bankPaymentConfigService;

  @Inject
  public BankReconciliationQueryServiceImpl(BankPaymentConfigService bankPaymentConfigService) {
    this.bankPaymentConfigService = bankPaymentConfigService;
  }

  @Override
  public String getRequestMoveLines() {
    String query =
        "(self.move.statusSelect = :statusDaybook OR self.move.statusSelect = :statusAccounted)"
            + " AND self.move.company = :company"
            + " AND self.move.currency = :bankReconciliationCurrency"
            + " AND self.account.accountType.technicalTypeSelect = :accountType"
            + " AND abs(self.currencyAmount) > 0 AND self.bankReconciledAmount < abs(self.currencyAmount)"
            + " AND (:includeOtherBankStatements IS TRUE OR (self.date BETWEEN :fromDate AND :toDate OR self.dueDate BETWEEN :fromDate AND :toDate))"
            + " AND (:journal IS NULL OR self.move.journal = :journal)"
            + " AND (:cashAccount IS NULL OR self.account = :cashAccount)";

    return query;
  }

  @Override
  public Map<String, Object> getBindRequestMoveLine(BankReconciliation bankReconciliation)
      throws AxelorException {
    Map<String, Object> params = new HashMap<>();
    BankPaymentConfig bankPaymentConfig =
        bankPaymentConfigService.getBankPaymentConfig(bankReconciliation.getCompany());

    params.put("statusDaybook", MoveRepository.STATUS_DAYBOOK);
    params.put("statusAccounted", MoveRepository.STATUS_ACCOUNTED);
    params.put("company", bankReconciliation.getCompany());
    params.put("bankReconciliationCurrency", bankReconciliation.getCurrency());
    params.put("$currencyNumberOfDecimals", bankReconciliation.getCurrency().getNumberOfDecimals());

    if (BankReconciliationToolService.isForeignCurrency(bankReconciliation)) {
      params.put(
          "$companyCurrencyNumberOfDecimals",
          bankReconciliation.getCompany().getCurrency().getNumberOfDecimals());
    }

    params.put("accountType", AccountTypeRepository.TYPE_CASH);

    params.put("includeOtherBankStatements", bankReconciliation.getIncludeOtherBankStatements());

    int dateMargin = bankPaymentConfig.getBnkStmtAutoReconcileDateMargin();
    params.put(
        "fromDate",
        bankReconciliation.getFromDate() != null
            ? bankReconciliation.getFromDate().minusDays(dateMargin)
            : null);
    params.put(
        "toDate",
        bankReconciliation.getToDate() != null
            ? bankReconciliation.getToDate().plusDays(dateMargin)
            : null);

    params.put("journal", bankReconciliation.getJournal());

    params.put("cashAccount", bankReconciliation.getCashAccount());

    return params;
  }
}
