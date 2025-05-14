/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerLinkTypeRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerLinkServiceImpl;
import com.google.inject.Inject;
import java.util.List;

public class PartnerLinkSupplychainServiceImpl extends PartnerLinkServiceImpl
    implements PartnerLinkSupplychainService {
  @Inject
  public PartnerLinkSupplychainServiceImpl(PartnerRepository partnerRepository) {
    super(partnerRepository);
  }

  @Override
  public Partner getDefaultInvoicedPartner(Partner clientPartner) {

    List<Long> partnerIds =
        getPartnerIds(clientPartner, PartnerLinkTypeRepository.TYPE_SELECT_INVOICED_TO);
    // If there is only one, then it is the default one
    if (partnerIds.size() == 1) {
      return partnerRepository.find(partnerIds.get(0));
    } else if (partnerIds.isEmpty()) {
      return clientPartner;
    } else {
      return null;
    }
  }

  @Override
  public Partner getDefaultDeliveredPartner(Partner clientPartner) {
    List<Long> partnerIds =
        getPartnerIds(clientPartner, PartnerLinkTypeRepository.TYPE_SELECT_DELIVERED_TO);
    // If there is only one, then it is the default one
    if (partnerIds.size() == 1) {
      return partnerRepository.find(partnerIds.get(0));
    } else if (partnerIds.isEmpty()) {
      return clientPartner;
    } else {
      return null;
    }
  }
}
