package com.axelor.apps.hr.service;

import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.PeriodServiceAccountImpl;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.AdjustHistoryService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class PeriodServiceHumanResourceImpl extends PeriodServiceAccountImpl {

  @Inject
  public PeriodServiceHumanResourceImpl(
      PeriodRepository periodRepo,
      AdjustHistoryService adjustHistoryService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepository) {
    super(periodRepo, adjustHistoryService, moveValidateService, moveRepository);
  }

  public void close(Period period) throws AxelorException {
    if (period.getYear().getTypeSelect() == YearRepository.TYPE_PAYROLL) {}
    super.close(period);
  }
}
