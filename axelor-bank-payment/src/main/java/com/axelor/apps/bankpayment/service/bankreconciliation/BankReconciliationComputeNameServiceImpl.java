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

import com.axelor.apps.bankpayment.db.BankReconciliation;
import java.time.format.DateTimeFormatter;

public class BankReconciliationComputeNameServiceImpl
    implements BankReconciliationComputeNameService {

  @Override
  public String computeName(BankReconciliation bankReconciliation) {

    String name = "";
    if (bankReconciliation.getCompany() != null) {
      name += bankReconciliation.getCompany().getCode();
    }
    if (bankReconciliation.getCurrency() != null) {
      if (name != "") {
        name += "-";
      }
      name += bankReconciliation.getCurrency().getCodeISO();
    }
    if (bankReconciliation.getBankDetails() != null) {
      if (name != "") {
        name += "-";
      }
      name += bankReconciliation.getBankDetails().getAccountNbr();
    }
    if (bankReconciliation.getFromDate() != null) {
      if (name != "") {
        name += "-";
      }
      name += bankReconciliation.getFromDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
    if (bankReconciliation.getToDate() != null) {
      if (name != "") {
        name += "-";
      }
      name += bankReconciliation.getToDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }

    return name;
  }
}
