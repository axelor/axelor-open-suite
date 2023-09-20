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
package com.axelor.apps.maintenance.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ProdProcessController {

  public void print(ActionRequest request, ActionResponse response) {
    try {
      ProdProcess prodProcess = request.getContext().asType(ProdProcess.class);
      prodProcess = Beans.get(ProdProcessRepository.class).find(prodProcess.getId());
      BirtTemplate maintenanceProdProcessBirtTemplate =
          Beans.get(ProductionConfigService.class)
              .getProductionConfig(prodProcess.getCompany())
              .getMaintenanceProdProcessBirtTemplate();
      if (maintenanceProdProcessBirtTemplate == null
          || maintenanceProdProcessBirtTemplate.getTemplateMetaFile() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.BIRT_TEMPLATE_CONFIG_NOT_FOUND));
      }

      String title = prodProcess.getName();
      String fileLink =
          Beans.get(BirtTemplateService.class)
              .generateBirtTemplateLink(maintenanceProdProcessBirtTemplate, prodProcess, title);

      response.setView(ActionView.define(title).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
