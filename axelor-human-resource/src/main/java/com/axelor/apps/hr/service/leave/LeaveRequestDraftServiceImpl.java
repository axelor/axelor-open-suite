/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;

public class LeaveRequestDraftServiceImpl implements LeaveRequestDraftService {

  protected LeaveRequestRepository leaveRequestRepository;

  @Inject
  public LeaveRequestDraftServiceImpl(LeaveRequestRepository leaveRequestRepository) {
    this.leaveRequestRepository = leaveRequestRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void draft(LeaveRequest leaveRequest) throws AxelorException {
    int statusSelect = leaveRequest.getStatusSelect();
    if (statusSelect != LeaveRequestRepository.STATUS_REFUSED
        && statusSelect != LeaveRequestRepository.STATUS_CANCELED) {
      throw new AxelorException(
          leaveRequest,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_DRAFT_WRONG_STATUS));
    }

    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_DRAFT);
    leaveRequestRepository.save(leaveRequest);
  }
}
