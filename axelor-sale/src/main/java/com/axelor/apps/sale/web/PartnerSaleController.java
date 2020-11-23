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
package com.axelor.apps.sale.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.service.PartnerSaleService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

@Singleton
public class PartnerSaleController {

  public void displayValues(ActionRequest request, ActionResponse response) {
    Partner customer = request.getContext().asType(Partner.class);

    try {

      customer = Beans.get(PartnerRepository.class).find(customer.getId());

      SortedSet<Map<String, Object>> saleDetailsByProduct =
          new TreeSet<Map<String, Object>>(Comparator.comparing(m -> (String) m.get("name")));

      PartnerSaleService partnerSaleService = Beans.get(PartnerSaleService.class);
      List<Product> productList = partnerSaleService.getProductBoughtByCustomer(customer);

      if (productList.isEmpty()) {
        response.setAttr("$saleDetailsByProduct", "hidden", true);
        return;
      }

      response.setAttr("$saleDetailsByProduct", "hidden", false);

      HashMap<String, BigDecimal> qtyAndPrice;

      for (Product product : productList) {
        qtyAndPrice = partnerSaleService.getTotalSaleQuantityAndPrice(customer, product);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", product.getName());
        map.put("$quantitySold", qtyAndPrice.get("qty"));
        map.put("$totalPrice", qtyAndPrice.get("price"));
        map.put(
            "$averagePrice",
            qtyAndPrice
                .get("price")
                .divide(
                    qtyAndPrice.get("qty"),
                    AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                    RoundingMode.HALF_EVEN));
        saleDetailsByProduct.add(map);
      }

      response.setValue("$saleDetailsByProduct", saleDetailsByProduct);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void averageByCustomer(
      String averageElement, ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    try {

      String fromDate = (String) context.get("fromDate");
      String toDate = (String) context.get("toDate");

      List<Map<String, Object>> dataList =
          Beans.get(PartnerSaleService.class).averageByCustomer(averageElement, fromDate, toDate);
      response.setData(dataList);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void marginRateByCustomer(ActionRequest request, ActionResponse response) {
    this.averageByCustomer("marginRate", request, response);
  }

  public void markupByCustomer(ActionRequest request, ActionResponse response) {
    this.averageByCustomer("markup", request, response);
  }
}
