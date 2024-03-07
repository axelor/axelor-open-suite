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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.message.db.Message;
import java.io.IOException;
import wslite.json.JSONException;

public interface TimesheetWorkflowService {

  void confirm(Timesheet timesheet) throws AxelorException;

  Message sendConfirmationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  Message confirmAndSendConfirmationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  void validate(Timesheet timesheet) throws AxelorException;

  Message sendValidationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  Message validateAndSendValidationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  void refuse(Timesheet timesheet) throws AxelorException;

  void refuseAndSendRefusalEmail(Timesheet timesheet, String groundForRefusal)
      throws AxelorException, JSONException, IOException, ClassNotFoundException;

  Message sendRefusalEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  Message refuseAndSendRefusalEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  void cancel(Timesheet timesheet) throws AxelorException;

  void draft(Timesheet timesheet);

  Message sendCancellationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  Message cancelAndSendCancellationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  Message complete(Timesheet timesheet)
      throws AxelorException, JSONException, IOException, ClassNotFoundException;

  void completeOrConfirm(Timesheet timesheet)
      throws AxelorException, JSONException, IOException, ClassNotFoundException;
}
