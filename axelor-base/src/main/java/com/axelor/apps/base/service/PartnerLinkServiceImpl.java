/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerLink;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PartnerLinkServiceImpl implements PartnerLinkService {
  protected PartnerRepository partnerRepository;

  @Inject
  public PartnerLinkServiceImpl(PartnerRepository partnerRepository) {
    this.partnerRepository = partnerRepository;
  }

  @Override
  public String computePartnerFilter(Partner partner, String strFilter) {
    List<Long> partnerIds = getPartnerIds(partner, strFilter);

    return "self.id IN (" + Joiner.on(",").join(partnerIds) + ")";
  }

  public List<Long> getPartnerIds(Partner partner, String strFilter) {
    List<Long> partnerIds = new ArrayList<>();
    // manage the case where nothing is found
    partnerIds.add(0L);
    if (partner != null && partner.getManagedByPartnerLinkList() != null) {
      // add current partner
      partnerIds.add(partner.getId());
      partnerIds.addAll(
          partner.getManagedByPartnerLinkList().stream()
              .filter(
                  partnerSupplychainLink ->
                      partnerSupplychainLink.getPartnerLinkType() != null
                          && strFilter.equals(
                              partnerSupplychainLink.getPartnerLinkType().getTypeSelect())
                          && partnerSupplychainLink.getPartner2() != null)
              .map(PartnerLink::getPartner2)
              .map(Partner::getId)
              .collect(Collectors.toList()));
    }
    return partnerIds;
  }
}
