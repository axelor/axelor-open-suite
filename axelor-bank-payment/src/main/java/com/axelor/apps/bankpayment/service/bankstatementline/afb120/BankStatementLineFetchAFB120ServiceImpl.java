/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankstatementline.afb120;

import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.base.db.BankDetails;
import com.google.inject.Inject;
import java.util.Comparator;
import java.util.Optional;

public class BankStatementLineFetchAFB120ServiceImpl
    implements BankStatementLineFetchAFB120Service {
  protected BankPaymentBankStatementLineAFB120Repository
      bankPaymentBankStatementLineAFB120Repository;

  @Inject
  public BankStatementLineFetchAFB120ServiceImpl(
      BankPaymentBankStatementLineAFB120Repository bankPaymentBankStatementLineAFB120Repository) {
    this.bankPaymentBankStatementLineAFB120Repository =
        bankPaymentBankStatementLineAFB120Repository;
  }

  @Override
  public BankStatementLineAFB120 getLastBankStatementLineAFB120FromBankDetails(
      BankDetails bankDetails) {
    if (bankDetails != null) {
      String predicate =
          "self.bankDetails is not null AND self.bankDetails.id = "
              + bankDetails.getId()
              + " AND self.lineTypeSelect = "
              + BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE;
      Optional<BankStatementLineAFB120> id =
          bankPaymentBankStatementLineAFB120Repository
              .all()
              .filter(predicate)
              .fetchStream()
              .sorted(Comparator.comparing(BankStatementLineAFB120::getOperationDate))
              .findFirst();
      return id.isPresent()
          ? bankPaymentBankStatementLineAFB120Repository.find(id.get().getId())
          : null;
    }
    return null;
  }
}
