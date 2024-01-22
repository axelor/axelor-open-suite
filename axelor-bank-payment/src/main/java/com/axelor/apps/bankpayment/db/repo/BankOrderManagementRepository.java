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

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class BankOrderManagementRepository extends BankOrderRepository {

  @Override
  public BankOrder save(BankOrder entity) {

    try {

      BankOrderService bankOrderService = Beans.get(BankOrderService.class);
      bankOrderService.generateSequence(entity);
      bankOrderService.setSequenceOnBankOrderLines(entity);
      if (entity.getStatusSelect() == BankOrderRepository.STATUS_DRAFT) {
        bankOrderService.updateTotalAmounts(entity);
      }

      return super.save(entity);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public BankOrder copy(BankOrder entity, boolean deep) {

    BankOrder copy = super.copy(entity, deep);

    copy.setStatusSelect(STATUS_DRAFT);
    copy.setGeneratedMetaFile(null);
    copy.setSignedMetaFile(null);
    copy.setConfirmationDateTime(null);
    copy.setFileGenerationDateTime(null);
    copy.setValidationDateTime(null);
    copy.setSendingDateTime(null);
    copy.setBankOrderSeq(null);

    if (CollectionUtils.isNotEmpty(copy.getBankOrderLineList())) {
      for (BankOrderLine bankOrderLine : copy.getBankOrderLineList()) {
        bankOrderLine.setSenderMove(null);
        bankOrderLine.setReceiverMove(null);
        bankOrderLine.setRejectMove(null);
      }
    }
    copy.setAreMovesGenerated(false);
    copy.setHasBeenSentToBank(false);
    return copy;
  }

  @Override
  public void remove(BankOrder entity) {
    if (entity.getStatusSelect() == BankOrderRepository.STATUS_DRAFT
        || entity.getStatusSelect() == BankOrderRepository.STATUS_CANCELED) {
      super.remove(entity);
      return;
    }
    throw new PersistenceException(I18n.get(BankPaymentExceptionMessage.BANK_ORDER_CANNOT_REMOVE));
  }
}
