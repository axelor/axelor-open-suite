/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.HandleExceptionResponse;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorImportService;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ConfiguratorCreatorController {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  /**
   * Called from the configurator creator form on formula changes
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  @HandleExceptionResponse
  public void updateAndActivate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ConfiguratorCreator creator = request.getContext().asType(ConfiguratorCreator.class);
    ConfiguratorCreatorService configuratorCreatorService =
        Beans.get(ConfiguratorCreatorService.class);
    creator = Beans.get(ConfiguratorCreatorRepository.class).find(creator.getId());
    configuratorCreatorService.updateIndicators(creator);
    configuratorCreatorService.activate(creator);
    response.setSignal("refresh-app", true);
  }

  /**
   * Called from configurator creator grid view, on clicking import button. Call {@link
   * ConfiguratorCreatorService#importConfiguratorCreators(String)}.
   *
   * @param request
   * @param response
   * @throws IOException
   */
  @HandleExceptionResponse
  public void importConfiguratorCreators(ActionRequest request, ActionResponse response)
      throws IOException {
    String pathDiff = (String) ((Map) request.getContext().get("dataFile")).get("filePath");
    String importLog =
        Beans.get(ConfiguratorCreatorImportService.class).importConfiguratorCreators(pathDiff);
    response.setValue("importLog", importLog);
  }

  @HandleExceptionResponse
  public void updateAttributes(ActionRequest request, ActionResponse response) {
    ConfiguratorCreator creator = request.getContext().asType(ConfiguratorCreator.class);
    creator = Beans.get(ConfiguratorCreatorRepository.class).find(creator.getId());
    Beans.get(ConfiguratorCreatorService.class).updateAttributes(creator);
    response.setReload(true);
  }
}
