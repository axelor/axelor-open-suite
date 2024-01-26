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
package com.axelor.csv.script;

import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.google.inject.Inject;
import java.util.Map;

public class ImportLead {

  @Inject private UserRepository userRepo;

  @Inject private LeadRepository leadRepo;

  public User importCreatedBy(String importId) {
    User user = userRepo.all().filter("self.importId = ?1", importId).fetchOne();
    if (user != null) return user;
    return userRepo.all().filter("self.code = 'democrm'").fetchOne();
  }

  public Object saveLead(Object bean, Map<String, Object> values) {

    assert bean instanceof Lead;

    Lead lead = (Lead) bean;

    return leadRepo.save(lead);
  }
}
