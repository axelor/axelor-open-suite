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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.google.common.base.Strings;
import org.apache.commons.collections4.ListUtils;

public class MoveViewHelperServiceImpl implements MoveViewHelperService {

  @Override
  public String filterPartner(Move move) {
    Long companyId = move.getCompany().getId();
    String domain = "self.isContact = false AND " + companyId + " member of self.companySet";
    if (move.getJournal() != null
        && !Strings.isNullOrEmpty(move.getJournal().getCompatiblePartnerTypeSelect())) {
      domain += " AND (";
      String[] partnerSet = move.getJournal().getCompatiblePartnerTypeSelect().split(", ");
      String lastPartner = partnerSet[partnerSet.length - 1];
      for (String partner : partnerSet) {
        domain += "self." + partner + " = true";
        if (!partner.equals(lastPartner)) {
          domain += " OR ";
        }
      }
      domain += ")";
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
