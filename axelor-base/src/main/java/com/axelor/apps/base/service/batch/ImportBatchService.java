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
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.ImportBatch;
import com.axelor.apps.base.db.repo.ImportBatchRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class ImportBatchService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return ImportBatch.class;
  }

  @Override
  public Batch run(Model model) throws AxelorException {

    Batch batch;
    ImportBatch importBatch = (ImportBatch) model;

    switch (importBatch.getActionSelect()) {
      case ImportBatchRepository.ACTION_SELECT_IMPORT:
        batch = importData(importBatch);
        break;
      case ImportBatchRepository.ACTION_SELECT_ADVANCE_IMPORT:
        batch = advancedImportData(importBatch);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.BASE_BATCH_1),
            importBatch.getActionSelect(),
            importBatch.getCode());
    }

    return batch;
  }

  public Batch importData(ImportBatch importBatch) {
    return Beans.get(BatchImportData.class).run(importBatch);
  }

  public Batch advancedImportData(ImportBatch importBatch) {
    return Beans.get(BatchAdvancedImportData.class).run(importBatch);
  }
}
