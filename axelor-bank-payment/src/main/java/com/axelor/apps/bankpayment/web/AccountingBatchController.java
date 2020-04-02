/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.bankpayment.service.batch.AccountingBatchBankPaymentService;
import com.axelor.apps.base.db.Batch;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AccountingBatchController {

  protected final AccountingBatchBankPaymentService accountingBatchService;
  protected final AccountingBatchRepository accountingBatchRepo;

  @Inject
  public AccountingBatchController(
      AccountingBatchBankPaymentService accountingBatchService,
      AccountingBatchRepository accountingBatchRepo) {
    this.accountingBatchService = accountingBatchService;
    this.accountingBatchRepo = accountingBatchRepo;
  }

  public void actionBankStatement(ActionRequest request, ActionResponse response) {
    try {
      AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
      accountingBatch = accountingBatchRepo.find(accountingBatch.getId());
      Batch batch = accountingBatchService.bankStatement(accountingBatch);
      response.setFlash(batch.getComments());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }
}
