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
package com.axelor.apps.intervention.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.db.repo.InterventionRepository;
import com.axelor.apps.intervention.exception.InterventionExceptionMessage;
import com.axelor.apps.intervention.service.helper.CustomerRequestHelper;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class InterventionManagementRepository extends InterventionRepository {

  protected SequenceService sequenceService;

  @Inject
  public InterventionManagementRepository(SequenceService sequenceService) {
    this.sequenceService = sequenceService;
  }

  @Override
  public Intervention save(Intervention intervention) {
    try {
      if (Strings.isNullOrEmpty(intervention.getSequence())) {

        String seq =
            sequenceService.getSequenceNumber(
                SequenceRepository.INTERVENTION_SEQUENCE,
                null,
                Intervention.class,
                "sequence",
                intervention);

        if (seq == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(InterventionExceptionMessage.INTERVENTION_NO_SEQUENCE));
        }
        intervention.setSequence(seq);
      }

      if (intervention.getCustomerRequest() == null
          && intervention.getInterventionType() != null
          && intervention.getInterventionType().getAutoGenerateCustomerRequest()) {

        if (isMissingField(intervention)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(InterventionExceptionMessage.INTERVENTION_MISSING_FIELDS));
        }
        intervention.setCustomerRequest(CustomerRequestHelper.create(intervention));
      }

      return super.save(intervention);
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  protected boolean isMissingField(Intervention intervention) {
    return intervention.getCompany() == null
        || intervention.getDeliveredPartner() == null
        || intervention.getAddress() == null;
  }
}
