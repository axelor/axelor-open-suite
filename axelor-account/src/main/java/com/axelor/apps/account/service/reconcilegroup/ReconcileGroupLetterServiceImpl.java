/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.reconcilegroup;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class ReconcileGroupLetterServiceImpl implements ReconcileGroupLetterService {

  protected MoveLineRepository moveLineRepository;
  protected MoveLineService moveLineService;

  @Inject
  public ReconcileGroupLetterServiceImpl(
      MoveLineRepository moveLineRepository, MoveLineService moveLineService) {
    this.moveLineRepository = moveLineRepository;
    this.moveLineService = moveLineService;
  }

  @Override
  public void letter(ReconcileGroup reconcileGroup) throws AxelorException {
    Company company = reconcileGroup.getCompany();
    List<MoveLine> moveLines =
        moveLineRepository
            .all()
            .filter("self.reconcileGroup = :reconcileGroup")
            .bind("reconcileGroup", reconcileGroup)
            .fetch();
    if (CollectionUtils.isEmpty(moveLines)) {
      return;
    }
    for (MoveLine moveLine : moveLines) {
      Company moveCompany =
          Optional.ofNullable(moveLine).map(MoveLine::getMove).map(Move::getCompany).orElse(null);
      if (company != null && !company.equals(moveCompany)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.RECONCILE_GROUP_WRONG_MOVE_LINE_COMPANY),
            moveLine.getName(),
            reconcileGroup.getCode());
      }
    }
    moveLineService.reconcileMoveLines(moveLines);
  }
}
