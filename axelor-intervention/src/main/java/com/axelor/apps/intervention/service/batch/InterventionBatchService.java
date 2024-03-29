package com.axelor.apps.intervention.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.apps.intervention.db.InterventionBatch;
import com.axelor.apps.intervention.db.repo.InterventionBatchRepository;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class InterventionBatchService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return InterventionBatch.class;
  }

  @Override
  public Batch run(Model model) throws AxelorException {

    InterventionBatch interventionBatch = (InterventionBatch) model;

    switch (interventionBatch.getActionSelect()) {
      case InterventionBatchRepository.ACTION_SELECT_GENERATE_INTERVENTION_FOR_ACTIVE_CONTRACTS:
        return generateInterventionForActiveContracts(interventionBatch);
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.BASE_BATCH_1),
            interventionBatch.getActionSelect(),
            interventionBatch.getCode());
    }
  }

  public Batch generateInterventionForActiveContracts(InterventionBatch interventionBatch) {
    return Beans.get(BatchContractInterventionGenerationService.class).run(interventionBatch);
  }
}
