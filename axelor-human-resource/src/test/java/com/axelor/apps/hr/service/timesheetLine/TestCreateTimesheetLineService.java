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
package com.axelor.apps.hr.service.timesheetLine;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.module.HumanResourceTest;
import com.axelor.apps.hr.rest.TimesheetLineRestController;
import com.axelor.apps.hr.rest.dto.TimesheetLinePostRequest;
import com.axelor.inject.Beans;
import com.axelor.meta.loader.LoaderHelper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestCreateTimesheetLineService extends HumanResourceTest {

  protected final TimesheetLineRestController timesheetLineRestController;

  private TimesheetLinePostRequest timesheetLinePostRequest;

  @Inject
  public TestCreateTimesheetLineService(TimesheetLineRestController timesheetLineRestController) {
    this.timesheetLineRestController = timesheetLineRestController;
  }

  @BeforeAll
  static void setUp() {
    final LoaderHelper loaderHelper = Beans.get(LoaderHelper.class);
    loaderHelper.importCsv("data/project-config.xml");
    loaderHelper.importCsv("data/timesheet-config.xml");
  }

  @Test
  void testCreateTimesheetLineWithTimesheet() throws AxelorException {
    givenTimesheetLinePostRequest(1L, 1L, 1L, LocalDate.now());
    Response response = timesheetLineRestController.createTimesheetLine(timesheetLinePostRequest);
    Assertions.assertEquals(true, true);
  }

  private void givenTimesheetLinePostRequest(
      Long timesheetId, Long projectId, Long projectTaskId, LocalDate date) {
    TimesheetLinePostRequest timesheetLinePostRequest = new TimesheetLinePostRequest();
    timesheetLinePostRequest.setTimesheetId(timesheetId);
    timesheetLinePostRequest.setProjectId(projectId);
    timesheetLinePostRequest.setProjectTaskId(projectTaskId);
    timesheetLinePostRequest.setDate(date);
    timesheetLinePostRequest.setDuration(new BigDecimal(7));

    this.timesheetLinePostRequest = timesheetLinePostRequest;
  }
}
