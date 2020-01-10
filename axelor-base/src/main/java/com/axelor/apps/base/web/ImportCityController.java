/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.service.imports.ImportCityService;
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

    MetaFile dataFile = new MetaFile();

    String typeSelect = (String) request.getContext().get("typeSelect");

    if (typeSelect.equals("geonames")) {
      LinkedHashMap<String, Object> map =
          (LinkedHashMap<String, Object>) request.getContext().get("metaFile");

      List<ImportHistory> importHistoryList = new ArrayList<ImportHistory>();

      String downloadFileName = (String) request.getContext().get("autoImportTypeSelect");

      try {
        if (map != null) {
          dataFile =
              Beans.get(MetaFileRepository.class).find(Long.parseLong(map.get("id").toString()));
          importHistoryList.add(importCityService.importCity(typeSelect, dataFile));
        }

        if (downloadFileName != null) {
          dataFile = importCityService.downloadZip(downloadFileName);
          importHistoryList.add(importCityService.importCity(typeSelect, dataFile));
        }

        response.setAttr("$importHistoryList", "hidden", false);
        response.setAttr("$importHistoryList", "value", importHistoryList);

        response.setFlash(I18n.get("City import completed"));

      } catch (Exception e) {
        TraceBackService.trace(response, e);
      }
    }
  }
}
