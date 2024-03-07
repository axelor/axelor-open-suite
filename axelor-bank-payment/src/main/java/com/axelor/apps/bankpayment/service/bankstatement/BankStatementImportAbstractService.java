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
package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;

public abstract class BankStatementImportAbstractService {

  protected BankStatementRepository bankStatementRepository;

  @Inject
  protected BankStatementImportAbstractService(BankStatementRepository bankStatementRepository) {
    this.bankStatementRepository = bankStatementRepository;
  }

  public abstract void runImport(BankStatement bankStatement) throws AxelorException, IOException;

  protected abstract void checkImport(BankStatement bankStatement)
      throws AxelorException, IOException;

  protected abstract void updateBankDetailsBalance(BankStatement bankStatement);

  @Transactional
  public void setBankStatementImported(BankStatement bankStatement) {
    bankStatement.setStatusSelect(BankStatementRepository.STATUS_IMPORTED);
    bankStatementRepository.save(bankStatement);
  }
}
