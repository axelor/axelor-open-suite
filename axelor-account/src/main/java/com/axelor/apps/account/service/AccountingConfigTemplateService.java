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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingConfigTemplate;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingConfigTemplateRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.imports.importer.FactoryImporter;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;

public class AccountingConfigTemplateService {

  @Inject private FactoryImporter factoryImporter;

  @Inject private MetaFiles metaFiles;

  protected AccountConfigRepository accountConfigRepo;
  protected CompanyRepository companyRepo;
  protected AccountingConfigTemplateRepository accountingConfigTemplateRepository;
  protected AccountRepository accountRepository;

  @Inject
  public AccountingConfigTemplateService(
      AccountConfigRepository accountConfigRepo,
      CompanyRepository companyRepo,
      AccountingConfigTemplateRepository accountingConfigTemplateRepository,
      AccountRepository accountRepository) {

    this.accountConfigRepo = accountConfigRepo;
    this.companyRepo = companyRepo;
    this.accountingConfigTemplateRepository = accountingConfigTemplateRepository;
    this.accountRepository = accountRepository;
  }

  public boolean installAccountChart(AccountConfig accountConfig) throws AxelorException {

    AccountingConfigTemplate act = accountConfig.getAccountingConfigTemplate();
    Company company = accountConfig.getCompany();
    long accountCounter =
        accountRepository
            .all()
            .filter("self.company.id = ?1 AND self.parentAccount != null", company.getId())
            .count();

    if (accountCounter > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(AccountExceptionMessage.ACCOUNT_CHART_3));
    }

    return installProcess(accountConfig, act, company);
  }

  public boolean installProcess(
      AccountConfig accountConfig, AccountingConfigTemplate act, Company company)
      throws AxelorException {
    try {
      if (act.getMetaFile() == null) {
        return false;
      }
      String configFileName =
          Boolean.TRUE.equals(accountConfig.getIsImportAccountChartOnly())
              ? "/l10n/chart-config-account.xml"
              : "/l10n/chart-config.xml";
      InputStream inputStream = this.getClass().getResourceAsStream(configFileName);
      if (inputStream == null) {
        return false;
      }

      File configFile = createConfigFile(inputStream);

      Map<String, Object> importContext = new HashMap<String, Object>();
      importContext.put("_companyId", company.getId());

      importAccountChartData(act, configFile, importContext);

      updateChartCompany(act, company, accountConfig);

      FileUtils.forceDelete(configFile);

      return true;
    } catch (IOException e) {
      TraceBackService.trace(e);
      return false;
    }
  }

  protected File createConfigFile(InputStream inputStream)
      throws IOException, FileNotFoundException {

    File configFile = File.createTempFile("input-config", ".xml");

    try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
      int read = 0;
      byte[] bytes = new byte[1024];
      while ((read = inputStream.read(bytes)) != -1) {
        outputStream.write(bytes, 0, read);
      }
      outputStream.close();
    }

    return configFile;
  }

  @Transactional
  public void updateChartCompany(
      AccountingConfigTemplate act, Company company, AccountConfig accountConfig) {

    accountConfig = accountConfigRepo.find(accountConfig.getId());
    updateAccountConfigBooleans(accountConfig);
    act = accountingConfigTemplateRepository.find(act.getId());
    accountConfig.setAccountingConfigTemplate(act);
    company = companyRepo.find(company.getId());
    Set<Company> companySet = act.getCompanySet();
    companySet.add(company);
    act.setCompanySet(companySet);
    accountingConfigTemplateRepository.save(act);
    accountConfigRepo.save(accountConfig);
  }

  protected void updateAccountConfigBooleans(AccountConfig accountConfig) {
    accountConfig.setHasChartImported(true);
    accountConfig.setHasAccountingConfigTemplateImported(
        !accountConfig.getIsImportAccountChartOnly());
  }

  public void importAccountChartData(
      AccountingConfigTemplate act, File configFile, Map<String, Object> importContext)
      throws IOException, AxelorException {

    ImportConfiguration importConfiguration = new ImportConfiguration();
    importConfiguration.setDataMetaFile(act.getMetaFile());
    importConfiguration.setBindMetaFile(metaFiles.upload(configFile));

    factoryImporter.createImporter(importConfiguration).run(importContext);
  }
}
