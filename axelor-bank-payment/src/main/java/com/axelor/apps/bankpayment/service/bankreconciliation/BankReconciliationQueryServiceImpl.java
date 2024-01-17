package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.bankpayment.db.BankPaymentConfig;
import com.axelor.apps.bankpayment.db.BankReconciliation;
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
  public String getRequestMoveLines(BankReconciliation bankReconciliation) {
    String query =
        "(self.move.statusSelect = :statusDaybook OR self.move.statusSelect = :statusAccounted)"
            + " AND self.move.company = :company"
            + " AND self.move.currency = :bankReconciliationCurrency"
            + " AND self.account.accountType.technicalTypeSelect = :accountType"
            + " AND abs(self.currencyAmount) > 0 AND self.bankReconciledAmount < abs(self.currencyAmount)";

    if (!bankReconciliation.getIncludeOtherBankStatements()) {
      query =
          query
              + " AND (self.date >= :fromDate OR self.dueDate >= :fromDate)"
              + " AND (self.date <= :toDate OR self.dueDate <= :toDate)";
    }

    if (bankReconciliation.getJournal() != null) {
      query = query + " AND self.move.journal = :journal";
    }
    if (bankReconciliation.getCashAccount() != null) {
      query = query + " AND self.account = :cashAccount";
    }

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
    params.put("accountType", AccountTypeRepository.TYPE_CASH);
    if (!bankReconciliation.getIncludeOtherBankStatements()) {
      int dateMargin = bankPaymentConfig.getBnkStmtAutoReconcileDateMargin();
      params.put("fromDate", bankReconciliation.getFromDate().minusDays(dateMargin));
      params.put("toDate", bankReconciliation.getToDate().plusDays(dateMargin));
    }
    if (bankReconciliation.getJournal() != null) {
      params.put("journal", bankReconciliation.getJournal());
    }
    if (bankReconciliation.getCashAccount() != null) {
      params.put("cashAccount", bankReconciliation.getCashAccount());
    }

    return params;
  }
}
