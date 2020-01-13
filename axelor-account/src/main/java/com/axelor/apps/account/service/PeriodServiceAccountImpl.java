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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.AdjustHistoryService;
import com.axelor.apps.base.service.PeriodServiceImpl;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PeriodServiceAccountImpl extends PeriodServiceImpl implements PeriodServiceAccount {

  protected MoveValidateService moveValidateService;
  protected MoveRepository moveRepository;

  @Inject
  public PeriodServiceAccountImpl(
      PeriodRepository periodRepo,
      AdjustHistoryService adjustHistoryService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepository) {
    super(periodRepo, adjustHistoryService);
    this.moveValidateService = moveValidateService;
    this.moveRepository = moveRepository;
  }

  public void close(Period period) throws AxelorException {

    if (period.getYear().getTypeSelect() == YearRepository.TYPE_FISCAL) {
      moveValidateService.validateMultiple(getMoveListToValidateQuery(period));
      period = periodRepo.find(period.getId());
    }
    super.close(period);
  }

  public Query<Move> getMoveListToValidateQuery(Period period) {
    return moveRepository
        .all()
        .filter(
            "self.period.id = ?1 AND (self.statusSelect NOT IN (?2,?3, ?4) OR (self.statusSelect = ?2 AND (self.archived = false OR self.archived is null)))",
            period.getId(),
            MoveRepository.STATUS_NEW,
            MoveRepository.STATUS_VALIDATED,
            MoveRepository.STATUS_CANCELED)
        .order("date")
        .order("id");
  }
}
