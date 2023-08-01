/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class MoveControlServiceImpl implements MoveControlService {

  protected MoveToolService moveToolService;

  @Inject
  public MoveControlServiceImpl(MoveToolService moveToolService) {
    this.moveToolService = moveToolService;
  }

  @Override
  public void checkSameCompany(Move move) throws AxelorException {

    Journal journal = move.getJournal();
    Company company = move.getCompany();

    if (journal != null && company != null && !company.equals(journal.getCompany())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.MOVE_INCONSISTENCY_DETECTED_JOURNAL_COMPANY),
          move.getReference(),
          journal.getName());
    }
  }

  @Override
  public void checkDuplicateOrigin(Move move) throws AxelorException {
    if (move.getJournal() != null
        && move.getPartner() != null
        && move.getJournal().getHasDuplicateDetectionOnOrigin()) {
      List<Move> moveList = moveToolService.getMovesWithDuplicatedOrigin(move);
      if (ObjectUtils.notEmpty(moveList)) {
        throw new AxelorException(
            move,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_DUPLICATE_ORIGIN_BLOCKING_MESSAGE),
            moveList.stream().map(Move::getReference).collect(Collectors.joining(",")),
            move.getPartner().getFullName(),
            move.getPeriod().getYear().getName());
      }
    }
  }
}
