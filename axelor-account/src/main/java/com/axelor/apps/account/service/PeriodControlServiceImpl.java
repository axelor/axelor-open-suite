package com.axelor.apps.account.service;

import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Objects;

public class PeriodControlServiceImpl implements PeriodControlService {

  protected MoveRepository moveRepository;
  protected PeriodRepository periodRepository;

  @Inject
  public PeriodControlServiceImpl(
      MoveRepository moveRepository, PeriodRepository periodRepository) {
    this.moveRepository = moveRepository;
    this.periodRepository = periodRepository;
  }

  @Override
  public void controlDates(Period period) throws AxelorException {
    Objects.requireNonNull(period);
    if (period.getId() != null) {
      Period savedPeriod = periodRepository.find(period.getId());
      if (savedPeriod.getStatusSelect() >= PeriodRepository.STATUS_OPENED
          && haveDifferentDates(period, savedPeriod)
          && isLinkedToMove(period)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.PERIOD_DIFFERENTS_DATE_WHEN_NOT_OPENED));
      }
    }
  }

  protected boolean haveDifferentDates(Period entity, Period savedPeriod) {

    return (entity.getFromDate() != null
            && savedPeriod.getFromDate() != null
            && !entity.getFromDate().isEqual(savedPeriod.getFromDate()))
        || (entity.getToDate() != null
            && savedPeriod.getToDate() != null
            && !entity.getToDate().isEqual(savedPeriod.getToDate()));
  }

  @Override
  public boolean isLinkedToMove(Period period) {
    return moveRepository.all().filter("self.period = ?1", period).count() > 0;
  }

  @Override
  public boolean isStatusValid(Period period) {

    return period.getYear() != null
        && period.getStatusSelect() >= PeriodRepository.STATUS_OPENED
        && period.getYear().getStatusSelect() >= YearRepository.STATUS_OPENED;
  }
}
