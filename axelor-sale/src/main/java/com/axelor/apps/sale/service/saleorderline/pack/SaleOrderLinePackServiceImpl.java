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
package com.axelor.apps.sale.service.saleorderline.pack;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.Pack;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.PackLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.axelor.apps.sale.translation.ITranslation;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SaleOrderLinePackServiceImpl implements SaleOrderLinePackService {

  protected AppBaseService appBaseService;
  protected SaleOrderLineRepository saleOrderLineRepository;
  protected SaleOrderLineProductService saleOrderLineProductService;
  protected TaxService taxService;
  protected ProductCompanyService productCompanyService;
  protected CurrencyService currencyService;
  protected SaleOrderLinePriceService saleOrderLinePriceService;
  protected SaleOrderLineDiscountService saleOrderLineDiscountService;

  @Inject
  public SaleOrderLinePackServiceImpl(
      AppBaseService appBaseService,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderLineProductService saleOrderLineProductService,
      TaxService taxService,
      ProductCompanyService productCompanyService,
      CurrencyService currencyService,
      SaleOrderLinePriceService saleOrderLinePriceService,
      SaleOrderLineDiscountService saleOrderLineDiscountService) {
    this.appBaseService = appBaseService;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.saleOrderLineProductService = saleOrderLineProductService;
    this.taxService = taxService;
    this.productCompanyService = productCompanyService;
    this.currencyService = currencyService;
    this.saleOrderLinePriceService = saleOrderLinePriceService;
    this.saleOrderLineDiscountService = saleOrderLineDiscountService;
  }

  @Override
  public List<SaleOrderLine> createNonStandardSOLineFromPack(
      Pack pack,
      SaleOrder saleOrder,
      BigDecimal packQty,
      List<SaleOrderLine> saleOrderLineList,
      Integer sequence) {
    SaleOrderLine saleOrderLine;
    Set<Integer> packLineTypeSet = getPackLineTypes(pack.getComponents());
    int typeSelect = SaleOrderLineRepository.TYPE_START_OF_PACK;
    for (int i = 0; i < 2; i++) {
      if (packLineTypeSet == null || !packLineTypeSet.contains(typeSelect)) {
        saleOrderLine =
            this.createStartOfPackAndEndOfPackTypeSaleOrderLine(
                pack, saleOrder, packQty, null, typeSelect, sequence);
        saleOrderLineList.add(saleOrderLine);
      }
      if (typeSelect == SaleOrderLineRepository.TYPE_START_OF_PACK) {
        sequence += pack.getComponents().size() + 1;
        typeSelect = SaleOrderLineRepository.TYPE_END_OF_PACK;
      }
    }

    return saleOrderLineList;
  }

  @Override
  public SaleOrderLine createStartOfPackAndEndOfPackTypeSaleOrderLine(
      Pack pack,
      SaleOrder saleOrder,
      BigDecimal packqty,
      PackLine packLine,
      Integer typeSelect,
      Integer sequence) {

    SaleOrderLine saleOrderLine = new SaleOrderLine();
    saleOrderLine.setTypeSelect(typeSelect);
    switch (typeSelect) {
      case SaleOrderLineRepository.TYPE_START_OF_PACK:
        saleOrderLine.setProductName(packLine == null ? pack.getName() : packLine.getProductName());
        saleOrderLine.setQty(
            packLine != null && packLine.getQuantity() != null
                ? packLine
                    .getQuantity()
                    .multiply(packqty)
                    .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP)
                : packqty);
        break;

      case SaleOrderLineRepository.TYPE_END_OF_PACK:
        saleOrderLine.setProductName(
            packLine == null
                ? I18n.get(ITranslation.SALE_ORDER_LINE_END_OF_PACK)
                : packLine.getProductName());
        saleOrderLine.setIsShowTotal(pack.getIsShowTotal());
        saleOrderLine.setIsHideUnitAmounts(pack.getIsHideUnitAmounts());
        break;
      default:
        return null;
    }
    saleOrderLine.setSaleOrder(saleOrder);
    saleOrderLine.setSequence(sequence);
    return saleOrderLine;
  }

  @Override
  public boolean hasEndOfPackTypeLine(List<SaleOrderLine> saleOrderLineList) {
    return ObjectUtils.isEmpty(saleOrderLineList)
        ? Boolean.FALSE
        : saleOrderLineList.stream()
            .anyMatch(
                saleOrderLine ->
                    saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_END_OF_PACK);
  }

  @Override
  public boolean isStartOfPackTypeLineQtyChanged(List<SaleOrderLine> saleOrderLineList) {

    if (ObjectUtils.isEmpty(saleOrderLineList)) {
      return false;
    }
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      if (saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_START_OF_PACK
          && saleOrderLine.getId() != null) {
        SaleOrderLine oldSaleOrderLine = saleOrderLineRepository.find(saleOrderLine.getId());
        if (oldSaleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_START_OF_PACK
            && saleOrderLine.getQty().compareTo(oldSaleOrderLine.getQty()) != 0) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Map<String, Object> fillPriceFromPackLine(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, PackLine packLine) throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(
        saleOrderLineProductService.fillTaxInformation(saleOrderLine, saleOrder));

    saleOrderLine.setCompanyCostPrice(
        saleOrderLinePriceService.getCompanyCostPrice(saleOrder, saleOrderLine));
    BigDecimal exTaxPrice;
    BigDecimal inTaxPrice;
    if (saleOrderLine.getProduct().getInAti()) {
      inTaxPrice =
          this.getInTaxUnitPriceFromPackLine(
              saleOrder, saleOrderLine, saleOrderLine.getTaxLineSet(), packLine);
      saleOrderLineMap.putAll(
          saleOrderLineDiscountService.fillDiscount(saleOrderLine, saleOrder, inTaxPrice));
      inTaxPrice =
          saleOrderLineDiscountService.getDiscountedPrice(saleOrderLine, saleOrder, inTaxPrice);
      if (!saleOrderLine.getEnableFreezeFields()) {
        saleOrderLine.setPrice(
            taxService.convertUnitPrice(
                true,
                saleOrderLine.getTaxLineSet(),
                inTaxPrice,
                appBaseService.getNbDecimalDigitForUnitPrice()));
        saleOrderLine.setInTaxPrice(inTaxPrice);
      }
    } else {
      exTaxPrice =
          this.getExTaxUnitPriceFromPackLine(
              saleOrder, saleOrderLine, saleOrderLine.getTaxLineSet(), packLine);
      saleOrderLineMap.putAll(
          saleOrderLineDiscountService.fillDiscount(saleOrderLine, saleOrder, exTaxPrice));
      exTaxPrice =
          saleOrderLineDiscountService.getDiscountedPrice(saleOrderLine, saleOrder, exTaxPrice);
      if (!saleOrderLine.getEnableFreezeFields()) {
        saleOrderLine.setPrice(exTaxPrice);
        saleOrderLine.setInTaxPrice(
            taxService.convertUnitPrice(
                false,
                saleOrderLine.getTaxLineSet(),
                exTaxPrice,
                appBaseService.getNbDecimalDigitForUnitPrice()));
      }
    }

    saleOrderLineMap.put("companyCostPrice", saleOrderLine.getCompanyCostPrice());
    saleOrderLineMap.put("price", saleOrderLine.getPrice());
    saleOrderLineMap.put("inTaxPrice", saleOrderLine.getInTaxPrice());
    return saleOrderLineMap;
  }

  @Override
  public BigDecimal getExTaxUnitPriceFromPackLine(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet, PackLine packLine)
      throws AxelorException {
    Currency currency = packLine != null ? packLine.getPack().getCurrency() : null;
    return this.getUnitPriceFromPackLine(saleOrder, saleOrderLine, taxLineSet, false, currency);
  }

  @Override
  public BigDecimal getInTaxUnitPriceFromPackLine(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet, PackLine packLine)
      throws AxelorException {
    Currency currency = packLine != null ? packLine.getPack().getCurrency() : null;
    return this.getUnitPriceFromPackLine(saleOrder, saleOrderLine, taxLineSet, true, currency);
  }

  /**
   * A method used to get the unit price of a sale order line from pack line, either in ati or wt
   *
   * @param saleOrder the sale order containing the sale order line
   * @param saleOrderLine
   * @param taxLineSet the tax applied to the unit price
   * @param resultInAti whether you want the result in ati or not
   * @return the unit price of the sale order line
   * @throws AxelorException
   */
  protected BigDecimal getUnitPriceFromPackLine(
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      Set<TaxLine> taxLineSet,
      boolean resultInAti,
      Currency startCurrency)
      throws AxelorException {

    Product product = saleOrderLine.getProduct();

    Boolean productInAti =
        (Boolean) productCompanyService.get(product, "inAti", saleOrder.getCompany());
    BigDecimal productSalePrice = saleOrderLine.getPrice();

    BigDecimal price =
        (productInAti == resultInAti)
            ? productSalePrice
            : taxService.convertUnitPrice(
                productInAti, taxLineSet, productSalePrice, AppBaseService.COMPUTATION_SCALING);

    Currency currency =
        startCurrency != null
            ? startCurrency
            : (Currency) productCompanyService.get(product, "saleCurrency", saleOrder.getCompany());

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            currency, saleOrder.getCurrency(), price, saleOrder.getCreationDate())
        .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
  }

  @Override
  public Set<Integer> getPackLineTypes(List<PackLine> packLineList) {
    Set<Integer> packLineTypeSet = new HashSet<>();
    packLineList.stream()
        .forEach(
            packLine -> {
              if (packLine.getTypeSelect() == PackLineRepository.TYPE_START_OF_PACK) {
                packLineTypeSet.add(PackLineRepository.TYPE_START_OF_PACK);
              } else if (packLine.getTypeSelect() == PackLineRepository.TYPE_END_OF_PACK) {
                packLineTypeSet.add(PackLineRepository.TYPE_END_OF_PACK);
              }
            });
    return packLineTypeSet;
  }
}
