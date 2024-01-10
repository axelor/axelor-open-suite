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
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.base.service.subline.SubLineService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.translation.ITranslation;
import com.axelor.auth.AuthUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.studio.db.AppSale;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
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
        product = Beans.get(ProductRepository.class).find(product.getId());
        saleOrderLineService.computeProductInformation(saleOrderLine, saleOrder);

        if (Beans.get(AppBaseService.class).getAppBase().getEnablePricingScale()) {
          Optional<Pricing> defaultPricing =
              pricingService.getRandomPricing(saleOrder.getCompany(), saleOrderLine, null);

          if (defaultPricing.isPresent()
              && !saleOrderLineService.hasPricingLine(saleOrderLine, saleOrder)) {
            response.setInfo(
                String.format(
                    I18n.get(SaleExceptionMessage.SALE_ORDER_LINE_PRICING_NOT_APPLIED),
                    defaultPricing.get().getName()));
          }
        }

        response.setValue("saleSupplySelect", product.getSaleSupplySelect());
        response.setValues(saleOrderLine);
      } catch (Exception e) {
        resetProductInformation(response, saleOrderLine);
        TraceBackService.trace(response, e);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void resetProductInformation(ActionResponse response, SaleOrderLine line) {
    Beans.get(SaleOrderLineService.class).resetProductInformation(line);
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
        || saleOrderLine.getTaxLine() == null) return;

    response.setValue(
        "taxEquiv",
        Beans.get(FiscalPositionService.class)
            .getTaxEquiv(saleOrder.getFiscalPosition(), saleOrderLine.getTaxLine().getTax()));
  }

  public void getDiscount(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
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
            saleOrderLineService.getDiscountsFromPriceLists(
                saleOrder,
                saleOrderLine,
                saleOrderLineService.getInTaxUnitPrice(
                    saleOrder, saleOrderLine, saleOrderLine.getTaxLine()));
      } else {
        discounts =
            saleOrderLineService.getDiscountsFromPriceLists(
                saleOrder,
                saleOrderLine,
                saleOrderLineService.getExTaxUnitPrice(
                    saleOrder, saleOrderLine, saleOrderLine.getTaxLine()));
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
                    saleOrderLine.getTaxLine(),
                    price,
                    appBaseService.getNbDecimalDigitForUnitPrice()));
          } else {
            response.setValue("price", price);
            response.setValue(
                "inTaxPrice",
                taxService.convertUnitPrice(
                    false,
                    saleOrderLine.getTaxLine(),
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
                  saleOrderLine.getTaxLine(),
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
      TaxLine taxLine = saleOrderLine.getTaxLine();

      response.setValue(
          "price",
          Beans.get(TaxService.class)
              .convertUnitPrice(
                  true,
                  taxLine,
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
      TaxLine taxLine = saleOrderLine.getTaxLine();

      response.setValue(
          "inTaxPrice",
          Beans.get(TaxService.class)
              .convertUnitPrice(
                  false,
                  taxLine,
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
              price.multiply(saleOrderLine.getTaxLine().getValue().divide(new BigDecimal(100))));

      response.setValue("inTaxPrice", inTaxPrice);

    } catch (Exception e) {
      response.setInfo(e.getMessage());
    }
  }

  public void emptyLine(ActionRequest request, ActionResponse response) {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
    if (saleOrderLine.getTypeSelect() != SaleOrderLineRepository.TYPE_NORMAL) {
      Map<String, Object> newSaleOrderLine = Mapper.toMap(new SaleOrderLine());
      if (SaleOrderLineRepository.TYPE_PARENT != saleOrderLine.getTypeSelect()) {
        newSaleOrderLine.put("qty", BigDecimal.ZERO);
      }
      newSaleOrderLine.put("id", saleOrderLine.getId());
      newSaleOrderLine.put("version", saleOrderLine.getVersion());
      newSaleOrderLine.put("typeSelect", saleOrderLine.getTypeSelect());
      newSaleOrderLine.put("subLineId", saleOrderLine.getSubLineId());
      if (saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_END_OF_PACK) {
        newSaleOrderLine.put("productName", I18n.get(ITranslation.SALE_ORDER_LINE_END_OF_PACK));
      }
      response.setValues(newSaleOrderLine);
    }
  }

  public void checkQty(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    Beans.get(SaleOrderLineService.class).checkMultipleQty(saleOrderLine, response);
  }

  /**
   * Called from sale order line form view on load and on discount type select change. Call {@link
   * SaleOrderLineService#computeMaxDiscount} and set the message to the view.
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
      BigDecimal maxDiscount = saleOrderLineService.computeMaxDiscount(saleOrder, saleOrderLine);
      response.setValue("$maxDiscount", maxDiscount);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected void compute(ActionResponse response, SaleOrder saleOrder, SaleOrderLine orderLine)
      throws AxelorException {
    SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
    Map<String, BigDecimal> map = saleOrderLineService.computeValues(saleOrder, orderLine);
    map.put("price", orderLine.getPrice());
    map.put("inTaxPrice", orderLine.getInTaxPrice());
    map.put("companyCostPrice", orderLine.getCompanyCostPrice());
    map.put("discountAmount", orderLine.getDiscountAmount());

    response.setValues(map);
    response.setAttr(
        "priceDiscounted",
        "hidden",
        map.getOrDefault("priceDiscounted", BigDecimal.ZERO)
                .compareTo(saleOrder.getInAti() ? orderLine.getInTaxPrice() : orderLine.getPrice())
            == 0);

    response.setValue("isDirty", true);
    if (CollectionUtils.isNotEmpty(orderLine.getSubSoLineList())) {
      SubLineService subLineService = Beans.get(SubLineService.class);
      subLineService.updateSubLinesQty(orderLine.getQtyBeforeUpdate(), orderLine, saleOrder);
      subLineService.updateSubLinesPrice(orderLine.getPriceBeforeUpdate(), orderLine, saleOrder);
      response.setValue("qtyBeforeUpdate", orderLine.getQty());
      response.setValue("priceBeforeUpdate", orderLine.getPrice());
      response.setValue(
          "subSoLineList",
          orderLine.getSubSoLineList().stream()
              .map(saleOrderLineService::toMapWithSubLine)
              .collect(Collectors.toList()));
    }
  }

  /**
   * Called from sale order line form view, on product selection. Call {@link
   * SaleOrderLineService#computeProductDomain(SaleOrderLine, SaleOrder)}.
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
          "product", "domain", saleOrderLineService.computeProductDomain(saleOrderLine, saleOrder));
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
      Beans.get(SaleOrderLineService.class).computePricingScale(saleOrderLine, saleOrder);

      response.setValues(saleOrderLine);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void translateProductDescriptionAndName(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      InternationalService internationalService = Beans.get(InternationalService.class);
      SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
      Partner partner =
          Beans.get(SaleOrderLineService.class).getSaleOrder(context).getClientPartner();
      String userLanguage = AuthUtils.getUser().getLanguage();
      Product product = saleOrderLine.getProduct();

      if (product != null) {
        Map<String, String> translation =
            internationalService.getProductDescriptionAndNameTranslation(
                product, partner, userLanguage);

        String description = translation.get("description");
        String productName = translation.get("productName");

        if (description != null
            && !description.isEmpty()
            && productName != null
            && !productName.isEmpty()) {
          if (Boolean.TRUE.equals(
              Beans.get(AppSaleService.class).getAppSale().getIsEnabledProductDescriptionCopy())) {
            response.setValue("description", description);
          }
          response.setValue("productName", productName);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void subLinesOnChange(ActionRequest request, ActionResponse response) {
    try {
      SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
      SaleOrderLine line = request.getContext().asType(SaleOrderLine.class);
      saleOrderLineService.subLinesOnChange(
          saleOrderLineService.getSaleOrder(request.getContext()), line);
      response.setValues(line);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateSubLinesQty(ActionRequest request, ActionResponse response) {
    try {
      SubLineService subLineService = Beans.get(SubLineService.class);
      SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
      SaleOrderLine line = request.getContext().asType(SaleOrderLine.class);
      SaleOrder saleOrder = saleOrderLineService.getSaleOrder(request.getContext());

      BigDecimal oldQty = line.getQtyBeforeUpdate();
      subLineService.updateSubLinesQty(oldQty, line, saleOrder);
      response.setValues(saleOrderLineService.toMapWithSubLine(line));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateSubLinesPrice(ActionRequest request, ActionResponse response) {
    try {
      SubLineService subLineService = Beans.get(SubLineService.class);
      SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
      SaleOrderLine line = request.getContext().asType(SaleOrderLine.class);
      SaleOrder saleOrder = saleOrderLineService.getSaleOrder(request.getContext());

      BigDecimal oldPrice = line.getPriceBeforeUpdate();
      subLineService.updateSubLinesPrice(oldPrice, line, saleOrder);
      response.setValues(saleOrderLineService.toMapWithSubLine(line));

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateIsCounted(ActionRequest request, ActionResponse response) {
    SubLineService subLineService = Beans.get(SubLineService.class);
    SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
    SaleOrderLine line = request.getContext().asType(SaleOrderLine.class);

    subLineService.updateIsNotCountable(line);
    response.setValues(saleOrderLineService.toMapWithSubLine(line));
  }

  public void setIsChildCounted(ActionRequest request, ActionResponse response) {
    SaleOrderLine line = request.getContext().asType(SaleOrderLine.class);
    SubLineService subLineService = Beans.get(SubLineService.class);
    boolean isChildCounted = subLineService.isChildCounted(line);
    response.setValue("$isChildCounted", isChildCounted);
  }

  public void setSubLineId(ActionRequest request, ActionResponse response) {
    response.setValue("subLineId", UUID.randomUUID().toString());
  }

  public void setSubLineAttrs(ActionRequest request, ActionResponse response) {
    SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
    AppBaseService appBaseService = Beans.get(AppBaseService.class);
    AppSale appSale = Beans.get(AppSaleService.class).getAppSale();
    SaleOrder saleOrder = saleOrderLineService.getSaleOrder(request.getContext());

    Boolean enablePackManagement = appSale.getEnablePackManagement();
    List<Integer> allSelection =
        Arrays.asList(
            SaleOrderLineRepository.TYPE_NORMAL,
            SaleOrderLineRepository.TYPE_TITLE,
            SaleOrderLineRepository.TYPE_START_OF_PACK,
            SaleOrderLineRepository.TYPE_END_OF_PACK,
            SaleOrderLineRepository.TYPE_PARENT);

    List<Integer> withoutPackSelection =
        Arrays.asList(
            SaleOrderLineRepository.TYPE_NORMAL,
            SaleOrderLineRepository.TYPE_TITLE,
            SaleOrderLineRepository.TYPE_PARENT);

    Boolean isEditableGridEnabled = appSale.getIsEditableGridEnabled();
    Boolean isDiscountEnabledOnEditableGrid = appSale.getIsDiscountEnabledOnEditableGrid();

    response.setAttr("subSoLineList.exTaxTotal", "hidden", saleOrder.getInAti());
    response.setAttr("subSoLineList.price", "hidden", saleOrder.getInAti());
    response.setAttr("subSoLineList.inTaxTotal", "hidden", !saleOrder.getInAti());
    response.setAttr("subSoLineList.inTaxPrice", "hidden", !saleOrder.getInAti());

    response.setAttr("subSoLineList.qty", "scale", appBaseService.getNbDecimalDigitForQty());
    response.setAttr(
        "subSoLineList.price", "scale", appBaseService.getNbDecimalDigitForUnitPrice());

    response.setAttr(
        "subSoLineList.discountAmount", "scale", appBaseService.getNbDecimalDigitForUnitPrice());

    response.setAttr(
        "subSoLineList.typeSelect",
        "selection-in",
        enablePackManagement ? allSelection : withoutPackSelection);

    response.setAttr(
        "subSoLineList.discountTypeSelect",
        "hidden",
        !(isEditableGridEnabled && isDiscountEnabledOnEditableGrid));
    response.setAttr("subSoLineList.discountAmount", "hidden", !isDiscountEnabledOnEditableGrid);
  }
}
