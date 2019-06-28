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
package com.axelor.studio.service.validator;

import com.axelor.i18n.I18n;
import com.axelor.studio.service.excel.importer.DataReaderService;
import java.io.IOException;

public class WkfValidator {

  private static final int NAME = 0;
  private static final int WKF_MODEL = 1;
  private static final int WKF_STATUS = 4;
  private static final int WKF_NODE_TITLE = 1;

  private ValidatorService validatorService;

  private DataReaderService reader;

  public void validate(ValidatorService validatorService, DataReaderService reader)
      throws IOException {

    this.validatorService = validatorService;
    this.reader = reader;

    validateWkf("Wkf");
    validateWkfNode("WkfNode");
    validateWkfTransition("WkfTransition");
  }

  private void validateWkf(String key) throws IOException {

    if (key == null || reader == null) {
      return;
    }

    int totalLines = reader.getTotalLines(key);

    for (int rowNum = 1; rowNum < totalLines; rowNum++) {
      String[] row = reader.read(key, rowNum);
      if (row == null) {
        continue;
      }

      String name = row[NAME];
      String model = row[WKF_MODEL];
      String status = row[WKF_STATUS];

      if (name == null || model == null || status == null) {
        validatorService.addLog(I18n.get("Name or model or status is empty"), key, rowNum);
      }
    }
  }

  private void validateWkfNode(String key) throws IOException {

    if (key == null || reader == null) {
      return;
    }

    int totalLines = reader.getTotalLines(key);

    for (int rowNum = 1; rowNum < totalLines; rowNum++) {
      String[] row = reader.read(key, rowNum);
      if (row == null) {
        continue;
      }

      String name = row[NAME];
      String title = row[WKF_NODE_TITLE];

      if (name == null || title == null) {
        validatorService.addLog(I18n.get("Name or title is empty"), key, rowNum);
      }
    }
  }

  private void validateWkfTransition(String key) throws IOException {

    if (key == null || reader == null) {
      return;
    }

    int totalLines = reader.getTotalLines(key);

    for (int rowNum = 1; rowNum < totalLines; rowNum++) {
      String[] row = reader.read(key, rowNum);
      if (row == null) {
        continue;
      }

      String name = row[NAME];
      if (name == null) {
        validatorService.addLog(I18n.get("Name is empty"), key, rowNum);
      }
    }
  }
}
