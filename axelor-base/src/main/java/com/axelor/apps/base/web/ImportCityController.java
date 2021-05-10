/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.service.imports.ImportCityService;
import com.axelor.apps.base.service.imports.ImportCityServiceImpl.GEONAMES_FILE;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ImportCityController {

  @Inject private ImportCityService importCityService;

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

      String typeSelect = (String) request.getContext().get("typeSelect");
      if (CityRepository.TYPE_SELECT_GEONAMES.equals(typeSelect)) {

        String importTypeSelect = (String) request.getContext().get("importTypeSelect");
        switch (importTypeSelect) {
          case CityRepository.IMPORT_TYPE_SELECT_AUTO:
            String downloadFileName = (String) request.getContext().get("autoImportTypeSelect");
            importHistoryList = importFromGeonamesAutoConfig(downloadFileName, typeSelect);
            break;

          case CityRepository.IMPORT_TYPE_SELECT_MANUAL:
            Map<String, Object> map =
                (LinkedHashMap<String, Object>) request.getContext().get("metaFile");
            importHistoryList = importFromGeonamesManualConfig(map, typeSelect);
            break;

          default:
            break;
        }
      }

      response.setAttr("$importHistoryList", "hidden", false);
      response.setAttr("$importHistoryList", "value", importHistoryList);

      response.setFlash(I18n.get("City import completed"));

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Imports cities from a predefined Geonames configuration.
   *
   * @param downloadFileName
   * @param typeSelect
   * @return
   * @throws Exception
   */
  private List<ImportHistory> importFromGeonamesAutoConfig(
      String downloadFileName, String typeSelect) throws Exception {
    MetaFile zipImportDataFile = importCityService.downloadZip(downloadFileName, GEONAMES_FILE.ZIP);
    MetaFile dumpImportDataFile =
        importCityService.downloadZip(downloadFileName, GEONAMES_FILE.DUMP);

    List<ImportHistory> importHistoryList = new ArrayList<>();
    importHistoryList.add(importCityService.importCity(typeSelect + "-zip", zipImportDataFile));
    importHistoryList.add(importCityService.importCity(typeSelect + "-dump", dumpImportDataFile));

    return importHistoryList;
  }

  /**
   * Imports cities from a custom Geonames file. This is useful for the countries not present in the
   * predefined list.
   *
   * @param map
   * @param typeSelect
   * @return
   */
  private List<ImportHistory> importFromGeonamesManualConfig(
      Map<String, Object> map, String typeSelect) {
    List<ImportHistory> importHistoryList = new ArrayList<>();

    if (map != null) {
      MetaFile dataFile =
          Beans.get(MetaFileRepository.class).find(Long.parseLong(map.get("id").toString()));
      importHistoryList.add(importCityService.importCity(typeSelect + "-dump", dataFile));
    }

    return importHistoryList;
  }
}
