package com.axelor.apps.intervention.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.intervention.db.CustomerRequest;
import com.axelor.apps.intervention.db.repo.CustomerRequestRepository;
import com.axelor.apps.intervention.exception.InterventionExceptionMessage;
import com.axelor.apps.intervention.service.helper.CustomerRequestHelper;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class CustomerRequestManagementRepository extends CustomerRequestRepository {

  protected SequenceService sequenceService;

  @Inject
  public CustomerRequestManagementRepository(SequenceService sequenceService) {
    this.sequenceService = sequenceService;
  }

  @Override
  public CustomerRequest save(CustomerRequest customerRequest) {
    try {
      if (Strings.isNullOrEmpty(customerRequest.getSequence())) {
        String seq =
            sequenceService.getSequenceNumber(
                SequenceRepository.CUSTOMER_REQUEST_SEQUENCE,
                null,
                CustomerRequest.class,
                "sequence",
                customerRequest);

        if (seq == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(InterventionExceptionMessage.CUSTOMER_REQUEST_NO_SEQUENCE));
        }
        customerRequest.setSequence(seq);
      }
      CustomerRequestHelper.computeGt(customerRequest);
      return super.save(customerRequest);
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
