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
package com.axelor.apps.bankpayment.service.bankstatement.afb120;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementBankDetailsService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementCreateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportAbstractService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportCheckService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineDeleteService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchService;
import com.axelor.apps.bankpayment.service.bankstatementline.afb120.BankStatementLineCreateAFB120Service;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;

public class BankStatementImportAFB120Service extends BankStatementImportAbstractService {
  protected BankStatementLineDeleteService bankStatementLineDeleteService;
  protected BankStatementLineCreateAFB120Service bankStatementLineCreateAFB120Service;

  @Inject
  public BankStatementImportAFB120Service(
      BankStatementRepository bankStatementRepository,
      BankStatementImportCheckService bankStatementImportCheckService,
      BankStatementLineFetchService bankStatementLineFetchService,
      BankStatementBankDetailsService bankStatementBankDetailsService,
      BankStatementLineDeleteService bankStatementLineDeleteService,
      BankStatementLineCreateAFB120Service bankStatementLineCreateAFB120Service,
      BankStatementCreateService bankStatementCreateService) {
    super(
        bankStatementRepository,
        bankStatementImportCheckService,
        bankStatementLineFetchService,
        bankStatementBankDetailsService,
        bankStatementCreateService);
    this.bankStatementLineDeleteService = bankStatementLineDeleteService;
    this.bankStatementLineCreateAFB120Service = bankStatementLineCreateAFB120Service;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void runImport(BankStatement bankStatement) throws AxelorException, IOException {
    bankStatementLineCreateAFB120Service.process(bankStatement);

    // The process from bankStatementFileAFB120Service clears the JPA cache, so we need to find the
    // bank statement.
    bankStatement = bankStatementRepository.find(bankStatement.getId());
    checkImport(bankStatement);
    updateBankDetailsBalance(bankStatement);
    computeBankStatementName(bankStatement);
    setBankStatementImported(bankStatement);
  }
}
