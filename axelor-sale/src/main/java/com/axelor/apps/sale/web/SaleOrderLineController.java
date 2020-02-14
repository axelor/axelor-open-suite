/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.web;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

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
    SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
    SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);

    saleOrderLine.setSaleOrder(saleOrder);
    Map<String, BigDecimal> map = saleOrderLineService.computeSubMargin(saleOrder, saleOrderLine);

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
      SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);

      Product product = saleOrderLine.getProduct();

      if (saleOrder == null || product == null) {
        resetProductInformation(response, saleOrderLine);
        return;
      }

      Integer packPriceSelect = product.getPackPriceSelect();
      if (saleOrderLine.getIsSubLine()) {
        if (context.getParent().getContextClass().equals(SaleOrderLine.class)) {
          packPriceSelect = context.getParent().asType(SaleOrderLine.class).getPackPriceSelect();
        } else if (saleOrderLine.getParentLine() != null) {
          packPriceSelect = saleOrderLine.getParentLine().getPackPriceSelect();
        }
      }

      try {
        product = Beans.get(ProductRepository.class).find(product.getId());
        saleOrderLineService.computeProductInformation(saleOrderLine, saleOrder, packPriceSelect);
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
    response.setValue("packPriceSelect", null);
    response.setValue("subLineList", null);
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
            .getTaxEquiv(
                saleOrder.getClientPartner().getFiscalPosition(),
                saleOrderLine.getTaxLine().getTax()));
  }

  public void getDiscount(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);

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
                saleOrderLineService.convertUnitPrice(true, saleOrderLine.getTaxLine(), price));
          } else {
            response.setValue("price", price);
            response.setValue(
                "inTaxPrice",
                saleOrderLineService.convertUnitPrice(false, saleOrderLine.getTaxLine(), price));
          }
        }

        if (saleOrderLine.getProduct().getInAti() != saleOrder.getInAti()
            && (Integer) discounts.get("discountTypeSelect")
                != PriceListLineRepository.AMOUNT_TYPE_PERCENT) {
          response.setValue(
              "discountAmount",
              saleOrderLineService.convertUnitPrice(
                  saleOrderLine.getProduct().getInAti(),
                  saleOrderLine.getTaxLine(),
                  (BigDecimal) discounts.get("discountAmount")));
        } else {
          response.setValue("discountAmount", discounts.get("discountAmount"));
        }
        response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
      }

    } catch (Exception e) {
      response.setFlash(e.getMessage());
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
          Beans.get(SaleOrderLineService.class).convertUnitPrice(true, taxLine, inTaxPrice));
    } catch (Exception e) {
      response.setFlash(e.getMessage());
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
          Beans.get(SaleOrderLineService.class).convertUnitPrice(false, taxLine, exTaxPrice));
    } catch (Exception e) {
      response.setFlash(e.getMessage());
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
      BigDecimal inTaxPrice = price.add(price.multiply(saleOrderLine.getTaxLine().getValue()));

      response.setValue("inTaxPrice", inTaxPrice);

    } catch (Exception e) {
      response.setFlash(e.getMessage());
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
      response.setValues(newSaleOrderLine);
    }
  }

  public void checkQty(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    Beans.get(SaleOrderLineService.class).checkMultipleQty(saleOrderLine, response);
  }

  public void updateSubLineQty(ActionRequest request, ActionResponse response)
      throws AxelorException {

    SaleOrderLine newkitLine = request.getContext().asType(SaleOrderLine.class);
    BigDecimal qty = BigDecimal.ZERO;
    BigDecimal oldKitQty = BigDecimal.ZERO;
    BigDecimal newKitQty = BigDecimal.ZERO;
    BigDecimal exTaxTotal = BigDecimal.ZERO;
    BigDecimal inTaxTotal = BigDecimal.ZERO;
    BigDecimal priceDiscounted = BigDecimal.ZERO;
    BigDecimal taxRate = BigDecimal.ZERO;
    BigDecimal companyExTaxTotal = BigDecimal.ZERO;
    BigDecimal companyInTaxTotal = BigDecimal.ZERO;

    Context context = request.getContext();
    SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
    SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);

    if (newkitLine.getTypeSelect() == SaleOrderLineRepository.TYPE_PACK) {

      if (newkitLine.getOldQty().compareTo(BigDecimal.ZERO) == 0) {
        oldKitQty = BigDecimal.ONE;
      } else {
        oldKitQty = newkitLine.getOldQty();
      }

      if (newkitLine.getQty().compareTo(BigDecimal.ZERO) != 0) {
        newKitQty = newkitLine.getQty();
      }

      List<SaleOrderLine> orderLines = newkitLine.getSubLineList();

      if (orderLines != null) {
        if (newKitQty.compareTo(BigDecimal.ZERO) != 0) {
          for (SaleOrderLine line : orderLines) {
            qty = (line.getQty().divide(oldKitQty, 2, RoundingMode.HALF_EVEN)).multiply(newKitQty);
            priceDiscounted = saleOrderLineService.computeDiscount(line, saleOrder.getInAti());

            if (line.getTaxLine() != null) {
              taxRate = line.getTaxLine().getValue();
            }

            if (!saleOrder.getInAti()) {
              exTaxTotal = saleOrderLineService.computeAmount(qty, priceDiscounted);
              inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
              companyExTaxTotal =
                  saleOrderLineService.getAmountInCompanyCurrency(exTaxTotal, saleOrder);
              companyInTaxTotal = companyExTaxTotal.add(companyExTaxTotal.multiply(taxRate));
            } else {
              inTaxTotal = saleOrderLineService.computeAmount(qty, priceDiscounted);
              exTaxTotal =
                  inTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
              companyInTaxTotal =
                  saleOrderLineService.getAmountInCompanyCurrency(inTaxTotal, saleOrder);
              companyExTaxTotal =
                  companyInTaxTotal.divide(
                      taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
            }

            line.setQty(qty.setScale(2, RoundingMode.HALF_EVEN));
            line.setPriceDiscounted(priceDiscounted);
            line.setExTaxTotal(exTaxTotal);
            line.setInTaxTotal(inTaxTotal);
            line.setCompanyExTaxTotal(companyExTaxTotal);
            line.setCompanyInTaxTotal(companyInTaxTotal);
          }
        } else {
          for (SaleOrderLine line : orderLines) {
            line.setQty(qty.setScale(2, RoundingMode.HALF_EVEN));
          }
        }

        response.setValue("oldQty", newKitQty);
        response.setValue("subLineList", orderLines);
      }
    }
  }

  public void resetPackLine(ActionRequest request, ActionResponse response) throws AxelorException {

    Context context = request.getContext();
    SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
    SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);
    SaleOrderLine packLine = context.asType(SaleOrderLine.class);
    try {
      saleOrderLineService.fillPrice(packLine, saleOrder, packLine.getPackPriceSelect());
      compute(response, saleOrder, packLine);
    } catch (Exception e) {
      e.printStackTrace();
      TraceBackService.trace(response, e);
    }
  }

  public void resetPackSubLine(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Context context = request.getContext();
    SaleOrderLineService saleOrderLineService = Beans.get(SaleOrderLineService.class);
    SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);
    SaleOrderLine packLine = context.asType(SaleOrderLine.class);
    List<SaleOrderLine> subLines = packLine.getSubLineList();

    try {
      if (subLines != null) {
        for (SaleOrderLine line : subLines) {
          saleOrderLineService.fillPrice(line, saleOrder, packLine.getPackPriceSelect());
          saleOrderLineService.computeValues(saleOrder, line);
        }
        response.setValue("subLineList", subLines);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  private void compute(ActionResponse response, SaleOrder saleOrder, SaleOrderLine orderLine)
      throws AxelorException {

    Map<String, BigDecimal> map =
        Beans.get(SaleOrderLineService.class).computeValues(saleOrder, orderLine);

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
  }
}
