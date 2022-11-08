package com.axelor.apps.stock.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.apps.stock.db.StockBatch;
import com.axelor.apps.stock.db.repo.StockBatchRepository;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
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
