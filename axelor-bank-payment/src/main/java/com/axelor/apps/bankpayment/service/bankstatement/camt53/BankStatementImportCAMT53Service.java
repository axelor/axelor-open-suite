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
package com.axelor.apps.bankpayment.service.bankstatement.camt53;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementBankDetailsService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementCreateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportAbstractService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportCheckService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchService;
import com.axelor.apps.bankpayment.service.bankstatementline.camt53.BankStatementLineCreateCAMT53Service;
import com.axelor.apps.base.AxelorException;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.io.IOException;

public class BankStatementImportCAMT53Service extends BankStatementImportAbstractService {

  protected BankStatementLineCreateCAMT53Service bankStatementLineCreateCAMT53Service;

  @Inject
  public BankStatementImportCAMT53Service(
      BankStatementRepository bankStatementRepository,
      BankStatementImportCheckService bankStatementImportCheckService,
      BankStatementLineFetchService bankStatementLineFetchService,
      BankStatementBankDetailsService bankStatementBankDetailsService,
      BankStatementCreateService bankStatementCreateService,
      BankStatementLineCreateCAMT53Service bankStatementLineCreateCAMT53Service) {
    super(
        bankStatementRepository,
        bankStatementImportCheckService,
        bankStatementLineFetchService,
        bankStatementBankDetailsService,
        bankStatementCreateService);
    this.bankStatementLineCreateCAMT53Service = bankStatementLineCreateCAMT53Service;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void runImport(BankStatement bankStatement) throws AxelorException, IOException {
    BankStatement processedBankStatement =
        bankStatementLineCreateCAMT53Service.processCAMT53(bankStatement);

    checkImport(processedBankStatement);
    updateBankDetailsBalance(processedBankStatement);
    computeBankStatementName(processedBankStatement);
    setBankStatementImported(processedBankStatement);
  }
}
