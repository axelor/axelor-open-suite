/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.test;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.service.timesheet.TimesheetComputeNameServiceImpl;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestTimesheetComputeNameService {
  protected TimesheetComputeNameServiceImpl timesheetComputeNameService;

  @Before
  public void prepare() {
    timesheetComputeNameService = new TimesheetComputeNameServiceImpl();
  }

  @Test
  public void testComputeEmptyFullName() {
    Timesheet emptyTimesheet = new Timesheet();

    String result = timesheetComputeNameService.computeTimesheetFullname(emptyTimesheet);

    Assert.assertEquals("", result);
  }

  @Test
  public void testComputeFullnameMinimal() {
    Timesheet timesheet1 = new Timesheet();

    Partner contactPartner1 = new Partner();
    contactPartner1.setFullName("P0048 - Axelor");

    Employee employee1 = new Employee();
    employee1.setContactPartner(contactPartner1);

    timesheet1.setEmployee(employee1);

    String result = timesheetComputeNameService.computeTimesheetFullname(timesheet1);

    Assert.assertEquals("P0048 - Axelor", result);
  }

  @Test
  public void testComputeFullnameWithFromDate() {
    Timesheet timesheet2 = new Timesheet();

    Partner contactPartner2 = new Partner();
    contactPartner2.setFullName("P0048 - Axelor");

    Employee employee2 = new Employee();
    employee2.setContactPartner(contactPartner2);

    timesheet2.setEmployee(employee2);
    timesheet2.setFromDate(LocalDate.of(2023, 01, 10));

    String result = timesheetComputeNameService.computeTimesheetFullname(timesheet2);

    Assert.assertEquals("P0048 - Axelor 10/01/2023", result);
  }

  @Test
  public void testComputeFullnameWithFromDateAndToDate() {
    Timesheet timesheet3 = new Timesheet();

    Partner contactPartner3 = new Partner();
    contactPartner3.setFullName("P0048 - Axelor");

    Employee employee3 = new Employee();
    employee3.setContactPartner(contactPartner3);

    timesheet3.setEmployee(employee3);
    timesheet3.setFromDate(LocalDate.of(2023, 01, 10));
    timesheet3.setToDate(LocalDate.of(2023, 01, 12));

    String result = timesheetComputeNameService.computeTimesheetFullname(timesheet3);

    Assert.assertEquals("P0048 - Axelor 10/01/2023-12/01/2023", result);
  }
}
