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
package com.axelor.csv.script;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportService;
import com.axelor.common.StringUtils;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportBankStatement {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected BankStatementImportService bankStatementImportService;
  protected MetaFiles metaFiles;
  protected BankStatementRepository bankStatementRepository;

  @Inject
  public ImportBankStatement(
      BankStatementImportService bankStatementImportService,
      MetaFiles metaFiles,
      BankStatementRepository bankStatementRepository) {
    this.bankStatementImportService = bankStatementImportService;
    this.metaFiles = metaFiles;
    this.bankStatementRepository = bankStatementRepository;
  }

  public Object importBankStatement(Object bean, Map<String, Object> values) {
    assert bean instanceof BankStatement;
    BankStatement bankStatement = (BankStatement) bean;

    String fileName = (String) values.get("bank_statement_demo");

    if (!StringUtils.isEmpty(fileName)) {
      try {
        InputStream stream =
            this.getClass().getResourceAsStream("/apps/demo-data/demo-bank-statement/" + fileName);
        if (stream != null) {
          final MetaFile metaFile = metaFiles.upload(stream, fileName);
          bankStatement.setBankStatementFile(metaFile);
          bankStatementRepository.save(bankStatement);
          bankStatementImportService.runImport(bankStatement, true);
        }
      } catch (Exception e) {
        LOG.error("Error when importing demo bank statement : {0}", e);
      }
    }
    return bankStatementRepository.find(bankStatement.getId());
  }
}
