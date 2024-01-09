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
package com.axelor.apps.bankpayment.service.bankstatement.line;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportService;
import com.axelor.apps.bankpayment.service.bankstatement.line.afb120.StructuredContentLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class BankStatementLineCreateAbstractService {

  protected BankStatement bankStatement;
  protected File file;
  protected String bankStatementFileFormat;
  protected final BankStatementRepository bankStatementRepository;
  protected final BankStatementImportService bankStatementService;

  @Inject
  protected BankStatementLineCreateAbstractService(
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

  public void process(BankStatement bankStatement) throws IOException, AxelorException {
    setBankStatement(bankStatement);
    process();
  }

  protected void process() throws IOException, AxelorException {

    List<StructuredContentLine> structuredContentFile = readFile();

    int sequence = 0;
    findBankStatement();

    for (StructuredContentLine structuredContentLine : structuredContentFile) {
      try {
        createBankStatementLine(structuredContentLine, sequence++);
      } catch (Exception e) {
        TraceBackService.trace(
            new Exception(String.format("Line %s : %s", sequence, e), e),
            ExceptionOriginRepository.IMPORT);
        findBankStatement();
      } finally {
        if (sequence % 10 == 0) {
          JPA.clear();
          findBankStatement();
        }
      }
    }

    JPA.clear();
    findBankStatement();
  }

  protected abstract List<StructuredContentLine> readFile() throws IOException, AxelorException;

  protected abstract BankStatementLine createBankStatementLine(
      StructuredContentLine structuredContentLine, int sequence);

  protected File getFile(BankStatement bankStatement) {
    return MetaFiles.getPath(bankStatement.getBankStatementFile()).toFile();
  }

  protected BankStatement findBankStatement() {
    bankStatement =
        JPA.em().contains(bankStatement)
            ? bankStatement
            : bankStatementRepository.find(bankStatement.getId());
    return bankStatement;
  }
}
