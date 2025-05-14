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
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCategoryService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.MapUtils;

public class SaleOrderLineDiscountServiceImpl implements SaleOrderLineDiscountService {

  protected PriceListService priceListService;
  protected ProductCategoryService productCategoryService;
  protected SaleOrderLinePriceService saleOrderLinePriceService;
  protected TaxService taxService;
  protected AppBaseService appBaseService;

  @Inject
  public SaleOrderLineDiscountServiceImpl(
      PriceListService priceListService,
      ProductCategoryService productCategoryService,
      SaleOrderLinePriceService saleOrderLinePriceService,
      TaxService taxService,
      AppBaseService appBaseService) {
    this.priceListService = priceListService;
    this.productCategoryService = productCategoryService;
    this.saleOrderLinePriceService = saleOrderLinePriceService;
    this.taxService = taxService;
    this.appBaseService = appBaseService;
  }

  @Override
  public Map<String, Object> getDiscount(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();

    if (saleOrder == null || saleOrderLine.getProduct() == null) {
      return saleOrderLineMap;
    }

    if (saleOrderLine.getProduct().getInAti()) {
      saleOrderLineMap.putAll(
          getDiscountsFromPriceLists(
              saleOrder,
              saleOrderLine,
              saleOrderLinePriceService.getInTaxUnitPrice(
                  saleOrder, saleOrderLine, saleOrderLine.getTaxLineSet())));
    } else {
      saleOrderLineMap.putAll(
          getDiscountsFromPriceLists(
              saleOrder,
              saleOrderLine,
              saleOrderLinePriceService.getExTaxUnitPrice(
                  saleOrder, saleOrderLine, saleOrderLine.getTaxLineSet())));
    }

    if (saleOrderLineMap != null) {
      BigDecimal price = (BigDecimal) saleOrderLineMap.get("price");
      if (price != null
          && price.compareTo(
                  saleOrderLine.getProduct().getInAti()
                      ? saleOrderLine.getInTaxPrice()
                      : saleOrderLine.getPrice())
              != 0) {
        if (saleOrderLine.getProduct().getInAti()) {
          saleOrderLine.setInTaxPrice(price);
          saleOrderLineMap.put("inTaxPrice", saleOrderLine.getInTaxPrice());
          saleOrderLine.setPrice(
              taxService.convertUnitPrice(
                  true,
                  saleOrderLine.getTaxLineSet(),
                  price,
                  appBaseService.getNbDecimalDigitForUnitPrice()));
          saleOrderLineMap.put("price", saleOrderLine.getPrice());
        } else {
          saleOrderLine.setPrice(price);
          saleOrderLineMap.put("price", saleOrderLine.getPrice());
          saleOrderLine.setInTaxPrice(
              taxService.convertUnitPrice(
                  false,
                  saleOrderLine.getTaxLineSet(),
                  price,
                  appBaseService.getNbDecimalDigitForUnitPrice()));
          saleOrderLineMap.put("inTaxPrice", saleOrderLine.getInTaxPrice());
        }
      }

      if (!saleOrderLine.getProduct().getInAti().equals(saleOrder.getInAti())
          && (Integer) saleOrderLineMap.get("discountTypeSelect")
              != PriceListLineRepository.AMOUNT_TYPE_PERCENT) {
        saleOrderLine.setDiscountAmount(
            taxService.convertUnitPrice(
                saleOrderLine.getProduct().getInAti(),
                saleOrderLine.getTaxLineSet(),
                (BigDecimal) saleOrderLineMap.get("discountAmount"),
                appBaseService.getNbDecimalDigitForUnitPrice()));
        saleOrderLineMap.put("discountAmount", saleOrderLine.getDiscountAmount());
      }
    }
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> getDiscountsFromPriceLists(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, BigDecimal price) {

    Map<String, Object> discounts = new HashMap<>();

    PriceList priceList = saleOrder.getPriceList();

    if (priceList != null) {
      PriceListLine priceListLine = this.getPriceListLine(saleOrderLine, priceList, price);
      discounts = priceListService.getReplacedPriceAndDiscounts(priceList, priceListLine, price);
      saleOrderLine.setDiscountAmount(
          Optional.ofNullable((BigDecimal) discounts.get("discountAmount"))
              .orElse(saleOrderLine.getDiscountAmount()));
      saleOrderLine.setDiscountTypeSelect(
          Optional.ofNullable((Integer) discounts.get("discountTypeSelect"))
              .orElse(saleOrderLine.getDiscountTypeSelect()));
      if (saleOrder.getTemplate()) {
        Integer manualDiscountAmountType = saleOrderLine.getDiscountTypeSelect();
        BigDecimal manualDiscountAmount = saleOrderLine.getDiscountAmount();
        Integer priceListDiscountAmountType = (Integer) discounts.get("discountTypeSelect");
        BigDecimal priceListDiscountAmount = (BigDecimal) discounts.get("discountAmount");

        if (!manualDiscountAmountType.equals(priceListDiscountAmountType)
            && manualDiscountAmountType.equals(PriceListLineRepository.AMOUNT_TYPE_PERCENT)
            && priceListDiscountAmountType.equals(PriceListLineRepository.AMOUNT_TYPE_FIXED)) {
          priceListDiscountAmount =
              priceListDiscountAmount
                  .multiply(new BigDecimal(100))
                  .divide(price, 2, RoundingMode.HALF_UP);
        } else if (!manualDiscountAmountType.equals(priceListDiscountAmountType)
            && manualDiscountAmountType.equals(PriceListLineRepository.AMOUNT_TYPE_FIXED)
            && priceListDiscountAmountType.equals(PriceListLineRepository.AMOUNT_TYPE_PERCENT)) {
          priceListDiscountAmount =
              priceListDiscountAmount
                  .multiply(price)
                  .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        }

        if (manualDiscountAmount.compareTo(priceListDiscountAmount) > 0) {
          discounts.put("discountAmount", manualDiscountAmount);
          discounts.put("discountTypeSelect", manualDiscountAmountType);
        }
      }
    } else {
      discounts.put("discountTypeSelect", saleOrderLine.getDiscountTypeSelect());
      discounts.put("discountAmount", saleOrderLine.getDiscountAmount());
    }

    return discounts;
  }

  @Override
  public BigDecimal computeMaxDiscount(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Optional<BigDecimal> maxDiscount = Optional.empty();
    Product product = saleOrderLine.getProduct();
    if (product != null && product.getProductCategory() != null) {
      maxDiscount = productCategoryService.computeMaxDiscount(product.getProductCategory());
    }
    if (!maxDiscount.isPresent()
        || saleOrderLine.getDiscountTypeSelect() == PriceListLineRepository.AMOUNT_TYPE_NONE
        || saleOrder == null
        || (saleOrder.getStatusSelect() != SaleOrderRepository.STATUS_DRAFT_QUOTATION
            && (saleOrder.getStatusSelect() != SaleOrderRepository.STATUS_ORDER_CONFIRMED
                || !saleOrder.getOrderBeingEdited()))) {
      return null;
    } else {
      return maxDiscount.get();
    }
  }

  @Override
  public boolean isSaleOrderLineDiscountGreaterThanMaxDiscount(
      SaleOrderLine saleOrderLine, BigDecimal maxDiscount) {
    return (saleOrderLine.getDiscountTypeSelect() == PriceListLineRepository.AMOUNT_TYPE_PERCENT
            && saleOrderLine.getDiscountAmount().compareTo(maxDiscount) > 0)
        || (saleOrderLine.getDiscountTypeSelect() == PriceListLineRepository.AMOUNT_TYPE_FIXED
            && saleOrderLine.getPrice().signum() != 0
            && saleOrderLine
                    .getDiscountAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(saleOrderLine.getPrice(), 2, RoundingMode.HALF_UP)
                    .compareTo(maxDiscount)
                > 0);
  }

  protected PriceListLine getPriceListLine(
      SaleOrderLine saleOrderLine, PriceList priceList, BigDecimal price) {

    return priceListService.getPriceListLine(
        saleOrderLine.getProduct(), saleOrderLine.getQty(), priceList, price);
  }

  @Override
  public Map<String, Object> fillDiscount(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, BigDecimal price) {

    Map<String, Object> discounts = getDiscountsFromPriceLists(saleOrder, saleOrderLine, price);

    Map<String, Object> saleOrderLineMap = new HashMap<>();

    if (MapUtils.isNotEmpty(discounts)) {
      if (!saleOrderLine.getProduct().getInAti().equals(saleOrder.getInAti())
          && (Integer) discounts.get("discountTypeSelect")
              != PriceListLineRepository.AMOUNT_TYPE_PERCENT) {
        saleOrderLine.setDiscountAmount(
            taxService.convertUnitPrice(
                saleOrderLine.getProduct().getInAti(),
                saleOrderLine.getTaxLineSet(),
                (BigDecimal) discounts.get("discountAmount"),
                appBaseService.getNbDecimalDigitForUnitPrice()));
      } else {
        saleOrderLine.setDiscountAmount((BigDecimal) discounts.get("discountAmount"));
      }
      saleOrderLine.setDiscountTypeSelect((Integer) discounts.get("discountTypeSelect"));
    } else if (!saleOrder.getTemplate()) {
      saleOrderLine.setDiscountAmount(BigDecimal.ZERO);
      saleOrderLine.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_NONE);
    }

    saleOrderLineMap.put("discountAmount", saleOrderLine.getDiscountAmount());
    saleOrderLineMap.put("discountTypeSelect", saleOrderLine.getDiscountTypeSelect());
    return saleOrderLineMap;
  }

  @Override
  public BigDecimal getDiscountedPrice(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, BigDecimal price) {
    Map<String, Object> discounts = getDiscountsFromPriceLists(saleOrder, saleOrderLine, price);
    if (discounts != null && (discounts.get("price") != null)) {
      price = (BigDecimal) discounts.get("price");
    }
    return price;
  }
}
