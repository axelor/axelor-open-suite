package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.message.db.Message;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import java.io.IOException;
import wslite.json.JSONException;

public class LeaveRequestMailServiceImpl implements LeaveRequestMailService {

  protected HRConfigService hrConfigService;
  protected TemplateMessageService templateMessageService;

  @Inject
  public LeaveRequestMailServiceImpl(
      HRConfigService hrConfigService, TemplateMessageService templateMessageService) {
    this.hrConfigService = hrConfigService;
    this.templateMessageService = templateMessageService;
  }

  @Override
  public Message sendCancellationEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());

    if (hrConfig.getLeaveMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          leaveRequest, hrConfigService.getCanceledLeaveTemplate(hrConfig));
    }

    return null;
  }

  @Override
  public Message sendConfirmationEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());

    if (hrConfig.getLeaveMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          leaveRequest, hrConfigService.getSentLeaveTemplate(hrConfig));
    }

    return null;
  }

  @Override
  public Message sendValidationEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());

    if (hrConfig.getLeaveMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          leaveRequest, hrConfigService.getValidatedLeaveTemplate(hrConfig));
    }

    return null;
  }

  @Override
  public Message sendRefusalEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());

    if (hrConfig.getLeaveMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          leaveRequest, hrConfigService.getRefusedLeaveTemplate(hrConfig));
    }

    return null;
  }
}
