/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.job;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.exception.AxelorException;
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

  @Transactional(rollbackOn = {Exception.class})
  protected void updateBatchOrigin(Batch batch) {
    batch.setActionLaunchOrigin(BatchRepository.ACTION_LAUNCH_ORIGIN_SCHEDULED);
    batchRepo.save(batch);
  }
}
