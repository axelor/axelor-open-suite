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
package com.axelor.apps.partner.portal.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.crm.db.repo.LeadManagementRepository;
import com.axelor.apps.partner.portal.db.repo.LeadPartnerRepository;
import com.axelor.apps.partner.portal.service.ClientViewPartnerPortalService;
import com.axelor.apps.partner.portal.service.ClientViewPartnerPortalServiceImpl;
import com.axelor.apps.partner.portal.service.LeadPartnerPortalService;
import com.axelor.apps.partner.portal.service.LeadPartnerPortalServiceImpl;
import com.axelor.apps.portal.service.ClientViewServiceImpl;

public class PartnerPortalModule extends AxelorModule {
  @Override
  protected void configure() {
    bind(ClientViewPartnerPortalService.class).to(ClientViewPartnerPortalServiceImpl.class);
    bind(ClientViewServiceImpl.class).to(ClientViewPartnerPortalServiceImpl.class);
    bind(LeadPartnerPortalService.class).to(LeadPartnerPortalServiceImpl.class);
    bind(LeadManagementRepository.class).to(LeadPartnerRepository.class);
  }
}
