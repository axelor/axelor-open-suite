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
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineTaxService;
import com.axelor.inject.Beans;
import java.util.Map;

public class ImportSaleOrderLine {

  public Object importSaleOrderLine(Object bean, Map<String, Object> values)
      throws AxelorException {
    assert bean instanceof SaleOrderLine;

    SaleOrderLine saleOrderLine = (SaleOrderLine) bean;
    SaleOrderLineTaxService saleOrderLineTaxService = Beans.get(SaleOrderLineTaxService.class);
    SaleOrder saleOrder = saleOrderLine.getSaleOrder();
    if (saleOrder == null) {
      saleOrder = getSaleOrder(saleOrderLine);
    }
    saleOrderLine.setTaxLineSet(saleOrderLineTaxService.getTaxLineSet(saleOrder, saleOrderLine));
    Beans.get(SaleOrderLineComputeService.class).computeValues(saleOrder, saleOrderLine);

    return saleOrderLine;
  }

  protected SaleOrder getSaleOrder(SaleOrderLine saleOrderLine) {
    SaleOrderLine parentSaleOrderLine = saleOrderLine.getParentSaleOrderLine();
    if (parentSaleOrderLine != null) {
      return getSaleOrder(parentSaleOrderLine);
    }
    if (saleOrderLine.getSaleOrder() != null) {
      return saleOrderLine.getSaleOrder();
    }
    return null;
  }
}
