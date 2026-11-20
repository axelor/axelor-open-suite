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
package com.axelor.apps.account.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxRateServiceImpl;
import com.axelor.apps.base.service.tax.TaxService;
import jakarta.inject.Inject;

public class TaxRateServiceAccountImpl extends TaxRateServiceImpl {

  protected TaxAccountService taxAccountService;
  protected final AppBaseService appBaseService;

  @Inject
  public TaxRateServiceAccountImpl(
      TaxService taxService, TaxAccountService taxAccountService, AppBaseService appBaseService) {
    super(taxService);
    this.taxAccountService = taxAccountService;
    this.appBaseService = appBaseService;
  }

  @Override
  protected TaxService getTaxService() {
    if (!appBaseService.isApp("account")) {
      return super.getTaxService();
    }
    return taxAccountService;
  }
}
