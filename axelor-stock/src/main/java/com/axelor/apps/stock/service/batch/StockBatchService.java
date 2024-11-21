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
package com.axelor.apps.stock.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.apps.stock.db.StockBatch;
import com.axelor.apps.stock.db.repo.StockBatchRepository;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class StockBatchService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return StockBatch.class;
  }

  @Override
  public Batch run(Model model) throws AxelorException {
    Batch batch;

    StockBatch stockBatch = (StockBatch) model;

    switch (stockBatch.getActionSelect()) {
      case StockBatchRepository.ACTION_RECOMPUTE_STOCK_LOCATION_LINE:
        batch = recomputeStockLocationLines(stockBatch);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.BASE_BATCH_1),
            stockBatch.getActionSelect(),
            stockBatch.getCode());
    }
    return batch;
  }

  protected Batch recomputeStockLocationLines(StockBatch stockBatch) {

    return Beans.get(BatchRecomputeStockLocationLines.class).run(stockBatch);
  }
}
