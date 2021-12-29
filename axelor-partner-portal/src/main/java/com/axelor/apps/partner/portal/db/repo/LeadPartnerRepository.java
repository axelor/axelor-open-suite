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
package com.axelor.apps.partner.portal.db.repo;

import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadManagementRepository;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LeadPartnerRepository extends LeadManagementRepository {

  @Inject UserRepository userRepo;

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    Map<String, Object> map = super.populate(json, context);

    boolean unread = false;
    if (json != null && json.get("id") != null) {
      final Lead lead = find((Long) json.get("id"));
      User currentUser = Beans.get(UserService.class).getUser();
      String ids = currentUser.getLeadUnreadIds();
      if (StringUtils.notBlank(ids)) {
        List<String> idList = Arrays.asList(ids.split(","));
        if (!ObjectUtils.isEmpty(idList) && idList.contains(lead.getId().toString())) {
          unread = true;
        }
      }
      map.put("$unread", unread);
    }

    return map;
  }
}
