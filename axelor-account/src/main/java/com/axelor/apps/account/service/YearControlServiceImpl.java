package com.axelor.apps.account.service;

import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class YearControlServiceImpl implements YearControlService {

  protected YearRepository yearRepository;
  protected MoveRepository moveRepository;

  @Inject
  public YearControlServiceImpl(YearRepository yearRepository, MoveRepository moveRepository) {
    this.yearRepository = yearRepository;
    this.moveRepository = moveRepository;
  }

  @Override
  public void controlDates(Year year) throws AxelorException {
    Objects.requireNonNull(year);
    if (year.getId() != null) {
      Year savedYear = yearRepository.find(year.getId());
      if (savedYear.getStatusSelect() >= PeriodRepository.STATUS_OPENED
          && haveDifferentDates(year, savedYear)
          && isLinkedToMove(year)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.FISCAL_YEARS_DIFFERENTS_DATE_WHEN_NOT_OPENED));
      }
    }
  }

  @Override
  public boolean isLinkedToMove(Year year) {

    if (CollectionUtils.isEmpty(year.getPeriodList())) {
      return false;
    }
    return moveRepository
            .all()
            .filter("self.period.id in (:periodIdList)")
            .bind(
                "periodIdList",
                year.getPeriodList().stream()
                    .map(period -> period.getId())
                    .collect(Collectors.toList()))
            .count()
        > 0;
  }

  protected boolean haveDifferentDates(Year year, Year savedYear) {
    return (year.getFromDate() != null
            && savedYear.getFromDate() != null
            && !year.getFromDate().isEqual(savedYear.getFromDate()))
        || (year.getToDate() != null
            && savedYear.getToDate() != null
            && !year.getToDate().isEqual(savedYear.getToDate()));
  }
}
