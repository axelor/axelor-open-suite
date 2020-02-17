/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankstatement.file;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementService;
import com.axelor.exception.AxelorException;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;

public abstract class BankStatementFileService {

  protected BankStatement bankStatement;
  protected File file;
  protected String bankStatementFileFormat;

  protected final BankStatementService bankStatementService;

  @Inject
  public BankStatementFileService(BankStatementService bankStatementService) {
    this.bankStatementService = bankStatementService;
  }

  public void setBankStatement(BankStatement bankStatement) {
    this.bankStatement = bankStatement;
    this.file = getFile(bankStatement);
    this.bankStatementFileFormat =
        bankStatement.getBankStatementFileFormat().getStatementFileFormatSelect();
  }

  protected File getFile(BankStatement bankStatement) {
    return MetaFiles.getPath(bankStatement.getBankStatementFile()).toFile();
  }

  public void process(BankStatement bankStatement) throws IOException, AxelorException {
    setBankStatement(bankStatement);
    process();
  }

  public void process() throws IOException, AxelorException {
    if (bankStatement == null) {
      throw new IllegalStateException("Bank statement is not set.");
    }
  }

  protected BankStatement findBankStatement() {
    bankStatement = bankStatementService.find(bankStatement);
    return bankStatement;
  }
}
