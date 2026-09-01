/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import static com.axelor.apps.base.db.repo.PartnerRepository.PARTNER_TYPE_INDIVIDUAL;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.partner.PartnerContactLinkService;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;

public class PartnerConvertServiceImpl implements PartnerConvertService {

  protected final PartnerService partnerService;
  protected final PartnerContactLinkService partnerContactLinkService;

  @Inject
  public PartnerConvertServiceImpl(
      PartnerService partnerService, PartnerContactLinkService partnerContactLinkService) {
    this.partnerService = partnerService;
    this.partnerContactLinkService = partnerContactLinkService;
  }

  @Transactional
  @Override
  public void convertToIndividualPartner(Partner partner) throws AxelorException {
    partnerContactLinkService.onPartnerRemove(partner);
    partner.setIsContact(false);
    partner.setPartnerTypeSelect(PARTNER_TYPE_INDIVIDUAL);
    Address mainAddress = partner.getMainAddress();
    if (mainAddress != null) {
      partnerService.addPartnerAddress(partner, mainAddress, true, false, false);
    }
    partner.setMainAddress(null);
  }
}
