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
package com.axelor.apps.intervention.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerLink;
import com.axelor.apps.base.db.repo.PartnerLinkTypeRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InterventionPartnerServiceImpl implements InterventionPartnerService {

  @Override
  public Partner getDefaultInvoicedPartner(Partner partner) {
    if (partner == null || partner.getId() == null) {
      return null;
    }

    if (CollectionUtils.isEmpty(partner.getManagedByPartnerLinkList())) {
      return partner;
    }

    // Retrieve all Invoiced to Type
    List<PartnerLink> partnerLinkInvoicedByList =
        partner.getManagedByPartnerLinkList().stream()
            .filter(
                partnerSupplychainLink ->
                    PartnerLinkTypeRepository.TYPE_SELECT_INVOICED_TO.equals(
                        partnerSupplychainLink.getPartnerLinkType().getTypeSelect()))
            .collect(Collectors.toList());
    // If there is only one, then it is the default one
    if (partnerLinkInvoicedByList.size() == 1) {
      PartnerLink partnerLinkInvoicedBy = partnerLinkInvoicedByList.get(0);
      return partnerLinkInvoicedBy.getPartner2();
    } else if (CollectionUtils.isEmpty(partnerLinkInvoicedByList)) {
      return partner;
    } else {
      return null;
    }
  }
}
