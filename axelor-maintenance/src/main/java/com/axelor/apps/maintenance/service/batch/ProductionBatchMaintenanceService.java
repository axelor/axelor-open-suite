package com.axelor.apps.maintenance.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.production.db.ProductionBatch;
import com.axelor.apps.production.db.repo.ProductionBatchRepository;
import com.axelor.apps.production.service.batch.ProductionBatchService;
import com.axelor.db.Model;
import com.axelor.inject.Beans;

public class ProductionBatchMaintenanceService extends ProductionBatchService {

  @Override
  public Batch run(Model model) throws AxelorException {
    ProductionBatch productionBatch = (ProductionBatch) model;

    if (productionBatch.getActionSelect()
        == ProductionBatchRepository.ACTION_GENERATE_PREVENTIVE_MAINTENANCE_REQUESTS) {
      return generatePreventiveMaintenanceRequests(productionBatch);
    }

    return super.run(model);
  }

  public Batch generatePreventiveMaintenanceRequests(ProductionBatch productionBatch) {
    return Beans.get(BatchGeneratePreventiveMaintenanceRequest.class).run(productionBatch);
  }
}