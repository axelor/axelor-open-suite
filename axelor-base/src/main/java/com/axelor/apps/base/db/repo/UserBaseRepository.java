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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class UserBaseRepository extends UserRepository {

  @Override
  public User save(User user) {
    try {
      if (user.getPartner() != null
          && user.getPartner().getEmailAddress() != null
          && StringUtils.notBlank(user.getPartner().getEmailAddress().getAddress())
          && !user.getPartner().getEmailAddress().getAddress().equals(user.getEmail())) {

        user.setEmail(user.getPartner().getEmailAddress().getAddress());
      }

      user = super.save(user);

      if (StringUtils.notBlank(user.getTransientPassword())) {
        Beans.get(UserService.class).processChangedPassword(user);
      }

      return user;
    } catch (Exception e) {
      e.printStackTrace();
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }

  @Override
  public User copy(User entity, boolean deep) {

    User copy = new User();

    copy.setGroup(entity.getGroup());
    copy.setRoles(entity.getRoles());
    copy.setPermissions(entity.getPermissions());
    copy.setMetaPermissions(entity.getMetaPermissions());
    copy.setActiveCompany(entity.getActiveCompany());
    copy.setCompanySet(entity.getCompanySet());
    copy.setLanguage(entity.getLanguage());
    copy.setHomeAction(entity.getHomeAction());
    copy.setSingleTab(entity.getSingleTab());
    copy.setNoHelp(entity.getNoHelp());

    return super.copy(copy, deep);
  }

  @Override
  public void remove(User user) {
    if (user.getPartner() != null) {
      PartnerBaseRepository partnerRepo = Beans.get(PartnerBaseRepository.class);
      Partner partner = partnerRepo.find(user.getPartner().getId());
      if (partner != null) {
        partner.setLinkedUser(null);
        partnerRepo.save(partner);
      }
    }
    super.remove(user);
  }
}
