package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.InterestRateHistoryLine;
import com.axelor.apps.account.service.payment.PaymentModeInterestRateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import java.util.Optional;
import javax.persistence.PersistenceException;

public class InterestRateHistoryLineRepositoryManagement extends InterestRateHistoryLineRepository {

  @Override
  public InterestRateHistoryLine save(InterestRateHistoryLine entity) {

    try {
      Beans.get(PaymentModeInterestRateService.class)
          .checkPeriodConsistency(
              entity.getPaymentMode(),
              entity,
              Optional.ofNullable(entity.getFromDate()),
              Optional.ofNullable(entity.getEndDate()));
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

    return super.save(entity);
  }
}
