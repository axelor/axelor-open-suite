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

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementLineService;
import com.google.inject.Inject;

public class BankStatementManagementRepository extends BankStatementRepository {

  protected BankStatementLineService bankStatementLineService;

  @Inject
  public BankStatementManagementRepository(BankStatementLineService bankStatementLineService) {
    this.bankStatementLineService = bankStatementLineService;
  }

  @Override
  public BankStatement copy(BankStatement entity, boolean deep) {
    BankStatement bankStatement = super.copy(entity, deep);
    bankStatement.setStatusSelect(this.STATUS_RECEIVED);
    return bankStatement;
  }

  @Override
  public void remove(BankStatement entity) {
    bankStatementLineService.removeBankReconciliationLines(entity);
    super.remove(entity);
  }
}
