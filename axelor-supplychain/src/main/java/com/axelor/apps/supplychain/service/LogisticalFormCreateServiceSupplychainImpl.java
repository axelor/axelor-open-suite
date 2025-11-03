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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.LogisticalFormRepository;
import com.axelor.apps.stock.service.LogisticalFormCreateServiceImpl;
import com.axelor.apps.stock.service.LogisticalFormService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class LogisticalFormCreateServiceSupplychainImpl extends LogisticalFormCreateServiceImpl {

  @Inject
  public LogisticalFormCreateServiceSupplychainImpl(
      AppBaseService appBaseService,
      LogisticalFormService logisticalFormService,
      LogisticalFormRepository logisticalFormRepository,
      StockConfigService stockConfigService) {
    super(appBaseService, logisticalFormService, logisticalFormRepository, stockConfigService);
  }

  protected void checkFields(
      Partner carrierPartner,
      Partner deliverToCustomerPartner,
      boolean isMultiClientEnabled,
      Company company,
      StockLocation stockLocation)
      throws AxelorException {
    super.checkFields(
        carrierPartner, deliverToCustomerPartner, isMultiClientEnabled, company, stockLocation);
    if (stockLocation != null && !stockLocation.getUsableOnSaleOrder()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              SupplychainExceptionMessage.LOGISTICAL_FORM_STOCK_LOCATION_MUST_BE_USABLE_ON_SO));
    }
  }
}
