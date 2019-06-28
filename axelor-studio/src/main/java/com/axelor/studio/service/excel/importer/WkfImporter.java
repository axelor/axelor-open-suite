/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.excel.importer;

import com.axelor.studio.service.CommonService;
import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WkfImporter {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Map<String, List<String[]>> wkfData;

  private List<String> realModels;

  public void wkfImport(
      String module,
      DataReaderService reader,
      Map<String, List<String[]>> wkfData,
      List<String> wkfList,
      List<String> realModels,
      ZipOutputStream zipOut) {

    this.wkfData = wkfData;
    this.realModels = realModels;

    List<String[]> wkfs = new ArrayList<>();
    List<String[]> wkfNodes = new ArrayList<>();
    List<String[]> wkfTransitions = new ArrayList<>();

    for (String key : wkfList) {

      log.debug("Importing sheet: {}", key);

      int totalLines = reader.getTotalLines(key);
      if (totalLines == 0) {
        continue;
      }

      for (int rowNum = 0; rowNum < totalLines; rowNum++) {
        if (rowNum == 0) {
          continue;
        }

        String[] row = reader.read(key, rowNum);
        if (row == null) {
          continue;
        }

        if (key.equals("Wkf")) {
          createWkf(row, wkfs);

        } else if (key.equals("WkfNode")) {
          createWkfNode(row, wkfNodes);

        } else if (key.equals("WkfTransition")) {
          createWkfTransition(row, wkfTransitions);
        }
      }
    }
  }

  private void createWkf(String[] row, List<String[]> wkfs) {

    Map<String, String> valMap = ExcelImporterService.createValMap(row, CommonService.WKF_HEADER);
    String jsonField = valMap.get(CommonService.WKF_JSON_FIELD);
    String isJson = "false";

    if (Strings.isNullOrEmpty(valMap.get(CommonService.WKF_JSON_FIELD))
        && realModels.contains(valMap.get(CommonService.WKF_MODEL))) {
      jsonField = "attrs";
    }

    if (valMap.get(CommonService.WKF_JSON) != null
        && valMap.get(CommonService.WKF_JSON).equals("x")
        && !realModels.contains(valMap.get(CommonService.WKF_MODEL))) {
      isJson = "true";
    }

    wkfs.add(
        new String[] {
          valMap.get(CommonService.WKF_NAME),
          valMap.get(CommonService.WKF_MODEL),
          jsonField,
          isJson,
          valMap.get(CommonService.WKF_STATUS),
          valMap.get(CommonService.WKF_DISPLAY),
          valMap.get(CommonService.WKF_XML),
          valMap.get(CommonService.WKF_APP),
          valMap.get(CommonService.WKF_DESC)
        });

    wkfData.put("Wkf", wkfs);
  }

  private void createWkfNode(String[] row, List<String[]> wkfNodes) {

    Map<String, String> valMap =
        ExcelImporterService.createValMap(row, CommonService.WKF_NODE_HEADER);

    wkfNodes.add(
        new String[] {
          valMap.get(CommonService.WKF_NODE_NAME),
          valMap.get(CommonService.WKF_NODE_TITLE),
          valMap.get(CommonService.WKF_NODE_XML),
          valMap.get(CommonService.WKF_NODE_WKF),
          valMap.get(CommonService.WKF_NODE_FIELD),
          valMap.get(CommonService.WKF_NODE_FIELD_MODEL),
          valMap.get(CommonService.WKF_NODE_SEQ),
          (valMap.get(CommonService.WKF_NODE_START) != null
                  && valMap.get(CommonService.WKF_NODE_START).equals("x"))
              ? "true"
              : "false",
          (valMap.get(CommonService.WKF_NODE_END) != null
                  && valMap.get(CommonService.WKF_NODE_END).equals("x"))
              ? "true"
              : "false",
          valMap.get(CommonService.WKF_NODE_ACTIONS)
        });

    wkfData.put("WkfNode", wkfNodes);
  }

  private void createWkfTransition(String[] row, List<String[]> wkfTransitions) {

    Map<String, String> valMap =
        ExcelImporterService.createValMap(row, CommonService.WKF_TRANSITION_HEADER);

    wkfTransitions.add(
        new String[] {
          valMap.get(CommonService.WKF_TRANS_NAME),
          valMap.get(CommonService.WKF_TRANS_XML),
          (valMap.get(CommonService.WKF_TRANS_BUTTON) != null
                  && valMap.get(CommonService.WKF_TRANS_BUTTON).equals("x"))
              ? "true"
              : "false",
          valMap.get(CommonService.WKF_TRANS_BUTTON_TITLE),
          valMap.get(CommonService.WKF_TRANS_WKF),
          valMap.get(CommonService.WKF_TRANS_SOURCE_NODE),
          valMap.get(CommonService.WKF_TRANS_TARGET_NODE),
          valMap.get(CommonService.WKF_TRANS_ALERT_TYPE),
          valMap.get(CommonService.WKF_TRANS_ALERT_MSG),
          valMap.get(CommonService.WKF_TRANS_SUCCESS_MSG)
        });

    wkfData.put("WkfTransition", wkfTransitions);
  }
}
