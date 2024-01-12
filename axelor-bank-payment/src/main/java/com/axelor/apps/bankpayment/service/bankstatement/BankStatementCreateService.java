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
package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

public class BankStatementCreateService {

  public String computeName(BankStatement bankStatement) {
    StringJoiner joiner = new StringJoiner("-");

    if (bankStatement.getBankStatementFileFormat() != null) {
      joiner.add(bankStatement.getBankStatementFileFormat().getName());
    }

    if (bankStatement.getFromDate() != null) {
      joiner.add(bankStatement.getFromDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
    }

    if (bankStatement.getToDate() != null) {
      joiner.add(bankStatement.getToDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
    }
    return joiner.toString();
  }
}
