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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.DebtRecovery;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.DebtRecoveryRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.batch.AccountingBatchService;
import com.axelor.apps.account.service.debtrecovery.DebtRecoveryActionService;
import com.axelor.apps.account.service.debtrecovery.DebtRecoveryService;
import com.axelor.apps.base.db.Batch;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DebtRecoveryController {

  @Inject DebtRecoveryService debtRecoveryService;

  public void runDebtRecovery(ActionRequest request, ActionResponse response) {
    DebtRecovery debtRecovery = request.getContext().asType(DebtRecovery.class);
    debtRecovery = Beans.get(DebtRecoveryRepository.class).find(debtRecovery.getId());
    try {
      if (debtRecoveryService.getAccountingSituation(debtRecovery) == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(IExceptionMessage.DEBT_RECOVERY_1));
      }
      debtRecovery.setDebtRecoveryMethodLine(debtRecovery.getWaitDebtRecoveryMethodLine());
      Beans.get(DebtRecoveryActionService.class).runManualAction(debtRecovery);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void blockCustomersWithLatePayments(ActionRequest request, ActionResponse response) {
    try {
      AccountingBatch accountingBatch =
          Beans.get(AccountingBatchRepository.class)
              .all()
              .filter("self.actionSelect = :select")
              .bind("select", AccountingBatchRepository.ACTION_LATE_PAYMENT_CUSTOMER_BLOCKING)
              .fetchOne();
      if (accountingBatch != null) {
        Batch batch =
            Beans.get(AccountingBatchService.class).blockCustomersWithLatePayments(accountingBatch);
        response.setFlash(batch.getComments());
      } else {
        response.setError(
            I18n.get(IExceptionMessage.BATCH_BLOCK_CUSTOMER_WITH_LATE_PAYMENT_MISSING));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
