/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.extra.hours;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.auth.db.User;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;

public interface ExtraHoursViewService {

  /**
   * Build view for editing extra hours in draft status for the current user.
   *
   * @param user The current user
   * @param company The active company
   * @return ActionViewBuilder for the appropriate view (empty form, edit form, or popup wizard)
   */
  ActionViewBuilder buildEditExtraHoursView(User user, Company company);

  /**
   * Build view for editing a specific selected extra hours record.
   *
   * @param extraHoursId The ID of the extra hours record to edit
   * @return ActionViewBuilder for the edit form view
   */
  ActionViewBuilder buildEditSelectedExtraHoursView(Long extraHoursId);

  /**
   * Build view for subordinate extra hours that need validation.
   *
   * @param user The current user (manager)
   * @param company The active company
   * @return ActionViewBuilder for the grid view
   * @throws AxelorException if company is null or no subordinate extra hours are found
   */
  ActionViewBuilder buildSubordinateExtraHoursView(User user, Company company)
      throws AxelorException;

  /**
   * Build view for historic colleague extra hours (validated or refused).
   *
   * @param user The current user
   * @param employee The current employee
   * @param company The active company
   * @return ActionViewBuilder for the grid view
   */
  ActionViewBuilder buildHistoricExtraHoursView(User user, Employee employee, Company company);
}
