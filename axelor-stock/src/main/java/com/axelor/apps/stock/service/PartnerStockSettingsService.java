/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.PartnerStockSettings;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.exception.AxelorException;

public interface PartnerStockSettingsService {

  /**
   * get the mail settings from the partner and the company, create if not found
   *
   * @param partner
   * @param company
   */
  PartnerStockSettings getOrCreateMailSettings(Partner partner, Company company)
      throws AxelorException;

  /**
   * Create PartnerStockSettings in the given partner
   *
   * @param partner
   * @param company
   */
  PartnerStockSettings createMailSettings(Partner partner, Company company) throws AxelorException;

  /**
   * get default stock location for given partner and company
   *
   * @param partner
   * @param company
   */
  public StockLocation getDefaultStockLocation(Partner partner, Company company);
}
