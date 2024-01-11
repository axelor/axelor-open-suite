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
package com.axelor.apps.hr.service.employee;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.hr.db.DPAE;
import com.axelor.apps.hr.db.Employee;
import com.axelor.auth.db.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface EmployeeService extends UserService {

  public int getLengthOfService(Employee employee, LocalDate refDate) throws AxelorException;

  public int getAge(Employee employee, LocalDate refDate) throws AxelorException;

  public BigDecimal getDaysWorksInPeriod(Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException;

  public Map<String, String> getSocialNetworkUrl(String name, String firstName);

  /** Generates a new {@link DPAE} for given {@link Employee} and returns its id. */
  Long generateNewDPAE(Employee employee) throws AxelorException;

  public User getUser(Employee employee) throws AxelorException;
}
