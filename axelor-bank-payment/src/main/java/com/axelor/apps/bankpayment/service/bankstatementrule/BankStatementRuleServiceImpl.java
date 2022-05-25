package com.axelor.apps.bankpayment.service.bankstatementrule;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementRule;
import com.axelor.apps.bankpayment.db.repo.BankStatementRuleRepository;
import com.axelor.apps.bankpayment.service.bankstatementquery.BankStatementQueryService;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class BankStatementRuleServiceImpl implements BankStatementRuleService {

  protected BankStatementQueryService bankStatementQueryService;

  @Inject
  public BankStatementRuleServiceImpl(BankStatementQueryService bankStatementQueryService) {
    this.bankStatementQueryService = bankStatementQueryService;
  }

  @Override
  public Optional<Partner> getPartner(
      BankStatementRule bankStatementRule, BankReconciliationLine bankReconciliationLine)
      throws AxelorException {
    Objects.requireNonNull(bankStatementRule);

    switch (bankStatementRule.getPartnerFetchMethodSelect()) {
      case BankStatementRuleRepository.PARTNER_FETCH_METHOD_NONE:
        return Optional.empty();
      case BankStatementRuleRepository.PARTNER_FETCH_METHOD_SELECT:
        return Optional.ofNullable(bankStatementRule.getPartner());
      case BankStatementRuleRepository.PARTNER_FETCH_METHOD_QUERY:
        Object result =
            bankStatementQueryService.evalQuery(
                bankStatementRule.getPartnerBankStatementQuery(),
                bankReconciliationLine.getBankStatementLine(),
                null);
        if (result != null) {
          return Optional.of((Partner) result);
        }
        return Optional.empty();
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            "The partner fetch method value does not exist");
    }
  }

  @Override
  public Optional<MoveLine> getMoveLine(
      BankStatementRule bankStatementRule, BankReconciliationLine bankReconciliationLine, Move move)
      throws AxelorException {
    Objects.requireNonNull(bankStatementRule);

    Object result =
        bankStatementQueryService.evalQuery(
            bankStatementRule.getMoveLineBankStatementQuery(),
            bankReconciliationLine.getBankStatementLine(),
            move);
    if (result != null) {
      return Optional.of((MoveLine) result);
    }
    return Optional.empty();
  }
}
