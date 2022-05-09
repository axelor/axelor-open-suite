/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.util.List;
import javax.persistence.PersistenceException;

public class MoveLineManagementRepository extends MoveLineRepository {

  @Override
  public void remove(MoveLine entity) {
    if (!entity.getMove().getStatusSelect().equals(MoveRepository.STATUS_NEW)) {
      try {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.MOVE_REMOVE_NOT_OK),
            entity.getMove().getReference());
      } catch (AxelorException e) {
        throw new PersistenceException(e.getMessage(), e);
      }
    } else {
      super.remove(entity);
    }
  }

  @Override
  public MoveLine save(MoveLine entity) {

    List<AnalyticMoveLine> analyticMoveLineList = entity.getAnalyticMoveLineList();
    if (analyticMoveLineList != null) {
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        analyticMoveLine.setAccount(entity.getAccount());
        analyticMoveLine.setAccountType(entity.getAccount().getAccountType());
      }
    }
    try {
      Beans.get(MoveLineControlService.class).validateMoveLine(entity);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
    return super.save(entity);
  }
}
