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
package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationCreateService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import javax.validation.ValidationException;

public class BankReconciliationManagementRepository extends BankReconciliationRepository {
  @Override
  public BankReconciliation copy(BankReconciliation entity, boolean deep) {
    entity.setStatusSelect(STATUS_DRAFT);
    entity.setStartingBalance(BigDecimal.ZERO);
    entity.setEndingBalance(BigDecimal.ZERO);
    entity.setComputedBalance(BigDecimal.ZERO);
    entity.setAccountBalance(BigDecimal.ZERO);
    entity.setTotalCashed(BigDecimal.ZERO);
    entity.setTotalPaid(BigDecimal.ZERO);
    entity.setBankReconciliationLineList(null);

    return super.copy(entity, deep);
  }

  @Override
  public BankReconciliation save(BankReconciliation entity) {

    if (Strings.isNullOrEmpty(entity.getName())) {
      entity.setName(Beans.get(BankReconciliationCreateService.class).computeName(entity));
    }

    return super.save(entity);
  }

  @Override
  public void remove(BankReconciliation entity) {
    if (entity.getStatusSelect().equals(BankReconciliationRepository.STATUS_VALIDATED)) {
      throw new ValidationException(
          I18n.get(BankPaymentExceptionMessage.BANK_RECONCILIATION_CANNOT_DELETE_VALIDATED));
    } else if (entity
        .getStatusSelect()
        .equals(BankReconciliationRepository.STATUS_UNDER_CORRECTION)) {
      throw new ValidationException(
          I18n.get(BankPaymentExceptionMessage.BANK_RECONCILIATION_CANNOT_DELETE_UNDER_CORRECTION));
    } else {
      BankReconciliationService bankReconciliationService =
          Beans.get(BankReconciliationService.class);
      bankReconciliationService.unreconcileLines(entity.getBankReconciliationLineList());
      super.remove(entity);
    }
  }
}
