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

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class MoveSequenceService {

  private SequenceService sequenceService;

  @Inject
  public MoveSequenceService(SequenceService sequenceService) {

    this.sequenceService = sequenceService;
  }

  public void setDraftSequence(Move move) throws AxelorException {

    if (move.getId() != null
        && Strings.isNullOrEmpty(move.getReference())
        && (move.getStatusSelect() == MoveRepository.STATUS_NEW
            || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED)) {
      move.setReference(sequenceService.getDraftSequenceNumber(move));
    }
  }

  public void setSequence(Move move) throws AxelorException {

    Journal journal = move.getJournal();

    if (journal.getSequence() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_5),
          journal.getName());
    }

    if (!sequenceService.isEmptyOrDraftSequenceNumber(move.getReference())) {
      return;
    }

    move.setReference(
        sequenceService.getSequenceNumber(
            journal.getSequence(), move.getDate(), Move.class, "reference"));
  }
}
