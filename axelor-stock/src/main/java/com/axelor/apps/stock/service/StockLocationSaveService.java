/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.apps.stock.db.repo.PartnerStockSettingsRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.util.List;

public class StockLocationSaveService {

  /**
   * Remove default stock locations in partner that are not linked with this stock location anymore.
   *
   * @param defaultStockLocation
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void removeForbiddenDefaultStockLocation(StockLocation defaultStockLocation) {
    Partner currentPartner = defaultStockLocation.getPartner();
    Company currentCompany = defaultStockLocation.getCompany();
    Long partnerId = currentPartner != null ? currentPartner.getId() : 0L;
    Long companyId = currentCompany != null ? currentCompany.getId() : 0L;
    PartnerStockSettingsRepository partnerStockSettingsRepo =
        Beans.get(PartnerStockSettingsRepository.class);
    List<PartnerStockSettings> partnerStockSettingsToRemove =
        partnerStockSettingsRepo
            .all()
            .filter(
                "(self.partner.id != :partnerId OR self.company.id != :companyId)"
                    + " AND (self.defaultStockLocation.id = :stockLocationId)")
            .bind("partnerId", partnerId)
            .bind("companyId", companyId)
            .bind("stockLocationId", defaultStockLocation.getId())
            .fetch();
    for (PartnerStockSettings partnerStockSettings : partnerStockSettingsToRemove) {
      Partner partnerToClean = partnerStockSettings.getPartner();
      partnerToClean.removePartnerStockSettingsListItem(partnerStockSettings);
    }
  }
}
