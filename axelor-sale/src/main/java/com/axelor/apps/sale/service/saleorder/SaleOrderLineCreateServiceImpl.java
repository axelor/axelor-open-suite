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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.PackLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SaleOrderLineCreateServiceImpl implements SaleOrderLineCreateService {

  protected SaleOrderLineService saleOrderLineService;
  protected AppSaleService appSaleService;
  protected AppBaseService appBaseService;

  @Inject
  public SaleOrderLineCreateServiceImpl(
      SaleOrderLineService saleOrderLineService,
      AppSaleService appSaleService,
      AppBaseService appBaseService) {
    this.saleOrderLineService = saleOrderLineService;
    this.appSaleService = appSaleService;
    this.appBaseService = appBaseService;
  }

  @Override
  public SaleOrderLine createSaleOrderLine(
      PackLine packLine,
      SaleOrder saleOrder,
      BigDecimal packQty,
      BigDecimal conversionRate,
      Integer sequence)
      throws AxelorException {

    if (packLine.getTypeSelect() == PackLineRepository.TYPE_START_OF_PACK
        || packLine.getTypeSelect() == PackLineRepository.TYPE_END_OF_PACK) {
      return saleOrderLineService.createStartOfPackAndEndOfPackTypeSaleOrderLine(
          packLine.getPack(), saleOrder, packQty, packLine, packLine.getTypeSelect(), sequence);
    }

    if (packLine.getProductName() != null) {
      SaleOrderLine soLine = new SaleOrderLine();

      Product product = packLine.getProduct();
      soLine.setProduct(product);
      soLine.setProductName(packLine.getProductName());
      if (packLine.getQuantity() != null) {
        soLine.setQty(
            packLine
                .getQuantity()
                .multiply(packQty)
                .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP));
      }
      soLine.setUnit(packLine.getUnit());
      soLine.setTypeSelect(packLine.getTypeSelect());
      soLine.setSequence(sequence);
      if (packLine.getPrice() != null) {
        soLine.setPrice(packLine.getPrice().multiply(conversionRate));
      }

      if (product != null) {
        if (appSaleService.getAppSale().getIsEnabledProductDescriptionCopy()) {
          soLine.setDescription(product.getDescription());
        }
        try {
          saleOrderLineService.fillPriceFromPackLine(soLine, saleOrder);
          saleOrderLineService.computeValues(saleOrder, soLine);
        } catch (AxelorException e) {
          TraceBackService.trace(e);
        }
      }
      return soLine;
    }
    return null;
  }
}
