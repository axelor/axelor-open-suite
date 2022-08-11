/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class SubrogationReleaseWorkflowServiceImpl implements SubrogationReleaseWorkflowService {
  @Transactional(rollbackOn = {AxelorException.class})
  @Override
  public void goBackToAccounted(SubrogationRelease subrogationRelease) throws AxelorException {
    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(SubrogationReleaseRepository.STATUS_CLEARED);
    authorizedStatus.add(SubrogationReleaseRepository.STATUS_CANCELED);
    if (subrogationRelease.getStatusSelect() == null
        || !authorizedStatus.contains(subrogationRelease.getStatusSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.SUBROGATION_RELEASE_BACK_TO_ACCOUNTED_WRONG_STATUS));
    }
    subrogationRelease.setStatusSelect(SubrogationReleaseRepository.STATUS_ACCOUNTED);
  }
}
