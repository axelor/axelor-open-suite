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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class PeriodManagementRepository extends PeriodRepository {

  protected MoveRepository moveRepository;

  @Inject
  public PeriodManagementRepository(MoveRepository moveRepository) {
    this.moveRepository = moveRepository;
  }

  @Override
  public Period save(Period entity) {

    try {
      updateDates(entity);
      return super.save(entity);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e);
    }
  }

  /**
   * Update period fromDate and toDate if it is not linked to a move.
   *
   * @param entity
   */
  protected void updateDates(Period entity) {

    Year fiscalYear = entity.getYear();

    if (fiscalYear != null && !isLinkedToMove(entity)) {
      if (entity.getFromDate() == null || entity.getFromDate().isBefore(fiscalYear.getFromDate())) {
        entity.setFromDate(fiscalYear.getFromDate());
      }
      if (entity.getToDate() == null || entity.getToDate().isAfter(fiscalYear.getToDate())) {
        entity.setToDate(fiscalYear.getToDate());
      }
    }
  }

  protected boolean isLinkedToMove(Period entity) {
    return moveRepository.all().filter("self.period = ?1", entity).count() > 0;
  }
}
