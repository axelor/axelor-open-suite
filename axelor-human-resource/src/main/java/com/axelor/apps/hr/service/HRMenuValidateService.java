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
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.auth.db.User;
import com.axelor.meta.schema.actions.ActionView;

public class HRMenuValidateService {

  public void createValidateDomain(
      User user, Employee employee, ActionView.ActionViewBuilder actionView) {

    actionView
        .domain("self.statusSelect = :_statusSelect")
        .context("_statusSelect", ExpenseRepository.STATUS_CONFIRMED);

    if (employee == null || !employee.getHrManager()) {
      if (employee == null || employee.getManagerUser() == null) {
        actionView
            .domain(
                actionView.get().getDomain()
                    + " AND (self.employee.user.id = :_user_id OR self.employee.managerUser = :_user)")
            .context("_user_id", user.getId())
            .context("_user", user);
      } else {
        actionView
            .domain(actionView.get().getDomain() + " AND self.employee.managerUser = :_user")
            .context("_user", user);
      }
    }
  }
}
