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
package com.axelor.csv.script;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.util.Map;

public class ImportSaleOrderLine {

  public Object importSaleOrderLine(Object bean, Map<String, Object> values)
      throws AxelorException {
    assert bean instanceof SaleOrderLine;

    SaleOrderLine saleOrderLine = (SaleOrderLine) bean;
    SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
    saleOrderLine.setTaxLine(
        saleOrderLineService.getTaxLine(saleOrderLine.getSaleOrder(), saleOrderLine));
    saleOrderLineService.computeValues(saleOrderLine.getSaleOrder(), saleOrderLine);

    return saleOrderLine;
  }
}
