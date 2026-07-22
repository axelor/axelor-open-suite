/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.loan;

import com.axelor.apps.account.db.Loan;
import com.axelor.apps.account.db.LoanLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class LoanAttrsServiceImpl implements LoanAttrsService {

  protected LoanConsistencyService loanConsistencyService;

  @Inject
  public LoanAttrsServiceImpl(LoanConsistencyService loanConsistencyService) {
    this.loanConsistencyService = loanConsistencyService;
  }

  @Override
  public Map<String, Map<String, Object>> getTotalsAttrsMap(Loan loan) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    BigDecimal interest = BigDecimal.ZERO;
    BigDecimal capital = BigDecimal.ZERO;
    BigDecimal insurance = BigDecimal.ZERO;
    BigDecimal total = BigDecimal.ZERO;
    if (loan.getLineList() != null) {
      for (LoanLine line : loan.getLineList()) {
        interest = interest.add(nz(line.getInterestAmount()));
        capital = capital.add(nz(line.getCapitalAmount()));
        insurance = insurance.add(nz(line.getInsuranceAmount()));
        total = total.add(nz(line.getTotalAmount()));
      }
    }

    addAttr("$totalCapital", "value", capital, attrsMap);
    addAttr("$totalInterest", "value", interest, attrsMap);
    addAttr("$totalInsurance", "value", insurance, attrsMap);
    addAttr("$totalPaid", "value", total, attrsMap);
    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getConsistencyAttrsMap(Loan loan) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    BigDecimal gap = loanConsistencyService.computeGap(loan);
    addAttr(
        "$theoreticalOutstanding",
        "value",
        loanConsistencyService.computeTheoreticalOutstanding(loan),
        attrsMap);
    addAttr(
        "$accountBalance", "value", loanConsistencyService.computeAccountBalance(loan), attrsMap);
    addAttr("$outstandingGap", "value", gap, attrsMap);

    String message = null;
    if (gap.signum() != 0 && loan.getBorrowingDebtAccount() != null) {
      message =
          String.format(
              I18n.get(AccountExceptionMessage.LOAN_CONSISTENCY_GAP),
              loan.getBorrowingDebtAccount().getCode());
    }
    addAttr("$consistencyAlertMessage", "value", message, attrsMap);
    return attrsMap;
  }

  protected BigDecimal nz(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }
    attrsMap.get(field).put(attr, value);
  }
}
