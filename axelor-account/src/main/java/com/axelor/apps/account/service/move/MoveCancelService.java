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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class MoveCancelService {

  protected MoveRepository moveRepository;
  protected AccountConfigService accountConfigService;

  @Inject
  public MoveCancelService(
      AccountConfigService accountConfigService, MoveRepository moveRepository) {

    this.accountConfigService = accountConfigService;
    this.moveRepository = moveRepository;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void cancel(Move move) throws AxelorException {

    if (move == null) {
      return;
    }

    for (MoveLine moveLine : move.getMoveLineList()) {

      if (moveLine.getAccount().getUseForPartnerBalance()
          && moveLine.getAmountPaid().compareTo(BigDecimal.ZERO) != 0) {
        throw new AxelorException(
            move,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.MOVE_CANCEL_1));
      }
    }

    Period period =
        Beans.get(PeriodService.class)
            .getPeriod(move.getDate(), move.getCompany(), YearRepository.TYPE_FISCAL);
    if (period == null || period.getStatusSelect() == PeriodRepository.STATUS_CLOSED) {
      throw new AxelorException(
          move,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MOVE_CANCEL_2));
    }

    try {

      if (move.getStatusSelect() == MoveRepository.STATUS_NEW
          || accountConfigService
              .getAccountConfig(move.getCompany())
              .getAllowRemovalValidatedMove()) {
        moveRepository.remove(move);
      } else {
        move.setStatusSelect(MoveRepository.STATUS_CANCELED);
        moveRepository.save(move);
      }

    } catch (Exception e) {

      throw new AxelorException(
          move,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MOVE_CANCEL_3));
    }
  }
}
