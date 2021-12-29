/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.partner.portal.service;

import com.axelor.apps.crm.db.Lead;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class LeadPartnerPortalServiceImpl implements LeadPartnerPortalService {

  @Override
  @Transactional
  public void manageUnreadLead(Lead lead, User user) {

    if (user == null) {
      return;
    }

    String ids = "";
    if (StringUtils.notBlank(user.getLeadUnreadIds())) {
      ids = String.format("%s,", user.getLeadUnreadIds());
    }

    user.setLeadUnreadIds(String.format("%s%s", ids, lead.getId().toString()));
    Beans.get(UserRepository.class).save(user);
  }
}
