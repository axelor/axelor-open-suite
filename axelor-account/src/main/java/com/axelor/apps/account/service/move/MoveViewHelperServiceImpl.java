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
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.Company;
import com.google.common.base.Strings;
import org.apache.commons.collections4.ListUtils;

public class MoveViewHelperServiceImpl implements MoveViewHelperService {

  @Override
  public String filterPartner(Company company, Journal journal) {
    String domain = "self.isContact = false";
    if (company != null) {
      domain += " AND " + company.getId() + " member of self.companySet";
      if (journal != null && !Strings.isNullOrEmpty(journal.getCompatiblePartnerTypeSelect())) {
        domain += " AND (";
        String[] partnerSet = journal.getCompatiblePartnerTypeSelect().split(", ");
        String lastPartner = partnerSet[partnerSet.length - 1];
        for (String partner : partnerSet) {
          domain += "self." + partner + " = true";
          if (!partner.equals(lastPartner)) {
            domain += " OR ";
          }
        }
        domain += ")";
      }
    }
    return domain;
  }

  @Override
  public Move updateMoveLinesDateExcludeFromPeriodOnlyWithoutSave(Move move) {
    if (move.getPeriod() != null && move.getDate() != null) {
      for (MoveLine moveLine : ListUtils.emptyIfNull(move.getMoveLineList())) {
        if ((move.getPeriod().getFromDate().isAfter(moveLine.getDate())
            || move.getPeriod().getToDate().isBefore(moveLine.getDate()))) {
          moveLine.setDate(move.getDate());
        }
      }
    }
    return move;
  }
}
