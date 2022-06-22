/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.base.db.repo.ProductBaseRepository;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductStockRepository extends ProductBaseRepository {

  @Inject protected StockMoveService stockMoveService;
  @Inject protected StockLocationRepository stockLocationRepo;
  @Inject protected StockLocationLineService stockLocationLineService;
  @Inject protected StockLocationService stockLocationService;
  @Inject protected WeightedAveragePriceService weightedAveragePriceService;

  public Product save(Product product) {
    addProductCompanies(product);
    return super.save(product);
  }

  protected void addProductCompanies(Product product) {
    Set<MetaField> specificProductFieldSet =
        appBaseService.getAppBase().getCompanySpecificProductFieldsSet();
    if (specificProductFieldSet.isEmpty()
        || !appBaseService.getAppBase().getEnableMultiCompany()
        || ObjectUtils.isEmpty(product.getProductCompanyList())) {
      return;
    }

    List<Company> productCompanies =
        product.getProductCompanyList().stream()
            .map(ProductCompany::getCompany)
            .collect(Collectors.toList());

    List<StockConfig> stockConfigList = Beans.get(StockConfigRepository.class).all().fetch();
    if (ObjectUtils.isEmpty(stockConfigList)) {
      return;
    }

    for (StockConfig stockConfig : stockConfigList) {
      Company company = stockConfig.getCompany();
      if (company != null
          && !productCompanies.contains(company)
          && stockConfig.getReceiptDefaultStockLocation() != null
          && (company.getArchived() == null || !company.getArchived())) {
        ProductCompany productCompany =
            createProductCompany(product, specificProductFieldSet, company);
        product.addProductCompanyListItem(productCompany);
      }
    }
  }

  protected ProductCompany createProductCompany(
      Product product, Set<MetaField> specificProductFieldSet, Company company) {
    Mapper mapper = Mapper.of(Product.class);

    ProductCompany productCompany = new ProductCompany();
    for (MetaField specificField : specificProductFieldSet) {
      mapper.set(
          productCompany, specificField.getName(), mapper.get(product, specificField.getName()));
    }

    // specific case for avgPrice per company
    productCompany.setAvgPrice(
        weightedAveragePriceService.computeAvgPriceForCompany(product, company));
    productCompany.setCompany(company);
    productCompany.setProduct(product);
    return productCompany;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    this.setAvailableQty(json, context);

    if (!context.containsKey("fromStockWizard")) {
      return json;
    }
    try {
      Long productId = (Long) json.get("id");
      Long locationId = Long.parseLong(context.get("locationId").toString());
      LocalDate fromDate = LocalDate.parse(context.get("stockFromDate").toString());
      LocalDate toDate = LocalDate.parse(context.get("stockToDate").toString());
      List<Map<String, Object>> stock =
          stockMoveService.getStockPerDate(locationId, productId, fromDate, toDate);

      if (stock != null && !stock.isEmpty()) {
        LocalDate minDate = null;
        LocalDate maxDate = null;
        BigDecimal minQty = BigDecimal.ZERO;
        BigDecimal maxQty = BigDecimal.ZERO;
        for (Map<String, Object> dateStock : stock) {
          LocalDate date = (LocalDate) dateStock.get("$date");
          BigDecimal qty = (BigDecimal) dateStock.get("$qty");
          if (minDate == null
              || qty.compareTo(minQty) < 0
              || qty.compareTo(minQty) == 0 && date.isAfter(minDate)) {
            minDate = date;
            minQty = qty;
          }
          if (maxDate == null
              || qty.compareTo(maxQty) > 0
              || qty.compareTo(maxQty) == 0 && date.isBefore(maxDate)) {
            maxDate = date;
            maxQty = qty;
          }
        }
        json.put("$stockMinDate", minDate);
        json.put("$stockMin", minQty);
        json.put("$stockMaxDate", maxDate);
        json.put("$stockMax", maxQty);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return json;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  protected void setAvailableQty(Map<String, Object> json, Map<String, Object> context) {
    try {
      Long productId = (Long) json.get("id");
      Product product = find(productId);

      if (context.get("_parent") != null) {
        Map<String, Object> _parent = (Map<String, Object>) context.get("_parent");

        StockLocation stockLocation = null;
        if (context.get("_model").toString().equals("com.axelor.apps.stock.db.StockMoveLine")) {
          if (_parent.get("fromStockLocation") != null) {
            stockLocation =
                stockLocationRepo.find(
                    Long.parseLong(((Map) _parent.get("fromStockLocation")).get("id").toString()));
          }
        } else {
          if (_parent.get("stockLocation") != null) {
            stockLocation =
                stockLocationRepo.find(
                    Long.parseLong(((Map) _parent.get("stockLocation")).get("id").toString()));
          }
        }

        if (stockLocation != null) {
          BigDecimal availableQty =
              stockLocationLineService.getAvailableQty(stockLocation, product);

          json.put("$availableQty", availableQty);
        }
      } else if (product.getParentProduct() != null) {
        json.put("$availableQty", stockLocationService.getRealQty(productId, null, null));
      }

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  @Override
  public Product copy(Product product, boolean deep) {
    Product copy = super.copy(product, deep);
    copy.setAvgPrice(BigDecimal.ZERO);
    return copy;
  }
}
