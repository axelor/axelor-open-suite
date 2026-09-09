/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.rpc.Criteria;
import java.math.BigDecimal;
import java.util.List;

public class MoveLineSummaryServiceImpl implements MoveLineSummaryService {

  @Override
  public MoveLineSums computeMoveLineSums(Criteria criteria) {
    BigDecimal totalDebit = BigDecimal.ZERO;
    BigDecimal totalCredit = BigDecimal.ZERO;
    BigDecimal totalAmountRemaining = BigDecimal.ZERO;

    if (criteria == null) {
      return new MoveLineSums(totalDebit, totalCredit, totalAmountRemaining);
    }

    Query<MoveLine> moveLineQuery = criteria.createQuery(MoveLine.class).order("id");
    int offset = 0;
    List<MoveLine> moveLineList;

    while (!(moveLineList = moveLineQuery.fetch(AbstractBatch.FETCH_LIMIT, offset)).isEmpty()) {
      for (MoveLine moveLine : moveLineList) {
        ++offset;
        if (moveLine.getDebit() != null) {
          totalDebit = totalDebit.add(moveLine.getDebit());
        }
        if (moveLine.getCredit() != null) {
          totalCredit = totalCredit.add(moveLine.getCredit());
        }
        if (moveLine.getAmountRemaining() != null) {
          totalAmountRemaining = totalAmountRemaining.add(moveLine.getAmountRemaining());
        }
      }
      JPA.clear();
    }

    return new MoveLineSums(totalDebit, totalCredit, totalAmountRemaining);
  }
}
