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
