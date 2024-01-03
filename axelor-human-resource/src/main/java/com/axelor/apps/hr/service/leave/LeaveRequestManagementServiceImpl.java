/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LeaveRequestManagementServiceImpl implements LeaveRequestManagementService {
  protected LeaveLineService leaveLineService;

  @Inject
  public LeaveRequestManagementServiceImpl(LeaveLineService leaveLineService) {
    this.leaveLineService = leaveLineService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void manageSentLeaves(LeaveRequest leave) throws AxelorException {
    Employee employee = leave.getEmployee();

    LeaveLine leaveLine = leaveLineService.getLeaveLine(leave);

    if (leaveLine == null) {
      throw new AxelorException(
          leave,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.LEAVE_LINE),
          employee.getName(),
          leave.getLeaveReason().getName());
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void manageValidateLeaves(LeaveRequest leave) throws AxelorException {
    Employee employee = leave.getEmployee();

    LeaveLine leaveLine = leaveLineService.getLeaveLine(leave);

    if (leaveLine == null) {
      throw new AxelorException(
          leave,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.LEAVE_LINE),
          employee.getName(),
          leave.getLeaveReason().getName());
    }

    if (leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME) {
      leaveLine.setQuantity(leaveLine.getQuantity().subtract(leave.getDuration()));
      if (leaveLine.getQuantity().signum() == -1 && !employee.getNegativeValueLeave()) {
        throw new AxelorException(
            leave,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(HumanResourceExceptionMessage.LEAVE_ALLOW_NEGATIVE_VALUE_EMPLOYEE),
            employee.getName());
      }
      if (leaveLine.getQuantity().signum() == -1
          && !leave.getLeaveReason().getAllowNegativeValue()) {
        throw new AxelorException(
            leave,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(HumanResourceExceptionMessage.LEAVE_ALLOW_NEGATIVE_VALUE_REASON),
            leave.getLeaveReason().getName());
      }
      leaveLine.setDaysValidated(leaveLine.getDaysValidated().add(leave.getDuration()));

    } else {
      leaveLine.setQuantity(leaveLine.getQuantity().add(leave.getDuration()));
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void manageRefuseLeaves(LeaveRequest leave) throws AxelorException {
    Employee employee = leave.getEmployee();

    LeaveLine leaveLine = leaveLineService.getLeaveLine(leave);

    if (leaveLine == null) {
      throw new AxelorException(
          leave,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.LEAVE_LINE),
          employee.getName(),
          leave.getLeaveReason().getName());
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void manageCancelLeaves(LeaveRequest leave) throws AxelorException {
    Employee employee = leave.getEmployee();

    LeaveLine leaveLine = leaveLineService.getLeaveLine(leave);

    if (leaveLine == null) {
      throw new AxelorException(
          leave,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.LEAVE_LINE),
          employee.getName(),
          leave.getLeaveReason().getName());
    }

    if (leave.getStatusSelect() == LeaveRequestRepository.STATUS_VALIDATED) {
      if (leave.getInjectConsumeSelect() == LeaveRequestRepository.SELECT_CONSUME) {
        leaveLine.setQuantity(leaveLine.getQuantity().add(leave.getDuration()));
      } else {
        leaveLine.setQuantity(leaveLine.getQuantity().subtract(leave.getDuration()));
      }
      leaveLine.setDaysValidated(leaveLine.getDaysValidated().subtract(leave.getDuration()));
    }
  }
}
