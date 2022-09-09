package com.axelor.apps.bankpayment.service.bankstatementrule;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementRule;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import java.util.Optional;

public interface BankStatementRuleService {

  /**
   * Method to get the partner from bankstatementrule. bankReconciliationLine.bankStatementLine is
   * used for the context of the formula.
   *
   * @param bankStatementRule: can not be null
   * @param bankReconciliationLine: can not be null
   * @return Optional of partner : {@link Partner}
   * @throws AxelorException if the formula can not be eval to a partner
   */
  Optional<Partner> getPartner(
      BankStatementRule bankStatementRule, BankReconciliationLine bankReconciliationLine)
      throws AxelorException;

  /**
   * Method to get the MoveLine from bankstatementrule. bankReconciliationLine.bankStatementLine is
   * used for the context of the formula.
   *
   * @param bankStatementRule: can not be null
   * @param bankReconciliationLine: can not be null
   * @param move : will be usable in context, can not be null
   * @return Optional of MoveLine : {@link MoveLine}
   * @throws AxelorException if the formula can not be eval to a MoveLine
   */
  Optional<MoveLine> getMoveLine(
      BankStatementRule bankStatementRule, BankReconciliationLine bankReconciliationLine, Move move)
      throws AxelorException;
}
