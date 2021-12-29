/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class ProductPortalServiceImpl implements ProductPortalService {

  @Inject UserService userService;
  @Inject PartnerPriceListService partnerPriceListService;
  @Inject PriceListService priceListService;
  @Inject AccountManagementService accountManagementService;
  @Inject StockLocationLineRepository stockLocationLineRepository;
  @Inject StockLocationLineService stockLocationLineService;
  @Inject UnitConversionService unitConversionService;

  @Override
  public BigDecimal getAvailableQty(Product product, Company company, StockLocation stockLocation)
      throws AxelorException {
    if (product == null || product.getUnit() == null) {
      return BigDecimal.ZERO;
    }
    Long companyId = 0L;
    Long stockLocationId = 0L;
    if (company != null) {
      companyId = company.getId();
    }
    if (stockLocation != null) {
      stockLocationId = stockLocation.getId();
    }
    String query =
        stockLocationLineService.getAvailableStockForAProduct(
            product.getId(), companyId, stockLocationId);
    List<StockLocationLine> stockLocationLineList =
        stockLocationLineRepository.all().filter(query).fetch();

    // Compute
    BigDecimal sumAvailableQty = BigDecimal.ZERO;
    if (!stockLocationLineList.isEmpty()) {

      Unit unitConversion = product.getUnit();
      for (StockLocationLine stockLocationLine : stockLocationLineList) {
        BigDecimal productAvailableQty = stockLocationLine.getCurrentQty();
        unitConversionService.convert(
            stockLocationLine.getUnit(),
            unitConversion,
            productAvailableQty,
            productAvailableQty.scale(),
            product);
        sumAvailableQty = sumAvailableQty.add(productAvailableQty);
      }
    }
    return sumAvailableQty;
  }

  @Override
  public BigDecimal getUnitPrice(
      Product product, Currency targetCurrency, Company company, Boolean isAti)
      throws AxelorException {
    BigDecimal price = product.getSalePrice();
    Boolean isPriceAti = product.getInAti();
    if (!isAti.equals(isPriceAti)) {
      Partner userPartner = userService.getUserPartner();
      TaxLine taxLine = getTaxLine(product, company, userPartner);
      price = computeTax(price, isPriceAti, taxLine);
    }
    return convertAmount(product.getSaleCurrency(), targetCurrency, price)
        .setScale(2, RoundingMode.HALF_EVEN);
  }

  @Override
  public String getDiscountStr(Product product, Currency targetCurrency, Company company)
      throws AxelorException {
    PriceListLine priceListLine = getPriceListLine(product);
    String discountStr = null;
    if (priceListLine != null
        && priceListLine.getTypeSelect() != PriceListLineRepository.TYPE_REPLACE) {
      BigDecimal amount = priceListLine.getAmount().setScale(2, RoundingMode.HALF_EVEN);
      String negativeSign =
          PriceListLineRepository.TYPE_DISCOUNT == priceListLine.getTypeSelect() ? "-" : "";
      switch (priceListLine.getAmountTypeSelect()) {
        case PriceListLineRepository.AMOUNT_TYPE_PERCENT:
          discountStr = String.format("%s%s%%", negativeSign, amount);
          break;
        case PriceListLineRepository.AMOUNT_TYPE_FIXED:
          discountStr =
              String.format(
                  "%s%s",
                  negativeSign, convertAmount(product.getSaleCurrency(), targetCurrency, amount));
          break;
        default:
          break;
      }
    }
    return discountStr;
  }

  @Override
  public BigDecimal getUnitPriceDiscounted(
      Product product, Currency targetCurrency, Company company, Boolean isAti)
      throws AxelorException {
    PriceListLine priceListLine = getPriceListLine(product);
    BigDecimal amountDiscounted = product.getSalePrice();
    if (priceListLine != null) {
      amountDiscounted =
          priceListService.getUnitPriceDiscounted(priceListLine, product.getSalePrice());
    }

    Boolean isPriceAti = product.getInAti();
    if (!isAti.equals(isPriceAti)) {
      Partner userPartner = userService.getUserPartner();
      TaxLine taxLine = getTaxLine(product, company, userPartner);
      amountDiscounted = computeTax(amountDiscounted, isPriceAti, taxLine);
    }

    return convertAmount(product.getSaleCurrency(), targetCurrency, amountDiscounted)
        .setScale(2, RoundingMode.HALF_EVEN);
  }

  private PriceListLine getPriceListLine(Product product) {
    Partner userPartner = userService.getUserPartner();
    PriceList priceList =
        partnerPriceListService.getDefaultPriceList(userPartner, PriceListRepository.TYPE_SALE);
    return priceListService.getPriceListLine(
        product, BigDecimal.ZERO, priceList, product.getSalePrice());
  }

  private BigDecimal computeTax(BigDecimal price, Boolean isPriceAti, TaxLine taxLine) {
    if (isPriceAti) {
      price = price.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
    } else {
      price = price.add(price.multiply(taxLine.getValue()));
    }
    return price;
  }

  private TaxLine getTaxLine(Product product, Company company, Partner userPartner)
      throws AxelorException {
    TaxLine taxLine =
        accountManagementService.getTaxLine(
            LocalDate.now(), product, company, userPartner.getFiscalPosition(), false);
    if (taxLine == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("No tax line found for Partner (%s) and Company (%s)"),
          userPartner,
          company);
    }
    return taxLine;
  }

  private BigDecimal convertAmount(
      Currency sourceCurrency, Currency targetCurrency, BigDecimal amount) throws AxelorException {
    CurrencyService currencyService = Beans.get(CurrencyService.class);
    return currencyService.getAmountCurrencyConvertedAtDate(
        sourceCurrency, targetCurrency, amount, LocalDate.now());
  }
}
