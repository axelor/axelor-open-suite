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
package com.axelor.apps.base.service.configuration.template;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ConfigurationTemplate;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.imports.importer.FactoryImporter;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.jsoup.internal.StringUtil.StringJoiner;

public class ConfigurationTemplateServiceImpl implements ConfigurationTemplateService {

  protected MetaFiles metaFiles;
  protected FactoryImporter factoryImporter;

  @Inject
  public ConfigurationTemplateServiceImpl(MetaFiles metaFiles, FactoryImporter factoryImporter) {
    this.metaFiles = metaFiles;
    this.factoryImporter = factoryImporter;
  }

  @Override
  public boolean installConfig(Company company) throws AxelorException {
    ConfigurationTemplate configurationTemplate = company.getConfigurationTemplate();

    return installProcess(configurationTemplate, company);
  }

  public boolean installProcess(ConfigurationTemplate configurationTemplate, Company company)
      throws AxelorException {
    try {
      Map<String, Object> importContext = new HashMap<String, Object>();
      importContext.put("_companyId", company.getId());
      importContext.put("_companyName", company.getName());
      importContext.put("_companyCode", company.getCode());
      importContext.put("_dataFileName", configurationTemplate.getMetaFile().getFileName());

      importConfigData(configurationTemplate, importContext);

      return true;
    } catch (IOException e) {
      TraceBackService.trace(e);
      return false;
    }
  }

  public void importConfigData(
      ConfigurationTemplate configurationTemplate, Map<String, Object> importContext)
      throws IOException, AxelorException {

    ImportConfiguration importConfiguration = new ImportConfiguration();
    importConfiguration.setDataMetaFile(configurationTemplate.getMetaFile());
    importConfiguration.setBindMetaFile(configurationTemplate.getBindingFile());

    factoryImporter.createImporter(importConfiguration).run(importContext);
  }

  @Override
  public String installConfigForAllCompanies(ConfigurationTemplate configurationTemplate) {
    List<Company> companyList =
        JPA.all(Company.class)
            .filter("self.configurationTemplate = :configurationTemplate")
            .bind("configurationTemplate", configurationTemplate)
            .fetch();

    if (CollectionUtils.isEmpty(companyList)) {
      return I18n.get(BaseExceptionMessage.CONFIGURATION_TEMPLATE_NO_COMPANY_TO_CONFIGURE);
    }
    int done = 0;
    int error = 0;
    StringJoiner sj = new StringJoiner("\n");
    for (Company company : companyList) {
      try {
        installProcess(configurationTemplate, company);
        done++;
      } catch (Exception e) {
        sj.add(
            I18n.get(
                String.format(
                    BaseExceptionMessage.CONFIG_IMPORT_ERROR_FOR_COMPANY, company.getCode())));
        error++;
      }
    }
    sj.add(I18n.get(String.format(BaseExceptionMessage.CONFIG_IMPORT_DONE, done, error)));
    return sj.toString();
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void linkCompanies(
      List<Company> companyList, ConfigurationTemplate configurationTemplate) {
    if (CollectionUtils.isEmpty(companyList)) {
      return;
    }
    for (Company company : companyList) {
      company.setConfigurationTemplate(configurationTemplate);
      JPA.save(company);
    }
  }
}
