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
package com.axelor.apps.supplychain.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.partner.PartnerCreationServiceImpl;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.base.service.wizard.BaseConvertLeadWizardService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PartnerCreationServiceSupplychainImpl extends PartnerCreationServiceImpl {

  protected BaseConvertLeadWizardService baseConvertLeadWizardService;

  @Inject
  public PartnerCreationServiceSupplychainImpl(
      PartnerRepository partnerRepository,
      UserService userService,
      AppBaseService appBaseService,
      BaseConvertLeadWizardService baseConvertLeadWizardService) {
    super(partnerRepository, userService, appBaseService);
    this.baseConvertLeadWizardService = baseConvertLeadWizardService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Partner createPartner(
      Integer partnerTypeSelect,
      Integer titleSelect,
      String firstName,
      String name,
      Partner mainPartner,
      String description,
      boolean isContact,
      boolean isCustomer,
      boolean isSupplier,
      boolean isProspect)
      throws AxelorException {
    Partner partner =
        super.createPartner(
            partnerTypeSelect,
            titleSelect,
            firstName,
            name,
            mainPartner,
            description,
            isContact,
            isCustomer,
            isSupplier,
            isProspect);
    baseConvertLeadWizardService.setPartnerFields(partner);
    partner.setAgency(partner.getUser().getActiveAgency());
    return partnerRepository.save(partner);
  }
}
