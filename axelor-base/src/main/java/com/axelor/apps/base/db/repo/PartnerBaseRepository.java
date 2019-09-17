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
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.auth.db.User;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import javax.persistence.PersistenceException;

public class PartnerBaseRepository extends PartnerRepository {

  @Override
  public Partner save(Partner partner) {
    try {
      Beans.get(PartnerService.class).onSave(partner);
      return super.save(partner);
    } catch (Exception e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (!context.containsKey("json-enhance")) {
      return json;
    }
    try {
      Long id = (Long) json.get("id");
      Partner partner = find(id);
      json.put("address", Beans.get(PartnerService.class).getDefaultAddress(partner));
    } catch (Exception e) {
      e.printStackTrace();
    }

    return json;
  }

  @Override
  public Partner copy(Partner partner, boolean deep) {

    Partner copy = super.copy(partner, deep);

    copy.setPartnerSeq(null);
    copy.setEmailAddress(null);

    PartnerAddressRepository partnerAddressRepository = Beans.get(PartnerAddressRepository.class);

    List<PartnerAddress> partnerAddressList = Lists.newArrayList();

    if (deep && copy.getPartnerAddressList() != null) {
      for (PartnerAddress partnerAddress : copy.getPartnerAddressList()) {

        partnerAddressList.add(partnerAddressRepository.copy(partnerAddress, deep));
      }
    }
    copy.setPartnerAddressList(partnerAddressList);
    copy.setBlockingList(null);
    copy.setBankDetailsList(null);

    return copy;
  }

  @Override
  public void remove(Partner partner) {
    if (partner.getLinkedUser() != null) {
      UserBaseRepository userRepo = Beans.get(UserBaseRepository.class);
      User user = userRepo.find(partner.getLinkedUser().getId());
      if (user != null) {
        user.setPartner(null);
        userRepo.save(user);
      }
    }

    super.remove(partner);
  }
}
