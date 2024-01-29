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
package com.axelor.csv.script;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementCreateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementService;
import java.util.Map;
import javax.inject.Inject;

public class ImportBankStatement {

  protected BankStatementCreateService bankStatementCreateService;
  protected BankStatementService bankStatementService;

  @Inject
  public ImportBankStatement(
      BankStatementCreateService bankStatementCreateService,
      BankStatementService bankStatementService) {
    this.bankStatementCreateService = bankStatementCreateService;
    this.bankStatementService = bankStatementService;
  }

  public Object importBankStatement(Object bean, Map<String, Object> values) {
    assert bean instanceof BankStatement;
    BankStatement bankStatement = (BankStatement) bean;

    if (bankStatement.getName() == null) {
      bankStatement.setName(bankStatementCreateService.computeName(bankStatement));
    }
    bankStatementService.updateStatus(bankStatement);
    return bankStatement;
  }
}
