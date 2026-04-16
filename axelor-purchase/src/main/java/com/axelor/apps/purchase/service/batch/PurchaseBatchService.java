/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.apps.purchase.db.PurchaseBatch;
import com.axelor.apps.purchase.db.repo.PurchaseBatchRepository;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class PurchaseBatchService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return PurchaseBatch.class;
  }

  @Override
  public Batch run(Model batchModel) throws AxelorException {
    PurchaseBatch purchaseBatch = (PurchaseBatch) batchModel;

    switch (purchaseBatch.getActionSelect()) {
      case PurchaseBatchRepository.ACTION_SUPPLIER_REMINDER:
        return supplierReminder(purchaseBatch);
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.BASE_BATCH_1),
            purchaseBatch.getActionSelect(),
            purchaseBatch.getCode());
    }
  }

  public Batch supplierReminder(PurchaseBatch purchaseBatch) {
    return Beans.get(BatchSupplierReminder.class).run(purchaseBatch);
  }
}
