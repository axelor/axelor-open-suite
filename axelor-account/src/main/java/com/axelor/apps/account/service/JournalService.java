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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Query;

public class JournalService {
  protected final PartnerService partnerService;
  protected JournalRepository journalRepository;

  @Inject
  public JournalService(PartnerService partnerService, JournalRepository journalRepository) {
    this.partnerService = partnerService;
    this.journalRepository = journalRepository;
  }

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
    resultQuery.setParameter("statusValidated", MoveRepository.STATUS_ACCOUNTED);

    Object[] resultArr = (Object[]) resultQuery.getResultList().get(0);

    resultMap.put(
        "debit", resultArr[0] != null ? new BigDecimal(resultArr[0].toString()) : BigDecimal.ZERO);
    resultMap.put(
        "credit", resultArr[1] != null ? new BigDecimal(resultArr[1].toString()) : BigDecimal.ZERO);
    resultMap.put("balance", resultMap.get("debit").subtract(resultMap.get("credit")));

    return resultMap;
  }

  @Transactional
  public void toggleStatusSelect(Journal journal) {
    if (journal != null) {
      if (journal.getStatusSelect() == JournalRepository.STATUS_INACTIVE) {
        journal = activate(journal);
      } else {
        journal = desactivate(journal);
      }
      journalRepository.save(journal);
    }
  }

  protected Journal activate(Journal journal) {
    journal.setStatusSelect(JournalRepository.STATUS_ACTIVE);
    return journal;
  }

  protected Journal desactivate(Journal journal) {
    journal.setStatusSelect(JournalRepository.STATUS_INACTIVE);
    return journal;
  }

  public String filterJournalPartnerCompatibleType(Move move) {
    Journal journal = move.getJournal();

    if (journal == null) {
      return null;
    }

    if (journal.getCompatiblePartnerTypeSelect() != null) {
      StringBuilder compatiblePartnerDomain = new StringBuilder("self.id IN (");
      String[] compatiblePartnerTypeSelect = journal.getCompatiblePartnerTypeSelect().split(",");
      Set<Long> compatiblePartnerIds = new HashSet<>();
      for (String compatiblePartnerType : compatiblePartnerTypeSelect) {
        compatiblePartnerIds.addAll(partnerService.getPartnerIdsByType(compatiblePartnerType));
      }
      if (compatiblePartnerIds.size() > 0) {
        compatiblePartnerDomain.append(
            compatiblePartnerIds.stream().map(Object::toString).collect(Collectors.joining(",")));
        compatiblePartnerDomain.deleteCharAt(compatiblePartnerDomain.length() - 1);
        compatiblePartnerDomain.append(")");
        return compatiblePartnerDomain.toString();
      } else {
        return null;
      }
    }
    return null;
  }
}
