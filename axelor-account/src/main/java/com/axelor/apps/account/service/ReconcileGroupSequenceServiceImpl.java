/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class ReconcileGroupSequenceServiceImpl implements ReconcileGroupSequenceService {

  @Override
  public void fillCodeFromSequence(ReconcileGroup reconcileGroup) throws AxelorException {
    String sequenceCode;
    String exceptionMessage;
    if (reconcileGroup.getStatusSelect() == ReconcileGroupRepository.STATUS_FINAL) {
      sequenceCode = SequenceRepository.RECONCILE_GROUP_FINAL;
      exceptionMessage = IExceptionMessage.RECONCILE_GROUP_NO_FINAL_SEQUENCE;
    } else {
      sequenceCode = SequenceRepository.RECONCILE_GROUP_DRAFT;
      exceptionMessage = IExceptionMessage.RECONCILE_GROUP_NO_TEMP_SEQUENCE;
    }
    String code =
        Beans.get(SequenceService.class)
            .getSequenceNumber(sequenceCode, reconcileGroup.getCompany());

    if (code == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(exceptionMessage),
          reconcileGroup);
    }

    reconcileGroup.setCode(code);
  }
}
