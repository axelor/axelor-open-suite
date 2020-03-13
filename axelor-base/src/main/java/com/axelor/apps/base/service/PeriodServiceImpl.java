/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PeriodServiceImpl implements PeriodService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected PeriodRepository periodRepo;
  protected AdjustHistoryService adjustHistoryService;

  @Inject
  public PeriodServiceImpl(PeriodRepository periodRepo, AdjustHistoryService adjustHistoryService) {
    this.periodRepo = periodRepo;
    this.adjustHistoryService = adjustHistoryService;
  }

  /**
   * Fetches the right period with the date in parameter
   *
   * @param date
   * @param company
   * @return
   * @throws AxelorException
   */
  public Period rightPeriod(LocalDate date, Company company, int typeSelect)
      throws AxelorException {

    Period period = this.getPeriod(date, company, typeSelect);
    if (period == null || period.getStatusSelect() == PeriodRepository.STATUS_CLOSED) {
      String dateStr = date != null ? date.toString() : "";
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PERIOD_1),
          company.getName(),
          dateStr);
    }
    LOG.debug("Period : {}", period);
    return period;
  }

  public Period getPeriod(LocalDate date, Company company, int typeSelect) {

    return periodRepo
        .all()
        .filter(
            "self.year.company = ?1 and self.fromDate <= ?2 and self.toDate >= ?2 and self.year.typeSelect = ?3",
            company,
            date,
            typeSelect)
        .fetchOne();
  }

  public Period getNextPeriod(Period period) throws AxelorException {

    Period nextPeriod =
        periodRepo
            .all()
            .filter(
                "self.fromDate > ?1 AND self.year.company = ?2 AND self.statusSelect = ?3",
                period.getToDate(),
                period.getYear().getCompany(),
                PeriodRepository.STATUS_OPENED)
            .fetchOne();

    if (nextPeriod == null || nextPeriod.getStatusSelect() == PeriodRepository.STATUS_CLOSED) {
      throw new AxelorException(
          period,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PERIOD_1),
          period.getYear().getCompany().getName());
    }
    LOG.debug("Next Period : {}", nextPeriod);
    return nextPeriod;
  }

  public void testOpenPeriod(Period period) throws AxelorException {
    if (period.getStatusSelect() == PeriodRepository.STATUS_CLOSED) {
      throw new AxelorException(
          period,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PERIOD_2));
    }
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void close(Period period) {

    if (period.getStatusSelect() == PeriodRepository.STATUS_ADJUSTING) {
      adjustHistoryService.setEndDate(period);
    }

    period.setStatusSelect(PeriodRepository.STATUS_CLOSED);
    period.setClosureDateTime(LocalDateTime.now());
    periodRepo.save(period);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void adjust(Period period) {
    period = periodRepo.find(period.getId());

    adjustHistoryService.setStartDate(period);

    period.setStatusSelect(PeriodRepository.STATUS_ADJUSTING);
    periodRepo.save(period);
  }

  /**
   * Check if the period corresponding to the date and the company is closed
   *
   * @param company
   * @param date
   * @throws AxelorException
   */
  public void checkPeriod(Company company, LocalDate date) throws AxelorException {
    this.checkPeriod(company, date, date);
  }

  /**
   * Check if the periods corresponding to the dates and the company are closed.
   *
   * @param company
   * @param fromDate
   * @param toDate
   */
  public void checkPeriod(Company company, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    List<Period> periodList =
        periodRepo
            .all()
            .filter(
                "self.year.typeSelect = :_typeSelect "
                    + "AND self.year.company = :_company "
                    + "AND ((self.fromDate <= :_fromDate "
                    + "AND self.toDate >= :_fromDate) "
                    + "OR (self.fromDate <= :_toDate "
                    + "AND self.toDate >= :_toDate))")
            .bind("_typeSelect", YearRepository.TYPE_PAYROLL)
            .bind("_company", company)
            .bind("_fromDate", fromDate)
            .bind("_toDate", toDate)
            .fetch();
    if (periodList == null) {
      return;
    }
    for (Period period : periodList) {
      checkPeriod(period);
    }
  }

  /**
   * @param period
   * @throws AxelorException if the period is closed
   */
  public void checkPeriod(Period period) throws AxelorException {
    if (period != null && period.getStatusSelect() == PeriodRepository.STATUS_CLOSED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.PAY_PERIOD_CLOSED),
          period.getName());
    }
  }
}
