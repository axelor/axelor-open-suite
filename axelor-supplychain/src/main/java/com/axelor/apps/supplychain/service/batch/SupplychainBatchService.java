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
package com.axelor.apps.supplychain.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class SupplychainBatchService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return SupplychainBatch.class;
  }

  @Override
  public Batch run(Model batchModel) throws AxelorException {

    Batch batch;
    SupplychainBatch supplychainBatch = (SupplychainBatch) batchModel;

    switch (supplychainBatch.getActionSelect()) {
      case SupplychainBatchRepository.ACTION_INVOICE_OUTGOING_STOCK_MOVES:
        batch = invoiceOutgoingStockMoves(supplychainBatch);
        break;
      case SupplychainBatchRepository.ACTION_INVOICE_ORDERS:
        batch = invoiceOrders(supplychainBatch);
        break;
      case SupplychainBatchRepository.ACTION_UPDATE_STOCK_HISTORY:
        batch = updateStockHistory(supplychainBatch);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.BASE_BATCH_1),
            supplychainBatch.getActionSelect(),
            supplychainBatch.getCode());
    }

    return batch;
  }

  public Batch invoiceOutgoingStockMoves(SupplychainBatch supplychainBatch) {
    return Beans.get(BatchOutgoingStockMoveInvoicing.class).run(supplychainBatch);
  }

  public Batch invoiceOrders(SupplychainBatch supplychainBatch) {
    switch (supplychainBatch.getInvoiceOrdersTypeSelect()) {
      case SupplychainBatchRepository.INVOICE_ORDERS_TYPE_SALE:
        return Beans.get(BatchOrderInvoicingSale.class).run(supplychainBatch);
      case SupplychainBatchRepository.INVOICE_ORDERS_TYPE_PURCHASE:
        return Beans.get(BatchOrderInvoicingPurchase.class).run(supplychainBatch);
      default:
        throw new IllegalArgumentException(
            String.format(
                "Unknown invoice orders type: %d", supplychainBatch.getInvoiceOrdersTypeSelect()));
    }
  }

  public Batch updateStockHistory(SupplychainBatch supplychainBatch) {
    return Beans.get(BatchUpdateStockHistory.class).run(supplychainBatch);
  }

  @Transactional
  public SupplychainBatch createNewSupplychainBatch(int action, Company company) {
    if (company != null) {
      SupplychainBatch supplychainBatch = new SupplychainBatch();
      supplychainBatch.setActionSelect(action);
      supplychainBatch.setCompany(company);
      Beans.get(SupplychainBatchRepository.class).save(supplychainBatch);
      return supplychainBatch;
    }
    return null;
  }
}
