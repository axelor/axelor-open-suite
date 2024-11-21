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
package com.axelor.apps.account.service.journal;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.base.db.Partner;
import com.axelor.db.mapper.Mapper;
import com.google.common.base.Strings;

public class JournalCheckPartnerTypeServiceImpl implements JournalCheckPartnerTypeService {

  @Override
  public boolean isPartnerCompatible(Journal journal, Partner partner) {
    if (journal == null || Strings.isNullOrEmpty(journal.getCompatiblePartnerTypeSelect())) {
      return true;
    }
    String[] compatiblePartnerTypeSelectArray = journal.getCompatiblePartnerTypeSelect().split(",");
    Mapper partnerMapper = Mapper.of(Partner.class);
    for (String compatiblePartnerType : compatiblePartnerTypeSelectArray) {
      if (Boolean.TRUE.equals(partnerMapper.get(partner, compatiblePartnerType))) {
        return true;
      }
    }
    return false;
  }
}
