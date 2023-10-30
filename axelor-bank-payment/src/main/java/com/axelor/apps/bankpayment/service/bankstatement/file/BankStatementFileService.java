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
package com.axelor.apps.bankpayment.service.bankstatement.file;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportService;
import com.axelor.apps.base.AxelorException;
import com.axelor.db.JPA;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;

public abstract class BankStatementFileService {

  protected BankStatement bankStatement;
  protected File file;
  protected String bankStatementFileFormat;
  protected final BankStatementRepository bankStatementRepository;
  protected final BankStatementImportService bankStatementService;

  @Inject
  public BankStatementFileService(
      BankStatementRepository bankStatementRepository,
      BankStatementImportService bankStatementService) {
    this.bankStatementRepository = bankStatementRepository;
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
    return JPA.em().contains(bankStatement)
        ? bankStatement
        : bankStatementRepository.find(bankStatement.getId());
  }
}
