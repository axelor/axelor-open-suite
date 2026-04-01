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
package com.axelor.apps.supplychain.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.pricing.PricingGenericService;
import com.axelor.db.Model;
import com.axelor.studio.db.AppBase;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class PricingSupplychainServiceImpl implements PricingSupplychainService {

  protected PricingGenericService pricingGenericService;
  protected AppBaseService appBaseService;

  @Inject
  public PricingSupplychainServiceImpl(
      PricingGenericService pricingGenericService, AppBaseService appBaseService) {
    this.pricingGenericService = pricingGenericService;
    this.appBaseService = appBaseService;
  }

  @Override
  public Model computeFiscalPositionPricing(Model model, Company company) throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    if (appBase != null
        && appBase.getEnablePricingScale()
        && appBase.getUsePricingForFiscalPosition()) {
      Map<String, Object> contextMap = new HashMap<>();
      contextMap.put("company", company);

      pricingGenericService.computePricingProcess(
          company,
          model,
          PricingRepository.PRICING_TYPE_SELECT_FISCAL_POSITION_PRICING,
          contextMap);
    }

    return model;
  }
}
