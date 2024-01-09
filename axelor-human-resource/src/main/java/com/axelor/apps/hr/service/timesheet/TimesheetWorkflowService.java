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

  void validate(Timesheet timesheet);

  Message sendValidationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  Message validateAndSendValidationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  void refuse(Timesheet timesheet);

  Message sendRefusalEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  Message refuseAndSendRefusalEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  void cancel(Timesheet timesheet);

  void draft(Timesheet timesheet);

  Message sendCancellationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  Message cancelAndSendCancellationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;
}
