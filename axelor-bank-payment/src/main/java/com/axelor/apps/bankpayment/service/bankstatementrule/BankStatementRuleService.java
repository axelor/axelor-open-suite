package com.axelor.apps.bankpayment.service.bankstatementrule;

import com.axelor.apps.account.db.Invoice;
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
   * Method to get the Invoice from bankstatementrule. bankReconciliationLine.bankStatementLine is
   * used for the context of the formula.
   *
   * @param bankStatementRule: can not be null
   * @param bankReconciliationLine: can not be null
   * @return Optional of invoice : {@link Invoice}
   * @throws AxelorException if the formula can not be eval to a Invoice
   */
  Optional<Invoice> getInvoice(
      BankStatementRule bankStatementRule, BankReconciliationLine bankReconciliationLine)
      throws AxelorException;
}
