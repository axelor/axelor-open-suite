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
