package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.SequenceBaseRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class SequenceAccountManagementRepository extends SequenceBaseRepository {

  @Override
  public Sequence save(Sequence entity) {
    try {
      Sequence sequence =
          Beans.get(SequenceService.class)
              .getSequence(SequenceRepository.FIXED_ASSET, entity.getCompany());
      if (entity.getCompany() != null && sequence != null && !sequence.equals(entity)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.FIXED_ASSET_SEQUENCE_ALREADY_EXISTS));
      }
      return super.save(entity);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e);
    }
  }
}
