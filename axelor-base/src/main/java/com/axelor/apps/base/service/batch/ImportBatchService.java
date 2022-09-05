package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.ImportBatch;
import com.axelor.apps.base.db.repo.ImportBatchRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
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
