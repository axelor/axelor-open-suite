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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.i18n.L10n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PeriodServiceImpl implements PeriodService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected PeriodRepository periodRepo;
  protected AdjustHistoryService adjustHistoryService;
  protected int oldPeriodStatusSelect;

  @Inject
  public PeriodServiceImpl(PeriodRepository periodRepo, AdjustHistoryService adjustHistoryService) {
    this.periodRepo = periodRepo;
    this.adjustHistoryService = adjustHistoryService;
  }

  /**
   * Fetches the active period with the date, company and type in parameter
   *
   * @param date
   * @param company
   * @param typeSelect
   * @return
   * @throws AxelorException
   */
  public Period getActivePeriod(LocalDate date, Company company, int typeSelect)
      throws AxelorException {

    Period period = this.getPeriod(date, company, typeSelect);
    if (period == null || this.isClosedPeriod(period)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.PERIOD_1),
          company.getName(),
          L10n.getInstance().format(date));
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

    if (nextPeriod == null || this.isClosedPeriod(nextPeriod)) {
      throw new AxelorException(
          period,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.PERIOD_1),
          period.getYear().getCompany().getName());
    }
    LOG.debug("Next Period : {}", nextPeriod);
    return nextPeriod;
  }

  public void testOpenPeriod(Period period) throws AxelorException {
    if (this.isClosedPeriod(period)) {
      throw new AxelorException(
          period,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.PERIOD_2));
    }
  }

  public void close(Period period) {
    if (period.getStatusSelect() == PeriodRepository.STATUS_ADJUSTING) {
      adjustHistoryService.setEndDate(period);
    }
    this.updateClosePeriod(period);
  }

  public void closeTemporarily(Period period) throws AxelorException {
    if (period.getStatusSelect() == PeriodRepository.STATUS_ADJUSTING) {
      adjustHistoryService.setEndDate(period);
    }
    this.updateCloseTemporarilyPeriod(period);
  }

  @Transactional
  protected void updateClosePeriod(Period period) {
    period.setStatusSelect(PeriodRepository.STATUS_CLOSED);
    period.setClosureDateTime(LocalDateTime.now());

    periodRepo.save(period);
  }

  @Transactional
  protected void updateCloseTemporarilyPeriod(Period period) {
    period.setStatusSelect(PeriodRepository.STATUS_TEMPORARILY_CLOSED);
    period.setTemporarilyCloseDateTime(LocalDateTime.now());

    periodRepo.save(period);
  }

  @Transactional
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
    if (this.isClosedPeriod(period)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BaseExceptionMessage.PAY_PERIOD_CLOSED),
          period.getName());
    }
  }

  @Override
  public boolean isClosedPeriod(Period period) throws AxelorException {
    List<Integer> unauthorizedStatus = new ArrayList<>();
    unauthorizedStatus.add(PeriodRepository.STATUS_TEMPORARILY_CLOSED);
    unauthorizedStatus.add(PeriodRepository.STATUS_CLOSED);

    return period != null && unauthorizedStatus.contains(period.getStatusSelect());
  }

  @Override
  public void validateTempClosure(Period period) throws AxelorException {
    if (period != null && period.getYear() != null && period.getYear().getCompany() != null) {
      Query resultQuery =
          JPA.em()
              .createQuery(
                  "SELECT self.id FROM Period self WHERE self.toDate = :date AND self.year.company = :company AND self.year.typeSelect = :typeSelect");
      resultQuery.setParameter("date", period.getFromDate().minusDays(1));
      resultQuery.setParameter("company", period.getYear().getCompany());
      resultQuery.setParameter("typeSelect", period.getYear().getTypeSelect());
      if (resultQuery.getResultList() != null && !resultQuery.getResultList().isEmpty()) {
        for (Object result : resultQuery.getResultList()) {
          Period previousPeriod = periodRepo.find(Long.valueOf(result.toString()).longValue());
          if (previousPeriod.getStatusSelect() != PeriodRepository.STATUS_TEMPORARILY_CLOSED
              && previousPeriod.getStatusSelect() != PeriodRepository.STATUS_CLOSED
              && previousPeriod.getStatusSelect() != PeriodRepository.STATUS_CLOSURE_IN_PROGRESS) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(BaseExceptionMessage.PREVIOUS_PERIOD_NOT_TEMP_CLOSED));
          }
        }
      }
    }
  }

  @Override
  public void validateClosure(Period period) throws AxelorException {
    if (period != null
        && period.getYear() != null
        && period.getYear().getCompany() != null
        && period.getYear().getTypeSelect() != null) {
      Query resultQuery =
          JPA.em()
              .createQuery(
                  "SELECT self.id FROM Period self WHERE self.toDate = :date AND self.year.company = :company AND self.year.typeSelect = :type");
      resultQuery.setParameter("date", period.getFromDate().minusDays(1));
      resultQuery.setParameter("company", period.getYear().getCompany());
      resultQuery.setParameter("type", period.getYear().getTypeSelect());
      if (resultQuery.getResultList() != null && !resultQuery.getResultList().isEmpty()) {

        for (Object result : resultQuery.getResultList()) {
          Period previousPeriod = periodRepo.find(Long.valueOf(result.toString()).longValue());
          if (previousPeriod.getStatusSelect() != PeriodRepository.STATUS_CLOSED
              && previousPeriod.getStatusSelect() != PeriodRepository.STATUS_CLOSURE_IN_PROGRESS) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(BaseExceptionMessage.PREVIOUS_PERIOD_NOT_CLOSED));
          }
        }
      }
    }
  }

  @Override
  @Transactional
  public void openPeriod(Period period) {
    if (period != null) {
      period.setStatusSelect(PeriodRepository.STATUS_OPENED);
    }
  }

  @Override
  @Transactional
  public void closureInProgress(Period period) {
    period.setStatusSelect(PeriodRepository.STATUS_CLOSURE_IN_PROGRESS);
    periodRepo.save(period);
  }

  @Override
  public void closePeriod(Period period) {
    this.oldPeriodStatusSelect = period.getStatusSelect();

    this.closureInProgress(period);
    this.close(period);
  }

  @Override
  @Transactional
  public void resetStatusSelect(Period period) {
    if (period != null) {
      period.setStatusSelect(this.oldPeriodStatusSelect);
    }
  }
}
