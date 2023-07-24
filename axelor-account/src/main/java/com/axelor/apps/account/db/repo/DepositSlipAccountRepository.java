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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import javax.persistence.PersistenceException;

public class DepositSlipAccountRepository extends DepositSlipRepository {

  @Override
  public DepositSlip save(DepositSlip entity) {
    try {

      if (Strings.isNullOrEmpty(entity.getDepositNumber())) {
        setDepositNumber(entity);
      }

      return super.save(entity);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  protected void setDepositNumber(DepositSlip entity) throws AxelorException {
    SequenceService sequenceService = Beans.get(SequenceService.class);
    String depositNumber =
        sequenceService.getSequenceNumber(
            SequenceRepository.DEPOSIT_SLIP,
            entity.getCompany(),
            DepositSlip.class,
            "depositNumber");

    if (Strings.isNullOrEmpty(depositNumber)) {
      throw new AxelorException(
          Sequence.class,
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AccountExceptionMessage.DEPOSIT_SLIP_MISSING_SEQUENCE),
          entity.getCompany().getName());
    }

    entity.setDepositNumber(depositNumber);
  }

  @Override
  public void remove(DepositSlip entity) {
    if (entity.getPublicationDate() != null) {
      throw new PersistenceException(I18n.get(AccountExceptionMessage.DEPOSIT_SLIP_CANNOT_DELETE));
    }

    entity.clearPaymentVoucherList();
    super.remove(entity);
  }
}
