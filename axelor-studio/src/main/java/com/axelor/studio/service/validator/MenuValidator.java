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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.axelor.i18n.I18n;
import com.axelor.studio.service.excel.importer.DataReaderService;

public class MenuValidator {

  public static final int OBJECT = 1;
  public static final int NAME = 3;
  public static final int TITLE = 4;
  public static final int TITLE_FR = 5;
  public static final int ORDER = 7;

  private ValidatorService validatorService;

  private List<String> menus;

  public void validate(ValidatorService validatorService, DataReaderService reader, String key)
      throws IOException {

    this.validatorService = validatorService;
    if (key == null || reader == null) {
      return;
    }

    menus = new ArrayList<String>();

    int totalLines = reader.getTotalLines(key);

    for (int rowNum = 1; rowNum < totalLines; rowNum++) {

      String[] row = reader.read(key, rowNum);
      if (row == null) {
        continue;
      }

      validateMenu(row, key, rowNum);
    }
  }

  private void validateMenu(String[] row, String key, int rowNum) throws IOException {

    String name = row[NAME];
    String title = row[TITLE];

    if (title == null) {
      title = row[TITLE_FR];
    }

    if (name == null && title == null) {
      validatorService.addLog(I18n.get("Name and title is empty"), key, rowNum);
    }

    String model = row[OBJECT];
    if (model != null && !validatorService.isValidModel(model)) {
      validatorService.addLog(I18n.get("Invalid model"), key, rowNum);
    }

    String order = row[ORDER];
    if (order != null) {
      try {
        Integer.parseInt(order.trim());
      } catch (Exception e) {
        validatorService.addLog(I18n.get("Invalid menu order"), key, rowNum);
      }
    }

    menus.add(name);
  }
}
