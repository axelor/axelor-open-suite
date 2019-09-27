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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.businessproject.db.ElementsToInvoice;
import com.axelor.apps.project.db.Project;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.Map;

@Singleton
public class ElementsToInvoiceController {

  public void getProductInformation(ActionRequest request, ActionResponse response) {
    ElementsToInvoice elementToInvoice = request.getContext().asType(ElementsToInvoice.class);
    Project project = elementToInvoice.getProject();
    if (project == null) {
      project = request.getContext().getParent().asType(Project.class);
    }
    Product product = elementToInvoice.getProduct();
    if (project != null && product != null) {
      elementToInvoice.setCostPrice(product.getCostPrice());
      elementToInvoice.setUnit(product.getUnit());
      BigDecimal price = product.getSalePrice();
      if (project.getClientPartner() != null) {
        PriceList priceList =
            Beans.get(PartnerPriceListService.class)
                .getDefaultPriceList(project.getClientPartner(), PriceListRepository.TYPE_SALE);
        if (priceList != null) {

          PriceListService priceListService = Beans.get(PriceListService.class);

          PriceListLine priceListLine =
              priceListService.getPriceListLine(product, elementToInvoice.getQty(), priceList);

          Map<String, Object> discounts =
              priceListService.getDiscounts(priceList, priceListLine, price);
          if (discounts != null) {
            price =
                priceListService.computeDiscount(
                    price,
                    (int) discounts.get("discountTypeSelect"),
                    (BigDecimal) discounts.get("discountAmount"));
          }
        }
      }
      elementToInvoice.setSalePrice(price);
    }
    response.setValues(elementToInvoice);
  }
}
