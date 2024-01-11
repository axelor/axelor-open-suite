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

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.persistence.PersistenceException;

public class MoveLineManagementRepository extends MoveLineRepository {

  @Override
  public void remove(MoveLine entity) {
    if (!entity.getMove().getStatusSelect().equals(MoveRepository.STATUS_NEW)) {
      try {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_REMOVE_NOT_OK),
            entity.getMove().getReference());
      } catch (AxelorException e) {
        throw new PersistenceException(e.getMessage(), e);
      }
    } else {
      super.remove(entity);
    }
  }

  @Override
  public MoveLine copy(MoveLine entity, boolean deep) {
    MoveLine copy = super.copy(entity, deep);

    copy.setPostedNbr(null);
    return copy;
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

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      if (context.containsKey("_cutOffPreview") && (boolean) context.get("_cutOffPreview")) {
        long id = (long) json.get("id");
        MoveLine moveLine = this.find(id);
        LocalDate moveDate = LocalDate.parse((String) context.get("_moveDate"));

        json.put(
            "$cutOffProrataAmount",
            Beans.get(MoveLineService.class).getCutOffProrataAmount(moveLine, moveDate));
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return super.populate(json, context);
  }
}
