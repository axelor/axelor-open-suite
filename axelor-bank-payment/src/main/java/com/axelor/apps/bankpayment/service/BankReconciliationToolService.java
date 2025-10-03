/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.base.db.Company;
import com.axelor.common.ObjectUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BankReconciliationToolService {

  public static boolean isForeignCurrency(BankReconciliation bankReconciliation) {
    Objects.requireNonNull(bankReconciliation);
    Company company = bankReconciliation.getCompany();

    if (company != null && company.getCurrency() != null) {
      return !company.getCurrency().equals(bankReconciliation.getCurrency());
    }
    return false;
  }

  public static List<MoveLine> getMoveLineOnMultipleReconciliationLine(
      BankReconciliation bankReconciliation) {
    List<MoveLine> errorMoveLineList = new ArrayList<>();

    if (bankReconciliation == null
        || ObjectUtils.isEmpty(bankReconciliation.getBankReconciliationLineList())) {
      return errorMoveLineList;
    }

    List<MoveLine> moveLineList = new ArrayList<>();

    for (BankReconciliationLine bankReconciliationLine :
        bankReconciliation.getBankReconciliationLineList()) {
      MoveLine moveLine = bankReconciliationLine.getMoveLine();
      if (moveLine == null) {
        continue;
      }

      if (moveLineList.contains(moveLine)) {
        errorMoveLineList.add(moveLine);
      } else {
        moveLineList.add(moveLine);
      }
    }

    return errorMoveLineList;
  }
}
