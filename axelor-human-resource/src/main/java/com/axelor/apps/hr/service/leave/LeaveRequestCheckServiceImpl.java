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
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LeaveRequestCheckServiceImpl implements LeaveRequestCheckService {

  @Override
  public void checkCompany(LeaveRequest leaveRequest) throws AxelorException {

    if (ObjectUtils.isEmpty(leaveRequest.getCompany())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_NO_COMPANY));
    }
  }

  @Override
  public void checkDates(LeaveRequest leaveRequest) throws AxelorException {
    if (isDatesInvalid(leaveRequest)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.INVALID_DATES));
    }

    if (isDurationInvalid(leaveRequest)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_WRONG_DURATION));
    }
  }

  @Override
  public boolean isDatesInvalid(LeaveRequest leaveRequest) {
    LocalDateTime fromDateTime = leaveRequest.getFromDateT();
    LocalDateTime toDateTime = leaveRequest.getToDateT();
    int startOnSelect = leaveRequest.getStartOnSelect();
    int endOnSelect = leaveRequest.getEndOnSelect();
    return toDateTime.isBefore(fromDateTime)
        || (toDateTime.isEqual(fromDateTime) && startOnSelect > endOnSelect);
  }

  @Override
  public boolean isDurationInvalid(LeaveRequest leaveRequest) {
    BigDecimal duration = leaveRequest.getDuration();
    return duration.signum() == 0;
  }
}
