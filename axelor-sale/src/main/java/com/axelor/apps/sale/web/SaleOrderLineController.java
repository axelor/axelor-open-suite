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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.cart.CartProductService;
import com.axelor.apps.sale.service.observer.SaleOrderLineFireService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.pricing.SaleOrderLinePricingService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCheckService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineContextHelper;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDomainService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDummyService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineInitValueService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineMultipleQtyService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineOnChangeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineViewService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class SaleOrderLineController {

  public void onNew(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);
    response.setAttrs(
        Beans.get(SaleOrderLineFireService.class).getOnNewAttrs(saleOrderLine, saleOrder));

    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineDummyService.class).getOnNewDummies(saleOrderLine, saleOrder));
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineInitValueService.class).onNewInitValues(saleOrder, saleOrderLine));
    response.setValues(saleOrderLineMap);
  }

  public void onLoad(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);
    response.setAttrs(
        Beans.get(SaleOrderLineFireService.class).getOnLoadAttrs(saleOrderLine, saleOrder));

    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineDummyService.class).getOnLoadDummies(saleOrderLine, saleOrder));
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineInitValueService.class).onLoadInitValues(saleOrder, saleOrderLine));
    response.setValues(saleOrderLineMap);
  }

  public void onNewEditable(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);
    response.setAttrs(Beans.get(SaleOrderLineViewService.class).focusProduct());

    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineDummyService.class)
            .getOnNewEditableDummies(saleOrderLine, saleOrder));
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineInitValueService.class)
            .onNewEditableInitValues(saleOrder, saleOrderLine));
    response.setValues(saleOrderLineMap);
  }

  public void computeSubMargin(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);
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
      PricingService pricingService = Beans.get(PricingService.class);
      SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);
      SaleOrderLineProductService saleOrderLineProductService =
          Beans.get(SaleOrderLineProductService.class);

      try {
        Map<String, Object> saleOrderLineMap =
            saleOrderLineProductService.computeProductInformation(saleOrderLine, saleOrder);
        saleOrderLineMap.putAll(
            Beans.get(SaleOrderLineComputeService.class).computeValues(saleOrder, saleOrderLine));
        saleOrderLineMap.putAll(
            Beans.get(SaleOrderLineDummyService.class)
                .getOnProductChangeDummies(saleOrderLine, saleOrder));
        saleOrderLineMap.putAll(
            Beans.get(SaleOrderLineOnChangeService.class)
                .productOnChange(saleOrderLine, saleOrder));
        response.setAttrs(
            Beans.get(SaleOrderLineViewService.class)
                .getProductOnChangeAttrs(saleOrderLine, saleOrder));

        Beans.get(SaleOrderLineCheckService.class).productOnChangeCheck(saleOrderLine, saleOrder);

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
        response.setValues(saleOrderLineProductService.resetProductInformation(saleOrderLine));
        TraceBackService.trace(response, e);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Update the ex. tax unit price of an invoice line from its in. tax unit price.
   *
   * @param request
   * @param response
   */
  public void updatePrice(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);
    Map<String, Object> saleOrderLineMap =
        Beans.get(SaleOrderLineOnChangeService.class).inTaxPriceOnChange(saleOrderLine, saleOrder);
    response.setAttrs(
        Beans.get(SaleOrderLineViewService.class).hidePriceDiscounted(saleOrder, saleOrderLine));
    response.setValues(saleOrderLineMap);
  }

  /**
   * Update the in. tax unit price of an invoice line from its ex. tax unit price.
   *
   * @param request
   * @param response
   */
  public void updateInTaxPrice(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);
    Map<String, Object> saleOrderLineMap =
        Beans.get(SaleOrderLineOnChangeService.class).priceOnChange(saleOrderLine, saleOrder);
    response.setAttrs(
        Beans.get(SaleOrderLineViewService.class).hidePriceDiscounted(saleOrder, saleOrderLine));
    response.setValues(saleOrderLineMap);
  }

  public void typeSelectOnChange(ActionRequest request, ActionResponse response) {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
    response.setValues(
        Beans.get(SaleOrderLineOnChangeService.class).typeSelectOnChange(saleOrderLine));
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
      SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);
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
      SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);
      Beans.get(SaleOrderLinePricingService.class).computePricingScale(saleOrderLine, saleOrder);

      response.setValues(saleOrderLine);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @ErrorException
  public void qtyOnChange(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);

    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineOnChangeService.class).qtyOnChange(saleOrderLine, saleOrder));
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineDummyService.class).checkMultipleQty(saleOrderLine));
    response.setAttrs(
        Beans.get(SaleOrderLineViewService.class).getQtyOnChangeAttrs(saleOrderLine, saleOrder));

    String notifyMessage =
        Beans.get(SaleOrderLineMultipleQtyService.class).getMultipleQtyErrorMessage(saleOrderLine);
    if (StringUtils.notEmpty(notifyMessage)) {
      response.setNotify(notifyMessage);
    }

    response.setValues(saleOrderLineMap);
  }

  public void taxLineOnChange(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);
    Map<String, Object> saleOrderLineMap =
        Beans.get(SaleOrderLineOnChangeService.class).taxLineOnChange(saleOrderLine, saleOrder);
    response.setValues(saleOrderLineMap);
  }

  public void discountTypeSelectOnChange(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineOnChangeService.class).compute(saleOrderLine, saleOrder));
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineDummyService.class)
            .getOnDiscountTypeChangeDummies(saleOrderLine, saleOrder));
    response.setAttrs(
        Beans.get(SaleOrderLineViewService.class)
            .getDiscountTypeSelectOnChangeAttrs(saleOrderLine, saleOrder));
    response.setValues(saleOrderLineMap);
  }

  public void discountAmountOnChange(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);
    Map<String, Object> saleOrderLineMap =
        Beans.get(SaleOrderLineOnChangeService.class).compute(saleOrderLine, saleOrder);
    response.setAttrs(
        Beans.get(SaleOrderLineViewService.class).hidePriceDiscounted(saleOrder, saleOrderLine));
    response.setValues(saleOrderLineMap);
  }

  public void selectedComplementaryProductListOnChange(
      ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    response.setValues(
        Beans.get(SaleOrderLineComplementaryProductService.class)
            .setIsComplementaryProductsUnhandledYet(saleOrderLine));
  }

  public void setScaleForPriceAndQty(ActionRequest request, ActionResponse response) {
    response.setAttrs(Beans.get(SaleOrderLineViewService.class).getPriceAndQtyScale());
  }

  public void unitOnChange(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = SaleOrderLineContextHelper.getSaleOrder(context);
    Beans.get(SaleOrderLineCheckService.class).unitOnChangeCheck(saleOrderLine, saleOrder);
  }

  public void addToCart(ActionRequest request, ActionResponse response) {
    try {
      SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
      Product product = saleOrderLine.getProduct();
      Beans.get(CartProductService.class).addToCart(product);
      response.setNotify(
          String.format(I18n.get(SaleExceptionMessage.PRODUCT_ADDED_TO_CART), product.getName()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
