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