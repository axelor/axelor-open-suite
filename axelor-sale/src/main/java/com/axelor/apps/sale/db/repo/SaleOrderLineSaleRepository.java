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
package com.axelor.apps.sale.db.repo;

import com.axelor.apps.base.db.AppSale;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.google.inject.Inject;

public class SaleOrderLineSaleRepository extends SaleOrderLineRepository {

  @Inject private SaleOrderLineService solService;

  @Inject private AppSaleService appSaleService;

  @Override
  public SaleOrderLine save(SaleOrderLine soLine) {

    AppSale appSale = appSaleService.getAppSale();
    soLine = super.save(soLine);

    if (appSale.getActive() && appSale.getProductPackMgt()) {
      soLine.setTotalPack(solService.computeTotalPack(soLine));
    }

    return soLine;
  }
}
