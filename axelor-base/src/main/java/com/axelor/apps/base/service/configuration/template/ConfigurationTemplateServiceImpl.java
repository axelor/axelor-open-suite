/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.imports.importer.FactoryImporter;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.apache.commons.collections.CollectionUtils;

public class ConfigurationTemplateServiceImpl implements ConfigurationTemplateService {

  protected FactoryImporter factoryImporter;

  @Inject
  public ConfigurationTemplateServiceImpl(FactoryImporter factoryImporter) {
    this.factoryImporter = factoryImporter;
  }

  @Override
  public void installConfig(Company company) throws AxelorException {
    ConfigurationTemplate configurationTemplate = company.getConfigurationTemplate();

    installProcess(configurationTemplate, company);
  }

  protected void installProcess(ConfigurationTemplate configurationTemplate, Company company)
      throws AxelorException {
    if (configurationTemplate == null
        || configurationTemplate.getBindingFile() == null
        || configurationTemplate.getMetaFile() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BaseExceptionMessage.CONFIG_IMPORT_MISSING_METAFILE));
    }

    Map<String, Object> importContext = new HashMap<>();
    importContext.put("_companyId", company.getId());
    importContext.put("_companyName", company.getName());
    importContext.put("_companyCode", company.getCode());
    importContext.put("_dataFileName", configurationTemplate.getMetaFile().getFileName());

    importConfigData(configurationTemplate, importContext);
  }

  protected void importConfigData(
      ConfigurationTemplate configurationTemplate, Map<String, Object> importContext)
      throws AxelorException {

    ImportConfiguration importConfiguration = new ImportConfiguration();
    importConfiguration.setDataMetaFile(configurationTemplate.getMetaFile());
    importConfiguration.setBindMetaFile(configurationTemplate.getBindingFile());

    try {
      factoryImporter.createImporter(importConfiguration).run(importContext);
    } catch (IOException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.CONFIG_IMPORT_ERROR));
    }
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
    StringJoiner sj = new StringJoiner("<br/>");
    for (Company company : companyList) {
      try {
        installProcess(configurationTemplate, company);
        done++;
      } catch (Exception e) {
        TraceBackService.trace(e);
        sj.add(
            String.format(
                I18n.get(BaseExceptionMessage.CONFIG_IMPORT_ERROR_FOR_COMPANY), company.getCode()));
        error++;
      }
    }
    sj.add(String.format(I18n.get(BaseExceptionMessage.CONFIG_IMPORT_DONE), done, error));
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
