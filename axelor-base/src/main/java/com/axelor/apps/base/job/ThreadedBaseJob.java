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
package com.axelor.apps.base.job;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public abstract class ThreadedBaseJob extends ThreadedJob {
  @Inject protected BatchRepository batchRepo;

  protected void executeBatch(Class<? extends AbstractBatchService> batchService, String batchCode)
      throws AxelorException {
    Batch batch = Beans.get(batchService).run(batchCode);
    this.updateBatchOrigin(batch);
  }

  @Transactional
  protected void updateBatchOrigin(Batch batch) {
    batch.setActionLaunchOrigin(BatchRepository.ACTION_LAUNCH_ORIGIN_SCHEDULED);
    batchRepo.save(batch);
  }
}
