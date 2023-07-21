/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.PartnerServiceImpl;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PartnerSaleServiceImpl extends PartnerServiceImpl implements PartnerSaleService {

  @Inject
  public PartnerSaleServiceImpl(PartnerRepository partnerRepo, AppBaseService appBaseService) {
    super(partnerRepo, appBaseService);
  }

  @Deprecated
  @Override
  public List<Long> findPartnerMails(Partner partner, int emailType) {
    if (!Beans.get(AppSaleService.class).isApp("sale")) {
      return super.findPartnerMails(partner, emailType);
    }

    List<Long> idList = new ArrayList<Long>();

    idList.addAll(this.findMailsFromPartner(partner, emailType));

    if (partner.getIsContact()) {
      idList.addAll(this.findMailsFromSaleOrderContact(partner, emailType));
      return idList;
    } else {
      idList.addAll(this.findMailsFromSaleOrder(partner, emailType));
    }

    Set<Partner> contactSet = partner.getContactPartnerSet();
    if (contactSet != null && !contactSet.isEmpty()) {
      for (Partner contact : contactSet) {
        idList.addAll(this.findMailsFromPartner(contact, emailType));
        idList.addAll(this.findMailsFromSaleOrderContact(contact, emailType));
      }
    }
    return idList;
  }

  @Deprecated
  @SuppressWarnings("unchecked")
  public List<Long> findMailsFromSaleOrder(Partner partner, int emailType) {

    String query =
        "SELECT DISTINCT(email.id) FROM Message as email, SaleOrder as so, Partner as part"
            + " WHERE part.id = "
            + partner.getId()
            + " AND so.clientPartner = part.id AND email.mediaTypeSelect = 2 AND "
            + "email IN (SELECT message FROM MultiRelated as related WHERE related.relatedToSelect = 'com.axelor.apps.sale.db.SaleOrder' AND related.relatedToSelectId = so.id)"
            + " AND email.typeSelect = "
            + emailType;

    return JPA.em().createQuery(query).getResultList();
  }

  @Deprecated
  @SuppressWarnings("unchecked")
  public List<Long> findMailsFromSaleOrderContact(Partner partner, int emailType) {

    String query =
        "SELECT DISTINCT(email.id) FROM Message as email, SaleOrder as so, Partner as part"
            + " WHERE part.id = "
            + partner.getId()
            + " AND so.contactPartner = part.id AND email.mediaTypeSelect = 2 AND "
            + "email IN (SELECT message FROM MultiRelated as related WHERE related.relatedToSelect = 'com.axelor.apps.sale.db.SaleOrder' AND related.relatedToSelectId = so.id)"
            + " AND email.typeSelect = "
            + emailType;

    return JPA.em().createQuery(query).getResultList();
  }

  public List<Product> getProductBoughtByCustomer(Partner customer) {
    String domain =
        "self.id in (SELECT line.product"
            + " FROM SaleOrderLine line join SaleOrder sale on line.saleOrder = sale.id"
            + " WHERE sale.statusSelect IN ("
            + SaleOrderRepository.STATUS_ORDER_CONFIRMED
            + ", "
            + SaleOrderRepository.STATUS_ORDER_COMPLETED
            + ")"
            + " AND sale.clientPartner = "
            + customer.getId()
            + ")";

    ProductRepository productRepo = Beans.get(ProductRepository.class);
    List<Product> productList = productRepo.all().filter(domain).fetch();

    return productList;
  }

  public HashMap<String, BigDecimal> getTotalSaleQuantityAndPrice(
      Partner customer, Product product) {

    String qtySelection = "SELECT SUM(line.qty)";
    String priceSelection = "SELECT SUM(line.subTotalCostPrice)";
    String endQuery =
        " FROM SaleOrderLine line join SaleOrder sale on line.saleOrder = sale.id"
            + " WHERE sale.statusSelect IN ("
            + SaleOrderRepository.STATUS_ORDER_CONFIRMED
            + ", "
            + SaleOrderRepository.STATUS_ORDER_COMPLETED
            + ")"
            + " AND sale.clientPartner = "
            + customer.getId()
            + " AND line.product = "
            + product.getId();

    BigDecimal qty =
        JPA.em().createQuery(qtySelection + endQuery, BigDecimal.class).getSingleResult();
    BigDecimal price =
        JPA.em().createQuery(priceSelection + endQuery, BigDecimal.class).getSingleResult();

    HashMap<String, BigDecimal> qtyAndPrice = new HashMap<String, BigDecimal>();
    qtyAndPrice.put("qty", qty);
    qtyAndPrice.put("price", price);

    return qtyAndPrice;
  }

  public List<Map<String, Object>> averageByCustomer(
      String averageOn, String fromDate, String toDate) {

    List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();

    String averageSelection = "SELECT AVG(sale." + averageOn + ")";
    String customerSelection = "SELECT sale.clientPartner.name";
    String endQuery =
        " FROM SaleOrder sale"
            + " WHERE sale.statusSelect IN ("
            + SaleOrderRepository.STATUS_ORDER_CONFIRMED
            + ", "
            + SaleOrderRepository.STATUS_ORDER_COMPLETED
            + ")"
            + " AND sale.confirmationDateTime BETWEEN to_date('"
            + fromDate
            + "', 'YYYY-MM-DD') AND to_date('"
            + toDate
            + "', 'YYYY-MM-DD') + 1"
            + " GROUP BY sale.clientPartner.name"
            + " ORDER BY AVG("
            + averageOn
            + ") desc";

    List<Double> averageList =
        JPA.em().createQuery(averageSelection + endQuery, Double.class).getResultList();
    List<String> customerList =
        JPA.em().createQuery(customerSelection + endQuery, String.class).getResultList();

    for (int i = 0; i < averageList.size(); i++) {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("_average", (Object) averageList.get(i));
      dataMap.put("_customer", (Object) customerList.get(i));
      dataList.add(dataMap);
    }

    return dataList;
  }
}
