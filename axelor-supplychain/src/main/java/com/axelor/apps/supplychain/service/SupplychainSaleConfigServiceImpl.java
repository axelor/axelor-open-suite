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

import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.service.config.SaleConfigServiceImpl;
import com.axelor.db.JPA;
import com.google.inject.persist.Transactional;
import javax.persistence.Query;

public class SupplychainSaleConfigServiceImpl extends SaleConfigServiceImpl
    implements SupplychainSaleConfigService {

  @Transactional
  public void updateCustomerCredit(SaleConfig saleConfig) {
    Query update =
        JPA.em()
            .createQuery(
                "UPDATE AccountingSituation self SET self.acceptedCredit = :acceptedCredit WHERE self.company = :company AND self.partner.id IN (SELECT partner.id FROM Partner partner WHERE partner.isContact IS FALSE AND partner.isCustomer IS TRUE)");

    update.setParameter("acceptedCredit", saleConfig.getAcceptedCredit());
    update.setParameter("company", saleConfig.getCompany());
    update.executeUpdate();
  }
}
