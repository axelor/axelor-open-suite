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

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.db.JPA;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Query;

public class JournalService {

  /**
   * Compute the balance of the journal, depending of the account type and balance type
   *
   * @param journal Journal
   * @return The balance (debit balance or credit balance)
   */
  public Map<String, BigDecimal> computeBalance(Journal journal) {

    Map<String, BigDecimal> resultMap = new HashMap<>();

    String query =
        "select sum(self.debit),sum(self.credit)"
            + " from MoveLine self where self.move.journal.id = :journal "
            + "and self.move.ignoreInAccountingOk IN ('false', null) and self.move.statusSelect IN (:statusDaybook, :statusValidated) and self.account.accountType MEMBER OF self.move.journal.journalType.accountTypeSet";

    Query resultQuery = JPA.em().createQuery(query);

    resultQuery.setParameter("journal", journal.getId());
    resultQuery.setParameter("statusDaybook", MoveRepository.STATUS_DAYBOOK);
    resultQuery.setParameter("statusValidated", MoveRepository.STATUS_VALIDATED);

    Object[] resultArr = (Object[]) resultQuery.getResultList().get(0);

    resultMap.put(
        "debit", resultArr[0] != null ? new BigDecimal(resultArr[0].toString()) : BigDecimal.ZERO);
    resultMap.put(
        "credit", resultArr[1] != null ? new BigDecimal(resultArr[1].toString()) : BigDecimal.ZERO);
    resultMap.put("balance", resultMap.get("debit").subtract(resultMap.get("credit")));

    return resultMap;
  }
}
