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
package com.axelor.apps.hr.service.timesheet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestTimesheetComputeNameService {
  private static TimesheetComputeNameServiceImpl timesheetComputeNameService;
  private static DateService dateService;

  @BeforeAll
  static void prepare() throws AxelorException {
    dateService = mock(DateService.class);
    when(dateService.getDateFormat()).thenReturn(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    timesheetComputeNameService = new TimesheetComputeNameServiceImpl(dateService);
  }

  @Test
  void testComputeEmptyFullName() {
    Timesheet emptyTimesheet = new Timesheet();

    String result = timesheetComputeNameService.computeTimesheetFullname(emptyTimesheet);

    Assertions.assertEquals("", result);
  }

  @Test
  public void testComputeFullnameMinimal() {
    Partner contactPartner = createPartner("P0048 - Axelor");
    Employee employee = createEmployee(contactPartner);
    Timesheet timesheet1 = createTimeSheet(employee, null, null);

    String result = timesheetComputeNameService.computeTimesheetFullname(timesheet1);

    Assertions.assertEquals("P0048 - Axelor", result);
  }

  @Test
  void testComputeFullnameWithFromDate() {
    Partner contactPartner = createPartner("P0048 - Axelor");
    Employee employee = createEmployee(contactPartner);
    Timesheet timesheet = createTimeSheet(employee, LocalDate.of(2023, 01, 10), null);

    String result = timesheetComputeNameService.computeTimesheetFullname(timesheet);

    Assertions.assertEquals("P0048 - Axelor 10/01/2023", result);
  }

  @Test
  void testComputeFullnameWithFromDateAndToDate() {
    Partner contactPartner = createPartner("P0048 - Axelor");
    Employee employee3 = createEmployee(contactPartner);
    Timesheet timesheet =
        createTimeSheet(employee3, LocalDate.of(2023, 01, 10), LocalDate.of(2023, 01, 12));

    String result = timesheetComputeNameService.computeTimesheetFullname(timesheet);

    Assertions.assertEquals("P0048 - Axelor 10/01/2023-12/01/2023", result);
  }

  protected Partner createPartner(String fullName) {
    Partner contactPartner = new Partner();
    contactPartner.setFullName(fullName);
    return contactPartner;
  }

  protected Employee createEmployee(Partner partner) {
    Employee employee = new Employee();
    employee.setContactPartner(partner);
    return employee;
  }

  protected Timesheet createTimeSheet(Employee employee, LocalDate fromDate, LocalDate toDate) {
    Timesheet timesheet = new Timesheet();
    if (employee != null) {
      timesheet.setEmployee(employee);
    }
    if (fromDate != null) {
      timesheet.setFromDate(fromDate);
    }
    if (toDate != null) {
      timesheet.setToDate(toDate);
    }
    return timesheet;
  }
}
