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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface MoveLineService {

  public MoveLine balanceCreditDebit(MoveLine moveLine, Move move);

  public void usherProcess(MoveLine moveLine);

  public void reconcileMoveLinesWithCacheManagement(List<MoveLine> moveLineList);

  public void reconcileMoveLines(List<MoveLine> moveLineList);

  public MoveLine setIsSelectedBankReconciliation(MoveLine moveLine);

  public MoveLine removePostedNbr(MoveLine moveLine, String postedNbr);

  public boolean checkManageAnalytic(Move move) throws AxelorException;

  void updatePartner(
          List<MoveLine> moveLineList, Partner partner, Partner previousPartner);
}
