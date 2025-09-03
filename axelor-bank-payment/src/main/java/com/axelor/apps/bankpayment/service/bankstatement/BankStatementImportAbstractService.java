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
package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;

public abstract class BankStatementImportAbstractService {

  protected BankStatementRepository bankStatementRepository;
  protected BankStatementImportCheckService bankStatementImportCheckService;
  protected BankStatementLineFetchService bankStatementLineFetchService;
  protected BankStatementBankDetailsService bankStatementBankDetailsService;
  protected BankStatementCreateService bankStatementCreateService;

  @Inject
  protected BankStatementImportAbstractService(
      BankStatementRepository bankStatementRepository,
      BankStatementImportCheckService bankStatementImportCheckService,
      BankStatementLineFetchService bankStatementLineFetchService,
      BankStatementBankDetailsService bankStatementBankDetailsService,
      BankStatementCreateService bankStatementCreateService) {
    this.bankStatementRepository = bankStatementRepository;
    this.bankStatementImportCheckService = bankStatementImportCheckService;
    this.bankStatementLineFetchService = bankStatementLineFetchService;
    this.bankStatementBankDetailsService = bankStatementBankDetailsService;
    this.bankStatementCreateService = bankStatementCreateService;
  }

  public abstract void runImport(BankStatement bankStatement) throws AxelorException, IOException;

  protected void checkImport(BankStatement bankStatement) throws AxelorException {
    bankStatementImportCheckService.checkImport(bankStatement);
  }

  protected void updateBankDetailsBalance(BankStatement bankStatement) {
    bankStatementBankDetailsService.updateBankDetailsBalance(bankStatement);
  }

  @Transactional
  public void setBankStatementImported(BankStatement bankStatement) {
    bankStatement.setName(bankStatementCreateService.computeName(bankStatement));
    bankStatement.setStatusSelect(BankStatementRepository.STATUS_IMPORTED);
    bankStatementRepository.save(bankStatement);
  }

  @Transactional
  protected void computeBankStatementName(BankStatement bankStatement) {
    bankStatement.setName(bankStatementCreateService.computeName(bankStatement));
  }
}
