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
package com.axelor.apps.purchase.service.config;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseConfig;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.i18n.I18n;

public class PurchaseConfigService {

  public PurchaseConfig getPurchaseConfig(Company company) throws AxelorException {

    PurchaseConfig purchaseConfig = company.getPurchaseConfig();

    if (purchaseConfig == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PurchaseExceptionMessage.PURCHASE_CONFIG_1),
          company.getName());
    }

    return purchaseConfig;
  }
}
