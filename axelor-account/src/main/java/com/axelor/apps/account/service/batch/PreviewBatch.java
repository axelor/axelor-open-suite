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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.service.debtrecovery.DoubtfulCustomerService;
import com.google.inject.Inject;
import java.util.List;

public abstract class PreviewBatch extends BatchStrategy {
  protected List<Long> recordIdList;

  protected PreviewBatch() {}

  @Inject
  protected PreviewBatch(
      DoubtfulCustomerService doubtfulCustomerService, BatchAccountCustomer batchAccountCustomer) {
    super(doubtfulCustomerService, batchAccountCustomer);
  }

  public void setRecordIdList(List<Long> recordIdList) {
    this.recordIdList = recordIdList;
  }

  @Override
  protected void process() {
    AccountingBatch accountingBatch = batch.getAccountingBatch();

    if (this.recordIdList == null) {
      this._processByQuery(accountingBatch);
    } else {
      this._processByIds(accountingBatch);
    }
  }

  protected abstract void _processByQuery(AccountingBatch accountingBatch);

  protected abstract void _processByIds(AccountingBatch accountingBatch);
}
