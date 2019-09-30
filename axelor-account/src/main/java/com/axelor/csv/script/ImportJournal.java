/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.inject.Beans;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ImportJournal {
  public Object importAccountType(Object bean, Map<String, Object> values) {
    assert bean instanceof Journal;
    Journal journal = (Journal) bean;

    // Only 'Manual misc ops' Journal
    String importId = journal.getImportId();
    if (!"24".equals(importId)) {
      return bean;
    }

    Set<AccountType> accountTypesSet =
        new HashSet<AccountType>(Beans.get(AccountTypeRepository.class).all().fetch());
    journal.setValidAccountTypeSet(accountTypesSet);

    return journal;
  }
}
