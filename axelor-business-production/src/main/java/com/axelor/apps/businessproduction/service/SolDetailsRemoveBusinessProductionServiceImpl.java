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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.google.inject.Inject;

public class SolDetailsRemoveBusinessProductionServiceImpl
    implements SolDetailsRemoveBusinessProductionService {

  protected final SaleOrderLineDetailsRepository saleOrderLineDetailsRepository;

  @Inject
  public SolDetailsRemoveBusinessProductionServiceImpl(
      SaleOrderLineDetailsRepository saleOrderLineDetailsRepository) {
    this.saleOrderLineDetailsRepository = saleOrderLineDetailsRepository;
  }

  @Override
  public void removeSaleOrderLineDetails(SaleOrderLineDetails saleOrderLineDetails) {
    SaleOrderLineDetails originSaleOrderLineDetails =
        saleOrderLineDetails.getOriginSaleOrderLineDetails();
    if (originSaleOrderLineDetails != null) {
      originSaleOrderLineDetails.setBillOfMaterialLine(null);
    }
    if (saleOrderLineDetails.getSaleOrderLine() != null) {
      SaleOrderLineDetails linkedSolDetails =
          saleOrderLineDetailsRepository
              .all()
              .autoFlush(false)
              .filter("self.originSaleOrderLineDetails = :originSolDetails")
              .bind("originSolDetails", saleOrderLineDetails)
              .fetchOne();
      if (linkedSolDetails != null) {
        linkedSolDetails.setOriginSaleOrderLineDetails(null);
      }
    }
  }
}
