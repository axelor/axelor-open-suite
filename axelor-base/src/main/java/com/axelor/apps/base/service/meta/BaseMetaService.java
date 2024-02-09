/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.meta;

import com.axelor.apps.base.service.pricing.PricingMetaService;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.rpc.Response;
import com.axelor.studio.service.CustomMetaService;
import com.google.inject.Inject;

public class BaseMetaService extends CustomMetaService {

  protected PricingMetaService pricingMetaService;

  @Inject
  public BaseMetaService(UserRepository userRepo, PricingMetaService pricingMetaService) {
    super(userRepo);
    this.pricingMetaService = pricingMetaService;
  }

  @Override
  public Response findView(String model, String name, String type) {
    Response response = super.findView(model, name, type);

    response = pricingMetaService.managePricing(response, model);
    return response;
  }
}
