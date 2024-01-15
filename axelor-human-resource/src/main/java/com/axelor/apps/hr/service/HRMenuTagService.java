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
package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.Employee;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.Model;

public class HRMenuTagService {

  /**
   * @param modelConcerned
   * @param status 1 : Draft 2 : Confirmed 3 : Validated 4 : Refused 5 : Canceled
   * @return The number of records
   */
  public <T extends Model> String countRecordsTag(Class<T> modelConcerned, int status) {

    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();
    String filter = "self.statusSelect = :_statusSelect";

    if (employee != null && employee.getHrManager()) {

      return Long.toString(
          JPA.all(modelConcerned).filter(filter).bind("_statusSelect", status).count());

    } else {

      filter +=
          (employee == null || employee.getManagerUser() == null)
              ? " AND (self.employee.user.id = :_userId OR self.employee.managerUser.id = :_userId)"
              : " AND self.employee.managerUser.id = :_userId";

      return Long.toString(
          JPA.all(modelConcerned)
              .filter(filter)
              .bind("_userId", user.getId())
              .bind("_statusSelect", status)
              .count());
    }
  }
}
