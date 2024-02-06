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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.production.db.ManufacturingOperation;

public interface ManufacturingOperationBusinessProductionCheckService {

  /**
   * This method checks if users currently working on the operation order are associated to a
   * employee.
   *
   * @param manufacturingOperation
   * @return true if it the case, else false
   */
  boolean workingUsersHaveEmployee(ManufacturingOperation manufacturingOperation);

  /**
   * This method check if users currently working on the operaton order have timesheet imputation
   * select set to tsImputationSelect.
   *
   * <p>Please note that if users do not have employee associated, the method will return true
   *
   * @param manufacturingOperation
   * @param tsImputationSelect
   * @return true if it the case, else false
   */
  boolean workingUsersHaveTSImputationSelect(
      ManufacturingOperation manufacturingOperation, int tsImputationSelect);

  /**
   * This method check if users currently working on the operaton order have their time logging
   * preference not set on DAYS.
   *
   * <p>Please note that if users do not have employee associated, the method will return true
   *
   * @param manufacturingOperation
   * @return true if it the case, else false
   */
  boolean workingUsersHaveCorrectTimeLoggingPref(ManufacturingOperation manufacturingOperation);

  /**
   * This method check if users currently working on the operaton order have their employee time
   * logging preference matching with their current timesheet.
   *
   * <p>Please note that if users do not have employee associated, the method will return true
   *
   * @param manufacturingOperation
   * @return true if it the case, else false
   */
  boolean workingUsersHaveTSTimeLoggingPrefMatching(ManufacturingOperation manufacturingOperation);
}
