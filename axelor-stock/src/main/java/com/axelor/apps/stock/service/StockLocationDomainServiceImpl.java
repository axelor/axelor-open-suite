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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;

public class StockLocationDomainServiceImpl implements StockLocationDomainService {
  @Override
  public String getSiteDomain(StockLocation stockLocation) {
    StringBuilder domain = new StringBuilder();
    int typeSelect = stockLocation.getTypeSelect();
    domain.append(String.format("self.typeSelect = %d", typeSelect));

    Company company = stockLocation.getCompany();
    if (company != null) {
      domain.append(String.format(" AND self.company = %d", company.getId()));
    }

    Partner partner = stockLocation.getPartner();
    if (partner != null && typeSelect == StockLocationRepository.TYPE_EXTERNAL) {
      domain.append(String.format(" AND self.partner = %d", partner.getId()));
    }

    TradingName tradingName = stockLocation.getTradingName();
    if (tradingName != null) {
      domain.append(String.format(" AND self.tradingName = %d", tradingName.getId()));
    }
    return domain.toString();
  }
}
