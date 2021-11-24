package com.axelor.apps.account.db.repo;

import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.exception.service.TraceBackService;
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
