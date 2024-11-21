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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
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

  @Transactional(rollbackOn = {Exception.class})
  public void cancel(Move move) throws AxelorException {

    if (move == null) {
      return;
    }

    for (MoveLine moveLine : move.getMoveLineList()) {

      if (moveLine.getReconcileGroup() != null) {
        throw new AxelorException(
            move,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.MOVE_CANCEL_7));
      }

      if (moveLine.getAccount().getUseForPartnerBalance()
          && moveLine.getAmountPaid().compareTo(BigDecimal.ZERO) != 0) {
        throw new AxelorException(
            move,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_CANCEL_1));
      }
    }

    if (move.getPeriod() == null
        || move.getPeriod().getStatusSelect() == PeriodRepository.STATUS_CLOSED
        || move.getPeriod().getStatusSelect() == PeriodRepository.STATUS_CLOSURE_IN_PROGRESS) {
      throw new AxelorException(
          move,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_CANCEL_2));
    }

    if (move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
        || move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK) {
      throw new AxelorException(
          move,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_CANCEL_5));
    }

    if (move.getStatusSelect() == MoveRepository.STATUS_CANCELED) {
      throw new AxelorException(
          move,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.MOVE_CANCEL_6));
    }

    try {

      if (move.getStatusSelect() == MoveRepository.STATUS_NEW) {
        moveRepository.remove(move);
      } else {
        move.setStatusSelect(MoveRepository.STATUS_CANCELED);
        moveRepository.save(move);
      }

    } catch (Exception e) {

      throw new AxelorException(
          move,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_CANCEL_3));
    }
  }
}
