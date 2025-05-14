/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.message.db.Message;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import java.io.IOException;

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
  public Message sendCancellationEmail(LeaveRequest leaveRequest) throws AxelorException {

    HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());

    try {
      if (hrConfig.getLeaveMailNotification()) {
        return templateMessageService.generateAndSendMessage(
            leaveRequest, hrConfigService.getCanceledLeaveTemplate(hrConfig));
      }
    } catch (ClassNotFoundException | IOException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }

    return null;
  }

  @Override
  public Message sendConfirmationEmail(LeaveRequest leaveRequest) throws AxelorException {

    HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());

    try {
      if (hrConfig.getLeaveMailNotification()) {
        return templateMessageService.generateAndSendMessage(
            leaveRequest, hrConfigService.getSentLeaveTemplate(hrConfig));
      }
    } catch (ClassNotFoundException | IOException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }

    return null;
  }

  @Override
  public Message sendValidationEmail(LeaveRequest leaveRequest) throws AxelorException {

    HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());

    try {
      if (hrConfig.getLeaveMailNotification()) {
        return templateMessageService.generateAndSendMessage(
            leaveRequest, hrConfigService.getValidatedLeaveTemplate(hrConfig));
      }
    } catch (ClassNotFoundException | IOException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }

    return null;
  }

  @Override
  public Message sendRefusalEmail(LeaveRequest leaveRequest) throws AxelorException {

    HRConfig hrConfig = hrConfigService.getHRConfig(leaveRequest.getCompany());

    try {
      if (hrConfig.getLeaveMailNotification()) {
        return templateMessageService.generateAndSendMessage(
            leaveRequest, hrConfigService.getRefusedLeaveTemplate(hrConfig));
      }
    } catch (ClassNotFoundException | IOException e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }

    return null;
  }
}
