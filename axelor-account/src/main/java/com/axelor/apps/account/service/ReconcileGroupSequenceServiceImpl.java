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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class ReconcileGroupSequenceServiceImpl implements ReconcileGroupSequenceService {

  @Override
  public void fillCodeFromSequence(ReconcileGroup reconcileGroup) throws AxelorException {
    String sequenceCode;
    String exceptionMessage;
    if (reconcileGroup.getStatusSelect() == ReconcileGroupRepository.STATUS_FINAL) {
      sequenceCode = SequenceRepository.RECONCILE_GROUP_FINAL;
      exceptionMessage = AccountExceptionMessage.RECONCILE_GROUP_NO_FINAL_SEQUENCE;
    } else {
      sequenceCode = SequenceRepository.RECONCILE_GROUP_DRAFT;
      exceptionMessage = AccountExceptionMessage.RECONCILE_GROUP_NO_TEMP_SEQUENCE;
    }
    String code =
        Beans.get(SequenceService.class)
            .getSequenceNumber(
                sequenceCode, reconcileGroup.getCompany(), ReconcileGroup.class, "code");

    if (code == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(exceptionMessage),
          reconcileGroup);
    }

    reconcileGroup.setCode(code);
  }
}
