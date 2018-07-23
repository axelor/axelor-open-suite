/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceListService {

  @Inject private PriceListLineRepository priceListLineRepo;

  @Inject private PriceListRepository priceListRepo;

  @Inject protected GeneralService generalService;

  public PriceListLine getPriceListLine(Product product, BigDecimal qty, PriceList priceList) {

    PriceListLine priceListLine = null;

    if (product != null && priceList != null) {
      priceListLine =
          Beans.get(PriceListLineRepository.class)
              .all()
              .filter(
                  "self.product = ?1 AND self.minQty <= ?2 AND self.priceList.id = ?3 ORDER BY self.minQty DESC",
                  product,
                  qty,
                  priceList.getId())
              .fetchOne();
      if (priceListLine == null && product.getProductCategory() != null) {
        priceListLine =
            priceListLineRepo
                .all()
                .filter(
                    "self.productCategory = ?1 AND self.minQty <= ?2 AND self.priceList.id = ?3 ORDER BY self.minQty DESC",
                    product.getProductCategory(),
                    qty,
                    priceList.getId())
                .fetchOne();
      }
    }

    return priceListLine;
  }

  public int getDiscountTypeSelect(PriceListLine priceListLine) {

    return priceListLine.getAmountTypeSelect();
  }

  public BigDecimal getDiscountAmount(PriceListLine priceListLine, BigDecimal unitPrice) {

    switch (priceListLine.getTypeSelect()) {
      case IPriceListLine.TYPE_ADDITIONNAL:
        return priceListLine.getAmount().negate();

      case IPriceListLine.TYPE_DISCOUNT:
        return priceListLine.getAmount();

      case IPriceListLine.TYPE_REPLACE:
        return unitPrice.subtract(priceListLine.getAmount());

      default:
        return BigDecimal.ZERO;
    }
  }

  public BigDecimal getUnitPriceDiscounted(PriceListLine priceListLine, BigDecimal unitPrice) {

    switch (priceListLine.getTypeSelect()) {
      case IPriceListLine.TYPE_ADDITIONNAL:
        if (priceListLine.getAmountTypeSelect() == IPriceListLine.AMOUNT_TYPE_FIXED) {
          return unitPrice.add(priceListLine.getAmount());
        } else if (priceListLine.getAmountTypeSelect() == IPriceListLine.AMOUNT_TYPE_PERCENT) {
          return unitPrice.multiply(
              BigDecimal.ONE.add(priceListLine.getAmount().divide(new BigDecimal(100))));
        }

      case IPriceListLine.TYPE_DISCOUNT:
        if (priceListLine.getAmountTypeSelect() == IPriceListLine.AMOUNT_TYPE_FIXED) {
          return unitPrice.subtract(priceListLine.getAmount());
        } else if (priceListLine.getAmountTypeSelect() == IPriceListLine.AMOUNT_TYPE_PERCENT) {
          return unitPrice.multiply(
              BigDecimal.ONE.subtract(priceListLine.getAmount().divide(new BigDecimal(100))));
        }

      case IPriceListLine.TYPE_REPLACE:
        return priceListLine.getAmount();

      default:
        return unitPrice;
    }
  }

  public BigDecimal getUnitPriceDiscounted(PriceList priceList, BigDecimal unitPrice) {

    BigDecimal discountPercent = priceList.getGeneralDiscount();

    return unitPrice.multiply(BigDecimal.ONE.subtract(discountPercent.divide(new BigDecimal(100))));
  }

  public BigDecimal computeDiscount(
      BigDecimal unitPrice, int discountTypeSelect, BigDecimal discountAmount) {

    if (discountTypeSelect == IPriceListLine.AMOUNT_TYPE_FIXED) {
      return unitPrice
          .subtract(discountAmount)
          .setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    } else if (discountTypeSelect == IPriceListLine.AMOUNT_TYPE_PERCENT) {
      return unitPrice
          .multiply(
              BigDecimal.ONE.subtract(
                  discountAmount.divide(
                      new BigDecimal(100),
                      generalService.getNbDecimalDigitForUnitPrice(),
                      RoundingMode.HALF_UP)))
          .setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    }

    return unitPrice;
  }

  public Map<String, Object> getReplacedPriceAndDiscounts(
      PriceList priceList, PriceListLine priceListLine, BigDecimal price) {
    int discountTypeSelect = 0;

    if (priceListLine != null) {
      discountTypeSelect = priceListLine.getTypeSelect();
    }
    Map<String, Object> discounts = getDiscounts(priceList, priceListLine, price);
    if (discounts != null) {
      int computeMethodDiscountSelect =
          generalService.getGeneral().getComputeMethodDiscountSelect();
      if ((computeMethodDiscountSelect == GeneralRepository.INCLUDE_DISCOUNT_REPLACE_ONLY
              && discountTypeSelect == IPriceListLine.TYPE_REPLACE)
          || computeMethodDiscountSelect == GeneralRepository.INCLUDE_DISCOUNT) {

        price =
            computeDiscount(
                price,
                (int) discounts.get("discountTypeSelect"),
                (BigDecimal) discounts.get("discountAmount"));
        discounts.put("price", price);
        discounts.put("discountTypeSelect", IPriceListLine.AMOUNT_TYPE_NONE);
        discounts.put("discountAmount", BigDecimal.ZERO);
      }
    }
    return discounts;
  }

  public Map<String, Object> getDiscounts(
      PriceList priceList, PriceListLine priceListLine, BigDecimal price) {

    Map<String, Object> discounts = new HashMap<>();

    if (priceListLine != null) {
      discounts.put(
          "discountAmount",
          this.getDiscountAmount(priceListLine, price)
              .setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));
      discounts.put("discountTypeSelect", this.getDiscountTypeSelect(priceListLine));

    } else {
      BigDecimal discountAmount =
          priceList
              .getGeneralDiscount()
              .setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
      discounts.put("discountAmount", discountAmount);
      if (discountAmount.compareTo(BigDecimal.ZERO) == 0) {
        discounts.put("discountTypeSelect", IPriceListLine.AMOUNT_TYPE_NONE);
      } else {
        discounts.put("discountTypeSelect", IPriceListLine.AMOUNT_TYPE_PERCENT);
      }
    }

    return discounts;
  }

  @Transactional
  public PriceList historizePriceList(PriceList priceList) {
    PriceList historizedPriceList = priceListRepo.copy(priceList, false);
    historizedPriceList.setIsActive(false);
    List<PriceListLine> priceListLineList = priceList.getPriceListLineList();
    for (PriceListLine priceListLine : priceListLineList) {
      PriceListLine newPriceListLine = priceListLineRepo.copy(priceListLine, false);
      newPriceListLine.setPriceList(null);
      historizedPriceList.addPriceListLineListItem(newPriceListLine);
    }
    priceListRepo.save(historizedPriceList);
    priceList.addHistorizedPriceListItem(historizedPriceList);
    priceListRepo.save(priceList);
    return priceList;
  }
}
