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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.supplychain.db.PartnerSupplychainLink;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PartnerSupplychainLinkServiceImpl implements PartnerSupplychainLinkService {

  @Override
  public String computePartnerFilter(Partner partner, String strFilter) {
    List<Long> partnerIds = new ArrayList<>();
    // manage the case where nothing is found
    partnerIds.add(0L);

    if (partner != null && partner.getPartner1SupplychainLinkList() != null) {
      // add current partner
      partnerIds.add(partner.getId());
      partnerIds.addAll(
          partner.getPartner1SupplychainLinkList().stream()
              .filter(
                  partnerSupplychainLink ->
                      partnerSupplychainLink.getPartnerSupplychainLinkType() != null
                          && strFilter.equals(
                              partnerSupplychainLink
                                  .getPartnerSupplychainLinkType()
                                  .getTypeSelect())
                          && partnerSupplychainLink.getPartner2() != null)
              .map(PartnerSupplychainLink::getPartner2)
              .map(Partner::getId)
              .collect(Collectors.toList()));
    }

    return "self.id IN (" + Joiner.on(",").join(partnerIds) + ")";
  }
}
