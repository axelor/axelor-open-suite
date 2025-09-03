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
package com.axelor.apps.bankpayment.service.bankstatement.classic;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementBankDetailsService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementCreateService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportAbstractService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportCheckService;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementLineImporter;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFetchService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.ImportConfigurationRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;

public class BankStatementImportClassicService extends BankStatementImportAbstractService {

  protected BankStatementLineImporter bankStatementLineImporter;
  protected AppBaseService appBaseService;
  protected ImportConfigurationRepository importConfigurationRepository;
  protected BankStatementLineRepository bankStatementLineRepository;
  protected MetaFiles metaFiles;

  public static final String BANK_STATEMENT_FILE_NAME = "bankstatementline.csv";

  @Inject
  public BankStatementImportClassicService(
      BankStatementRepository bankStatementRepository,
      BankStatementImportCheckService bankStatementImportCheckService,
      BankStatementLineFetchService bankStatementLineFetchService,
      BankStatementBankDetailsService bankStatementBankDetailsService,
      BankStatementLineImporter bankStatementLineImporter,
      AppBaseService appBaseService,
      ImportConfigurationRepository importConfigurationRepository,
      BankStatementLineRepository bankStatementLineRepository,
      MetaFiles metaFiles,
      BankStatementCreateService bankStatementCreateService) {
    super(
        bankStatementRepository,
        bankStatementImportCheckService,
        bankStatementLineFetchService,
        bankStatementBankDetailsService,
        bankStatementCreateService);
    this.bankStatementLineImporter = bankStatementLineImporter;
    this.appBaseService = appBaseService;
    this.importConfigurationRepository = importConfigurationRepository;
    this.bankStatementLineRepository = bankStatementLineRepository;
    this.metaFiles = metaFiles;
  }

  @Override
  public void runImport(BankStatement bankStatement) throws AxelorException, IOException {
    bankStatement = Beans.get(BankStatementRepository.class).find(bankStatement.getId());
    bankStatementLineImporter.setBankStatement(bankStatement);
    ImportHistory importHistory =
        bankStatementLineImporter.init(createImportConfiguration(bankStatement)).run();
    File readFile = MetaFiles.getPath(importHistory.getLogMetaFile()).toFile();
    FileUtils.readFileToString(readFile, StandardCharsets.UTF_8)
        .replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
    bankStatement = bankStatementRepository.find(bankStatement.getId());
    checkImport(bankStatement);
    updateBankDetailsBalance(bankStatement);
    computeBankStatementName(bankStatement);
    setBankStatementImported(bankStatement);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected ImportConfiguration createImportConfiguration(BankStatement bankStatement)
      throws IOException {
    ImportConfiguration importConfiguration = new ImportConfiguration();

    importConfiguration.setUser(AuthUtils.getUser());
    importConfiguration.setName(bankStatement.getName());
    importConfiguration.setBindMetaFile(
        bankStatement.getBankStatementFileFormat().getBindingFile());
    importConfiguration.setDataMetaFile(
        metaFiles.upload(
            new FileInputStream((MetaFiles.getPath(bankStatement.getBankStatementFile()).toFile())),
            BANK_STATEMENT_FILE_NAME));
    importConfiguration.setStartDateTime(appBaseService.getTodayDateTime().toLocalDateTime());

    return importConfigurationRepository.save(importConfiguration);
  }
}
