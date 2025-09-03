/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.sale.db.SaleBatch;
import com.axelor.apps.sale.db.repo.SaleBatchRepository;
import com.axelor.apps.sale.service.batch.SaleBatchService;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class SaleBatchSupplyChainService extends SaleBatchService {

  @Inject
  public SaleBatchSupplyChainService(SaleBatchRepository saleBatchRepo) {
    super(saleBatchRepo);
  }

  @Override
  public Batch run(Model batchModel) throws AxelorException {
    SaleBatch saleBatch = (SaleBatch) batchModel;
    switch (saleBatch.getActionSelect()) {
      case SaleBatchRepository.ACTION_INVOICING:
        return generateSubscriberInvoices(saleBatch);
      default:
        return super.run(batchModel);
    }
  }

  protected Batch generateSubscriberInvoices(SaleBatch saleBatch) {
    return Beans.get(BatchInvoicing.class).run(saleBatch);
  }
}
