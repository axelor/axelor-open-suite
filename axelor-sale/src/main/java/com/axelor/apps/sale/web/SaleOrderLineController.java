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
package com.axelor.apps.sale.web;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineDomainService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineMultipleQtyService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLinePricingService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineProductService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.translation.ITranslation;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class SaleOrderLineController {

  public void compute(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = Beans.get(SaleOrderLineService.class).getSaleOrder(context);
    try {
      compute(response, saleOrder, saleOrderLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeSubMargin(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = Beans.get(SaleOrderLineService.class).getSaleOrder(context);
    Map<String, BigDecimal> map =
        Beans.get(SaleOrderMarginService.class)
            .getSaleOrderLineComputedMarginInfo(saleOrder, saleOrderLine);

    response.setValues(map);
  }

  /**
   * Called by the sale order line form. Update all fields when the product is changed.
   *
   * @param request
   * @param response
   */
  public void getProductInformation(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
      SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
      PricingService pricingService = Beans.get(PricingService.class);
      SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);

      Product product = saleOrderLine.getProduct();

      if (saleOrder == null || product == null) {
        resetProductInformation(response, saleOrderLine);
        return;
      }

      try {
        Map<String, Object> saleOrderLineMap =
            Beans.get(SaleOrderLineProductService.class)
                .computeProductInformation(saleOrderLine, saleOrder);
        saleOrderLineMap.putAll(
            Beans.get(SaleOrderLineComputeService.class).computeValues(saleOrder, saleOrderLine));
        // Beans.get(SaleOrderLineCheckService.class).check(saleOrderLine); throws exception
        // response.setValues(Beans.get(SaleOrderLineDummyService.class).getDummies());
        // response.setAttrs(Beans.get(SaleOrderLineViewService.class).getHiddenAttrs())

        if (Beans.get(AppBaseService.class).getAppBase().getEnablePricingScale()) {
          Optional<Pricing> defaultPricing =
              pricingService.getRandomPricing(
                  saleOrder.getCompany(),
                  saleOrderLine,
                  null,
                  PricingRepository.PRICING_TYPE_SELECT_SALE_PRICING);

          if (defaultPricing.isPresent()
              && !Beans.get(SaleOrderLinePricingService.class)
                  .hasPricingLine(saleOrderLine, saleOrder)) {
            response.setInfo(
                String.format(
                    I18n.get(SaleExceptionMessage.SALE_ORDER_LINE_PRICING_NOT_APPLIED),
                    defaultPricing.get().getName()));
          }
        }
        response.setValues(saleOrderLineMap);
      } catch (Exception e) {
        resetProductInformation(response, saleOrderLine);
        TraceBackService.trace(response, e);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void resetProductInformation(ActionResponse response, SaleOrderLine line) {
    Beans.get(SaleOrderLineProductService.class).resetProductInformation(line);
    response.setValue("saleSupplySelect", null);
    response.setValue("typeSelect", SaleOrderLineRepository.TYPE_NORMAL);
    response.setValues(line);
  }

  public void getTaxEquiv(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = Beans.get(SaleOrderLineService.class).getSaleOrder(context);

    response.setValue("taxEquiv", null);

    if (saleOrder == null
        || saleOrderLine == null
        || saleOrder.getClientPartner() == null
        || CollectionUtils.isEmpty(saleOrderLine.getTaxLineSet())) return;

    response.setValue(
        "taxEquiv",
        Beans.get(FiscalPositionService.class)
            .getTaxEquivFromTaxLines(saleOrder.getFiscalPosition(), saleOrderLine.getTaxLineSet()));
  }

  public void getDiscount(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
    SaleOrderLineDiscountService saleOrderLineDiscountService =
        Beans.get(SaleOrderLineDiscountService.class);
    SaleOrderLinePriceService saleOrderLinePriceService =
        Beans.get(SaleOrderLinePriceService.class);
    TaxService taxService = Beans.get(TaxService.class);
    AppBaseService appBaseService = Beans.get(AppBaseService.class);

    SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);

    if (saleOrder == null || saleOrderLine.getProduct() == null) {
      return;
    }

    try {

      Map<String, Object> discounts;
      if (saleOrderLine.getProduct().getInAti()) {
        discounts =
            saleOrderLineDiscountService.getDiscountsFromPriceLists(
                saleOrder,
                saleOrderLine,
                saleOrderLinePriceService.getInTaxUnitPrice(
                    saleOrder, saleOrderLine, saleOrderLine.getTaxLineSet()));
      } else {
        discounts =
            saleOrderLineDiscountService.getDiscountsFromPriceLists(
                saleOrder,
                saleOrderLine,
                saleOrderLinePriceService.getExTaxUnitPrice(
                    saleOrder, saleOrderLine, saleOrderLine.getTaxLineSet()));
      }

      if (discounts != null) {
        BigDecimal price = (BigDecimal) discounts.get("price");
        if (price != null
            && price.compareTo(
                    saleOrderLine.getProduct().getInAti()
                        ? saleOrderLine.getInTaxPrice()
                        : saleOrderLine.getPrice())
                != 0) {
          if (saleOrderLine.getProduct().getInAti()) {
            response.setValue("inTaxPrice", price);
            response.setValue(
                "price",
                taxService.convertUnitPrice(
                    true,
                    saleOrderLine.getTaxLineSet(),
                    price,
                    appBaseService.getNbDecimalDigitForUnitPrice()));
          } else {
            response.setValue("price", price);
            response.setValue(
                "inTaxPrice",
                taxService.convertUnitPrice(
                    false,
                    saleOrderLine.getTaxLineSet(),
                    price,
                    appBaseService.getNbDecimalDigitForUnitPrice()));
          }
        }

        if (saleOrderLine.getProduct().getInAti() != saleOrder.getInAti()
            && (Integer) discounts.get("discountTypeSelect")
                != PriceListLineRepository.AMOUNT_TYPE_PERCENT) {
          response.setValue(
              "discountAmount",
              taxService.convertUnitPrice(
                  saleOrderLine.getProduct().getInAti(),
                  saleOrderLine.getTaxLineSet(),
                  (BigDecimal) discounts.get("discountAmount"),
                  appBaseService.getNbDecimalDigitForUnitPrice()));
        } else {
          response.setValue("discountAmount", discounts.get("discountAmount"));
        }
        response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
      }

    } catch (Exception e) {
      response.setInfo(e.getMessage());
    }
  }

  /**
   * Update the ex. tax unit price of an invoice line from its in. tax unit price.
   *
   * @param request
   * @param response
   */
  public void updatePrice(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);

    try {
      BigDecimal inTaxPrice = saleOrderLine.getInTaxPrice();
      Set<TaxLine> taxLineSet = saleOrderLine.getTaxLineSet();

      response.setValue(
          "price",
          Beans.get(TaxService.class)
              .convertUnitPrice(
                  true,
                  taxLineSet,
                  inTaxPrice,
                  Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice()));
    } catch (Exception e) {
      response.setInfo(e.getMessage());
    }
  }

  /**
   * Update the in. tax unit price of an invoice line from its ex. tax unit price.
   *
   * @param request
   * @param response
   */
  public void updateInTaxPrice(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);

    try {
      BigDecimal exTaxPrice = saleOrderLine.getPrice();
      Set<TaxLine> taxLineSet = saleOrderLine.getTaxLineSet();

      response.setValue(
          "inTaxPrice",
          Beans.get(TaxService.class)
              .convertUnitPrice(
                  false,
                  taxLineSet,
                  exTaxPrice,
                  Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice()));
    } catch (Exception e) {
      response.setInfo(e.getMessage());
    }
  }

  public void convertUnitPrice(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);

    SaleOrder saleOrder = Beans.get(SaleOrderLineService.class).getSaleOrder(context);

    if (saleOrder == null
        || saleOrderLine.getProduct() == null
        || saleOrderLine.getPrice() == null
        || saleOrderLine.getInTaxPrice() == null) {
      return;
    }

    try {

      BigDecimal price = saleOrderLine.getPrice();
      BigDecimal inTaxPrice =
          price.add(
              price.multiply(
                  Beans.get(TaxService.class).getTotalTaxRate(saleOrderLine.getTaxLineSet())));

      response.setValue("inTaxPrice", inTaxPrice);

    } catch (Exception e) {
      response.setInfo(e.getMessage());
    }
  }

  public void emptyLine(ActionRequest request, ActionResponse response) {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
    if (saleOrderLine.getTypeSelect() != SaleOrderLineRepository.TYPE_NORMAL) {
      Map<String, Object> newSaleOrderLine = Mapper.toMap(new SaleOrderLine());
      newSaleOrderLine.put("qty", BigDecimal.ZERO);
      newSaleOrderLine.put("id", saleOrderLine.getId());
      newSaleOrderLine.put("version", saleOrderLine.getVersion());
      newSaleOrderLine.put("typeSelect", saleOrderLine.getTypeSelect());
      if (saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_END_OF_PACK) {
        newSaleOrderLine.put("productName", I18n.get(ITranslation.SALE_ORDER_LINE_END_OF_PACK));
      }
      response.setValues(newSaleOrderLine);
    }
  }

  public void checkQty(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    Beans.get(SaleOrderLineMultipleQtyService.class).checkMultipleQty(saleOrderLine, response);
  }

  /**
   * Called from sale order line form view on load and on discount type select change. Call {@link
   * SaleOrderLineDiscountService#computeMaxDiscount} and set the message to the view.
   *
   * @param request
   * @param response
   */
  public void fillMaxDiscount(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
      SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
      SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);
      BigDecimal maxDiscount =
          Beans.get(SaleOrderLineDiscountService.class)
              .computeMaxDiscount(saleOrder, saleOrderLine);
      response.setValue("$maxDiscount", maxDiscount);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected void compute(ActionResponse response, SaleOrder saleOrder, SaleOrderLine orderLine)
      throws AxelorException {

    Map<String, Object> map =
        Beans.get(SaleOrderLineComputeService.class).computeValues(saleOrder, orderLine);

    map.put("price", orderLine.getPrice());
    map.put("inTaxPrice", orderLine.getInTaxPrice());
    map.put("companyCostPrice", orderLine.getCompanyCostPrice());
    map.put("discountAmount", orderLine.getDiscountAmount());

    response.setValues(map);
    response.setAttr(
        "priceDiscounted",
        "hidden",
        ((BigDecimal) map.getOrDefault("priceDiscounted", BigDecimal.ZERO))
                .compareTo(saleOrder.getInAti() ? orderLine.getInTaxPrice() : orderLine.getPrice())
            == 0);
  }

  /**
   * Called from sale order line form view, on product selection. Call {@link
   * com.axelor.apps.sale.service.saleorder.SaleOrderLineDomainService#computeProductDomain(SaleOrderLine,
   * SaleOrder)}.
   *
   * @param request
   * @param response
   */
  public void computeProductDomain(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
      SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
      SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);
      response.setAttr(
          "product",
          "domain",
          Beans.get(SaleOrderLineDomainService.class)
              .computeProductDomain(saleOrderLine, saleOrder));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computePricingScale(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
      SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
      SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);
      Beans.get(SaleOrderLinePricingService.class).computePricingScale(saleOrderLine, saleOrder);

      response.setValues(saleOrderLine);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
