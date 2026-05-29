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
package com.axelor.apps.base.web;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ConfigurationTemplate;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.configuration.template.ConfigurationTemplateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.MapHelper;
import java.util.List;

public class ConfigurationTemplateController {

  public void installConfig(ActionRequest request, ActionResponse response) {
    try {
      Company company = request.getContext().asType(Company.class);
      company = Beans.get(CompanyRepository.class).find(company.getId());
      Beans.get(ConfigurationTemplateService.class).installConfig(company);
      response.setInfo(I18n.get(BaseExceptionMessage.CONFIG_IMPORT_SUCCESS));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void importConfigurationForCompanies(ActionRequest request, ActionResponse response) {
    try {
      ConfigurationTemplate configurationTemplate =
          request.getContext().asType(ConfigurationTemplate.class);

      response.setNotify(
          Beans.get(ConfigurationTemplateService.class)
              .installConfigForAllCompanies(configurationTemplate));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void linkCompanies(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      ConfigurationTemplate configurationTemplate =
          MapHelper.get(context, ConfigurationTemplate.class, "_configurationTemplate");
      List<Company> companyList =
          MapHelper.getCollection(context, Company.class, "_companiesToLink");

      Beans.get(ConfigurationTemplateService.class)
          .linkCompanies(companyList, configurationTemplate);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
