/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankstatementrule;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementRule;
import com.axelor.apps.bankpayment.db.repo.BankStatementRuleRepository;
import com.axelor.apps.bankpayment.service.bankstatementquery.BankStatementQueryService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
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
