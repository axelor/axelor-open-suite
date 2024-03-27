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
