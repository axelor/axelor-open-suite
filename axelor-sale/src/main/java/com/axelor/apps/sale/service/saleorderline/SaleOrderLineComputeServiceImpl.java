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
package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.MarginComputeService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderLineComputeServiceImpl implements SaleOrderLineComputeService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected TaxService taxService;
  protected CurrencyScaleService currencyScaleService;
  protected ProductCompanyService productCompanyService;
  protected MarginComputeService marginComputeService;
  protected CurrencyService currencyService;
  protected PriceListService priceListService;
  protected SaleOrderLinePackService saleOrderLinePackService;
  protected SaleOrderLineCostPriceComputeService saleOrderLineCostPriceComputeService;

  @Inject
  public SaleOrderLineComputeServiceImpl(
      TaxService taxService,
      CurrencyScaleService currencyScaleService,
      ProductCompanyService productCompanyService,
      MarginComputeService marginComputeService,
      CurrencyService currencyService,
      PriceListService priceListService,
      SaleOrderLinePackService saleOrderLinePackService,
      SaleOrderLineCostPriceComputeService saleOrderLineCostPriceComputeService) {
    this.taxService = taxService;
    this.currencyScaleService = currencyScaleService;
    this.productCompanyService = productCompanyService;
    this.marginComputeService = marginComputeService;
    this.currencyService = currencyService;
    this.priceListService = priceListService;
    this.saleOrderLinePackService = saleOrderLinePackService;
    this.saleOrderLineCostPriceComputeService = saleOrderLineCostPriceComputeService;
  }

  @Override
  public Map<String, Object> computeValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    HashMap<String, Object> map = new HashMap<>();
    if (saleOrder == null
        || saleOrderLine.getPrice() == null
        || saleOrderLine.getInTaxPrice() == null
        || saleOrderLine.getQty() == null) {
      return map;
    }

    BigDecimal exTaxTotal;
    BigDecimal companyExTaxTotal;
    BigDecimal inTaxTotal;
    BigDecimal companyInTaxTotal;
    BigDecimal priceDiscounted = this.computeDiscount(saleOrderLine, saleOrder.getInAti());
    BigDecimal taxRate = BigDecimal.ZERO;

    if (CollectionUtils.isNotEmpty(saleOrderLine.getTaxLineSet())) {
      taxRate = taxService.getTotalTaxRate(saleOrderLine.getTaxLineSet());
    }

    if (!saleOrder.getInAti()) {
      exTaxTotal =
          this.computeAmount(
              saleOrderLine.getQty(), priceDiscounted, currencyScaleService.getScale(saleOrder));
      inTaxTotal =
          currencyScaleService.getScaledValue(
              saleOrder, exTaxTotal.add(exTaxTotal.multiply(taxRate)));
      companyExTaxTotal = this.getAmountInCompanyCurrency(exTaxTotal, saleOrder);
      companyInTaxTotal =
          currencyScaleService.getCompanyScaledValue(
              saleOrder, companyExTaxTotal.add(companyExTaxTotal.multiply(taxRate)));
    } else {
      inTaxTotal =
          this.computeAmount(
              saleOrderLine.getQty(), priceDiscounted, currencyScaleService.getScale(saleOrder));
      exTaxTotal =
          inTaxTotal.divide(
              taxRate.add(BigDecimal.ONE),
              currencyScaleService.getScale(saleOrder),
              RoundingMode.HALF_UP);
      companyInTaxTotal = this.getAmountInCompanyCurrency(inTaxTotal, saleOrder);
      companyExTaxTotal =
          companyInTaxTotal.divide(
              taxRate.add(BigDecimal.ONE),
              currencyScaleService.getCompanyScale(saleOrder),
              RoundingMode.HALF_UP);
    }

    Product product = saleOrderLine.getProduct();
    map.putAll(
        saleOrderLineCostPriceComputeService.computeSubTotalCostPrice(
            saleOrder, saleOrderLine, product));
    map.putAll(setProductIconType(saleOrderLine, product));

    saleOrderLine.setInTaxTotal(inTaxTotal);
    saleOrderLine.setExTaxTotal(exTaxTotal);
    saleOrderLine.setPriceDiscounted(priceDiscounted);
    saleOrderLine.setCompanyInTaxTotal(companyInTaxTotal);
    saleOrderLine.setCompanyExTaxTotal(companyExTaxTotal);
    map.put("inTaxTotal", inTaxTotal);
    map.put("exTaxTotal", exTaxTotal);
    map.put("priceDiscounted", priceDiscounted);
    map.put("companyExTaxTotal", companyExTaxTotal);
    map.put("companyInTaxTotal", companyInTaxTotal);

    map.putAll(
        marginComputeService.getComputedMarginInfo(
            saleOrder, saleOrderLine, saleOrderLine.getExTaxTotal()));

    return map;
  }

  protected Map<String, Object> setProductIconType(SaleOrderLine saleOrderLine, Product product) {
    Map<String, Object> map = new HashMap<>();
    String iconTypeSelect = getIconTypeSelect(product);

    if (!iconTypeSelect.isEmpty()) {
      saleOrderLine.setProductTypeIconSelect(iconTypeSelect);
      map.put("productTypeIconSelect", iconTypeSelect);
    }
    return map;
  }

  protected String getIconTypeSelect(Product product) {
    if (product != null) {
      if (ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect())) {
        return SaleOrderLineRepository.SALE_ORDER_LINE_PRODUCT_TYPE_SERVICE;
      } else {
        return getIconTypeSelect(product.getProductSubTypeSelect());
      }
    }
    return "";
  }

  protected String getIconTypeSelect(int productSubTypeSelect) {
    switch (productSubTypeSelect) {
      case ProductRepository.PRODUCT_SUB_TYPE_FINISHED_PRODUCT:
        return SaleOrderLineRepository.SALE_ORDER_LINE_PRODUCT_TYPE_FINISHED_PRODUCT;
      case ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT:
        return SaleOrderLineRepository.SALE_ORDER_LINE_PRODUCT_TYPE_SEMI_FINISH_PRODUCT;
      case ProductRepository.PRODUCT_SUB_TYPE_COMPONENT:
        return SaleOrderLineRepository.SALE_ORDER_LINE_PRODUCT_TYPE_COMPONENT;
      default:
        return "";
    }
  }

  protected BigDecimal computeAmount(BigDecimal quantity, BigDecimal price, int scale) {

    BigDecimal amount = quantity.multiply(price).setScale(scale, RoundingMode.HALF_UP);

    logger.debug(
        "Computation of W.T. amount with a quantity of {} for {} : {}", quantity, price, amount);

    return amount;
  }

  @Override
  public BigDecimal computeDiscount(SaleOrderLine saleOrderLine, Boolean inAti) {

    BigDecimal price = inAti ? saleOrderLine.getInTaxPrice() : saleOrderLine.getPrice();

    return priceListService.computeDiscount(
        price, saleOrderLine.getDiscountTypeSelect(), saleOrderLine.getDiscountAmount());
  }

  @Override
  public BigDecimal getAmountInCompanyCurrency(BigDecimal exTaxTotal, SaleOrder saleOrder)
      throws AxelorException {
    Company company = saleOrder.getCompany();

    return currencyScaleService.getCompanyScaledValue(
        saleOrder,
        currencyService.getAmountCurrencyConvertedAtDate(
            saleOrder.getCurrency(),
            company != null ? company.getCurrency() : null,
            exTaxTotal,
            saleOrder.getCreationDate()));
  }

  @Override
  public Map<String, Object> updateProductQty(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, BigDecimal oldQty, BigDecimal newQty)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    if (saleOrderLine.getTypeSelect() != SaleOrderLineRepository.TYPE_NORMAL) {
      return saleOrderLineMap;
    }
    saleOrderLinePackService.fillPriceFromPackLine(saleOrderLine, saleOrder);
    saleOrderLineMap.putAll(computeValues(saleOrder, saleOrderLine));
    return saleOrderLineMap;
  }

  @Override
  public void computeLevels(List<SaleOrderLine> saleOrderLineList, String parentLevel) {
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return;
    }
    int count = 1;
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      String levelIndicator =
          StringUtils.isBlank(parentLevel)
              ? String.valueOf(count)
              : String.format("%s.%s", parentLevel, count);
      saleOrderLine.setLevelIndicator(levelIndicator);
      count++;
      computeLevels(saleOrderLine.getSubSaleOrderLineList(), levelIndicator);
    }
  }
}
