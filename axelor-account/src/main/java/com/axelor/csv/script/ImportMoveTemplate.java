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
package com.axelor.csv.script;

import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import java.io.IOException;
import java.util.Map;

public class ImportMoveTemplate {
  public Object importMove(Object bean, Map<String, Object> values) throws IOException {
    assert bean instanceof MoveTemplate;
    MoveTemplate moveTemplate = (MoveTemplate) bean;

    try {
      if (moveTemplate.getJournal() != null) {
        moveTemplate.setCompany(moveTemplate.getJournal().getCompany());
        String dateShift = values.get("endOfValidityDateShift").toString();
        if (StringUtils.notBlank(dateShift)) {
          moveTemplate.setEndOfValidityDate(
              Beans.get(AppBaseService.class)
                  .getTodayDate(moveTemplate.getCompany())
                  .plusMonths(Integer.parseInt(dateShift)));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return moveTemplate;
  }
}
