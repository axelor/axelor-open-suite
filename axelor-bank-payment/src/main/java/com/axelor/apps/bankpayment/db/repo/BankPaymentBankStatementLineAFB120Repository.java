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
package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.service.CurrencyScaleServiceBankPayment;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.inject.Beans;
import java.time.LocalDate;
import java.util.Map;

public class BankPaymentBankStatementLineAFB120Repository
    extends BankStatementLineAFB120Repository {
  public BankStatementLineAFB120 findLineBetweenDate(
      LocalDate fromDate,
      LocalDate toDate,
      int lineType,
      boolean soonest,
      BankDetails bankDetails) {
    String order = "operationDate";
    if (!soonest) {
      order = "-" + order;
    }
    return all()
        .filter(
            "operationDate >= :fromDate"
                + " AND operationDate <= :toDate"
                + " AND lineTypeSelect = :lineType"
                + " AND bankDetails = :bankDetails")
        .bind("fromDate", fromDate)
        .bind("toDate", toDate)
        .bind("lineType", lineType)
        .bind("bankDetails", bankDetails)
        .order(order)
        .fetchOne();
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long bankStatementLineAFB120Id = (Long) json.get("id");
    BankStatementLineAFB120 bankStatementLineAFB120 =
        Beans.get(BankStatementLineAFB120Repository.class).find(bankStatementLineAFB120Id);

    json.put(
        "$currencyNumberOfDecimals",
        Beans.get(CurrencyScaleServiceBankPayment.class).getScale(bankStatementLineAFB120));

    return super.populate(json, context);
  }
}
