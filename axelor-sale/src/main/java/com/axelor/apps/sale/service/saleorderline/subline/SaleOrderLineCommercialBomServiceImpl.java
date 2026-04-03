/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorderline.subline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CommercialBom;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineOnProductChangeService;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineCommercialBomServiceImpl implements SaleOrderLineCommercialBomService {

  protected final SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService;
  protected final SaleOrderLineComputeService saleOrderLineComputeService;
  protected final AppSaleService appSaleService;

  @Inject
  public SaleOrderLineCommercialBomServiceImpl(
      SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      AppSaleService appSaleService) {
    this.saleOrderLineOnProductChangeService = saleOrderLineOnProductChangeService;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
    this.appSaleService = appSaleService;
  }

  @Override
  public List<SaleOrderLine> createSubLinesFromCommercialBom(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    if (appSaleService.getAppSale().getListDisplayTypeSelect()
        != AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI) {
      return new ArrayList<>();
    }
    if (saleOrderLine.getProduct() == null
        || CollectionUtils.isEmpty(saleOrderLine.getProduct().getCommercialBomList())) {
      return new ArrayList<>();
    }
    return mapBomListToSubLines(
        saleOrderLine.getProduct().getCommercialBomList(), saleOrderLine, saleOrder);
  }

  protected List<SaleOrderLine> mapBomListToSubLines(
      List<CommercialBom> bomList, SaleOrderLine parentLine, SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> subLines = new ArrayList<>();
    for (CommercialBom bom : bomList) {
      SaleOrderLine subLine = new SaleOrderLine();
      subLine.setProduct(bom.getProduct());
      subLine.setParentSaleOrderLine(parentLine);

      // Fires the SaleOrderLineProductOnChange CDI event, triggering all observers including
      // supplychain (managedInStockMove, saleSupplySelect, analyticMap, etc.)
      saleOrderLineOnProductChangeService.computeLineFromProduct(saleOrder, subLine);

      subLine.setQty(bom.getQty());
      subLine.setUnit(bom.getUnit() != null ? bom.getUnit() : bom.getProduct().getUnit());

      // Equivalent of qty onChange: recalculates prices based on final qty/unit
      saleOrderLineComputeService.computeValues(saleOrder, subLine);

      if (CollectionUtils.isNotEmpty(bom.getCommercialBomList())) {
        subLine.setSubSaleOrderLineList(
            mapBomListToSubLines(bom.getCommercialBomList(), subLine, saleOrder));
      }
      subLines.add(subLine);
    }
    return subLines;
  }
}
