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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.bankpayment.service.batch.AccountingBatchBankPaymentService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.supplychain.service.batch.BatchAccountingCutOffSupplyChain;
import com.axelor.inject.Beans;

public class AccountingBatchSupplyChainService extends AccountingBatchBankPaymentService {

  @Override
  public Batch accountingCutOff(AccountingBatch accountingBatch) {
    return Beans.get(BatchAccountingCutOffSupplyChain.class).run(accountingBatch);
  }
}
