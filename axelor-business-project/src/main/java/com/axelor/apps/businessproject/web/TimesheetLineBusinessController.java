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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.service.TimesheetLineBusinessService;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.StringTool;
import java.util.List;

public class TimesheetLineBusinessController {

  public void setDefaultToInvoice(ActionRequest request, ActionResponse response) {
    try {
      TimesheetLine timesheetLine = request.getContext().asType(TimesheetLine.class);
      timesheetLine =
          Beans.get(TimesheetLineBusinessService.class).getDefaultToInvoice(timesheetLine);
      response.setValue("toInvoice", timesheetLine.getToInvoice());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setTimesheet(ActionRequest request, ActionResponse response) {
    try {
      TimesheetLine timesheetLine = request.getContext().asType(TimesheetLine.class);
      timesheetLine = Beans.get(TimesheetLineBusinessService.class).setTimesheet(timesheetLine);
      response.setValues(timesheetLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setTimesheetDomain(ActionRequest request, ActionResponse response) {
    try {
      TimesheetLine timesheetLine = request.getContext().asType(TimesheetLine.class);
      List<Timesheet> timesheetList =
          Beans.get(TimesheetLineBusinessService.class).getTimesheetQuery(timesheetLine).fetch();
      String idList = StringTool.getIdListString(timesheetList);
      response.setAttr("timesheet", "domain", "self.id IN (" + idList + ")");
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
