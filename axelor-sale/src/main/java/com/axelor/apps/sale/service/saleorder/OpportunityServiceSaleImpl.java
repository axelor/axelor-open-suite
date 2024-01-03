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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.crm.db.LostReason;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.db.repo.OpportunityRepository;
import com.axelor.apps.crm.db.repo.OpportunityStatusRepository;
import com.axelor.apps.crm.service.OpportunityServiceImpl;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.google.inject.Inject;

public class OpportunityServiceSaleImpl extends OpportunityServiceImpl {

  protected OpportunitySaleOrderService opportunitySaleOrderService;

  @Inject
  public OpportunityServiceSaleImpl(
      OpportunityRepository opportunityRepo,
      OpportunityStatusRepository opportunityStatusRepo,
      AppCrmService appCrmService,
      PartnerRepository partnerRepository,
      OpportunitySaleOrderService opportunitySaleOrderService) {
    super(opportunityRepo, opportunityStatusRepo, appCrmService, partnerRepository);
    this.opportunitySaleOrderService = opportunitySaleOrderService;
  }

  @Override
  protected void lostProcess(Opportunity opportunity, LostReason lostReason, String lostReasonStr)
      throws AxelorException {
    super.lostProcess(opportunity, lostReason, lostReasonStr);
    opportunitySaleOrderService.cancelSaleOrders(opportunity);
  }
}
