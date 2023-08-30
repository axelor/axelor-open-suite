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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerLink;
import com.axelor.apps.base.db.repo.PartnerLinkTypeRepository;
import com.axelor.meta.CallMethod;
import org.apache.commons.collections.CollectionUtils;

public class PartnerAccountService {

  public String getDefaultSpecificTaxNote(Partner partner) {
    FiscalPosition fiscalPosition = partner.getFiscalPosition();

    if (fiscalPosition == null || !fiscalPosition.getCustomerSpecificNote()) {
      return "";
    }

    return fiscalPosition.getCustomerSpecificNoteText();
  }

  @CallMethod
  public Partner getPayedByPartner(Partner partner) {
    if (partner == null || CollectionUtils.isEmpty(partner.getManagedByPartnerLinkList())) {
      return null;
    }

    return partner.getManagedByPartnerLinkList().stream()
        .filter(
            it ->
                it.getPartnerLinkType().getTypeSelect()
                    == PartnerLinkTypeRepository.TYPE_SELECT_PAYED_BY)
        .map(PartnerLink::getPartner2)
        .findFirst()
        .orElse(null);
  }
}
