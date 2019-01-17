/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.apps.production.db.ProductionBatch;
import com.axelor.apps.production.db.repo.ProductionBatchRepository;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class ProductionBatchService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return ProductionBatch.class;
  }

  @Override
  public Batch run(Model model) throws AxelorException {

    Batch batch;
    ProductionBatch productionBatch = (ProductionBatch) model;

    switch (productionBatch.getActionSelect()) {
      case ProductionBatchRepository.ACTION_COMPUTE_WORK_IN_PROGRESS_VALUATION:
        batch = computeValuation(productionBatch);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.BASE_BATCH_1),
            productionBatch.getActionSelect(),
            productionBatch.getCode());
    }
    return batch;
  }

  public Batch computeValuation(ProductionBatch productionBatch) {
    return Beans.get(BatchComputeWorkInProgressValuation.class).run(productionBatch);
  }
}
