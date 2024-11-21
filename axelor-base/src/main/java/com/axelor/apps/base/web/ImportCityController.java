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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.imports.ImportCityService;
import com.axelor.apps.base.translation.ITranslation;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ImportCityController {

  /**
   * Import cities
   *
   * @param request
   * @param response
   * @throws InterruptedException
   */
  @SuppressWarnings("unchecked")
  public void importCity(ActionRequest request, ActionResponse response) {
    try {
      List<ImportHistory> importHistoryList = null;
      Map<String, Object> importCityMap = null;
      MetaFile errorFile = null;

      String typeSelect = (String) request.getContext().get("typeSelect");
      if (CityRepository.TYPE_SELECT_GEONAMES.equals(typeSelect)) {

        String importTypeSelect = (String) request.getContext().get("importTypeSelect");
        switch (importTypeSelect) {
          case CityRepository.IMPORT_TYPE_SELECT_AUTO:
            String downloadFileName = (String) request.getContext().get("autoImportTypeSelect");
            importCityMap =
                Beans.get(ImportCityService.class)
                    .importFromGeonamesAutoConfig(downloadFileName, typeSelect);
            break;

          case CityRepository.IMPORT_TYPE_SELECT_MANUAL:
            Map<String, Object> map =
                (LinkedHashMap<String, Object>) request.getContext().get("metaFile");
            importCityMap =
                Beans.get(ImportCityService.class).importFromGeonamesManualConfig(map, typeSelect);
            break;

          default:
            break;
        }
      }

      if (importCityMap.containsKey("importHistoryList")
          && importCityMap.containsKey("errorFile")) {
        importHistoryList = (List<ImportHistory>) importCityMap.get("importHistoryList");
        errorFile = (MetaFile) importCityMap.get("errorFile");
        if (errorFile != null) {
          response.setInfo(I18n.get(BaseExceptionMessage.CITIES_IMPORT_FAILED));
          response.setAttr("errorFile", "hidden", false);
          response.setValue("errorFile", errorFile);
        } else {
          response.setAttr("$importHistoryList", "hidden", false);
          response.setAttr("errorFile", "hidden", true);
          response.setAttr("$importHistoryList", "value", importHistoryList);
          response.setInfo(I18n.get(ITranslation.BASE_GEONAMES_CITY_IMPORT_COMPLETED));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
