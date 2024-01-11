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
package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BaseBatch;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.BaseBatchRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class BaseBatchService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return BaseBatch.class;
  }

  @Override
  public Batch run(Model batchModel) throws AxelorException {

    BaseBatch baseBatch = (BaseBatch) batchModel;

    switch (baseBatch.getActionSelect()) {
      case BaseBatchRepository.ACTION_SYNCHRONIZE_CALENDARS:
        return synchronizeCalendars(baseBatch);
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.BASE_BATCH_1),
            baseBatch.getActionSelect(),
            baseBatch.getCode());
    }
  }

  public Batch synchronizeCalendars(BaseBatch baseBatch) {
    return Beans.get(BatchCalendarSynchronization.class).run(baseBatch);
  }
}
