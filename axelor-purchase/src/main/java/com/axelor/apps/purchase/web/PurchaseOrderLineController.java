/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.web;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class PurchaseOrderLineController {

  @Inject private PurchaseOrderLineService purchaseOrderLineService;

  @Inject private FiscalPositionService fiscalPositionService;

  public void compute(ActionRequest request, ActionResponse response) throws AxelorException {

    try {
      Context context = request.getContext();
      PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);

      Map<String, BigDecimal> map =
          purchaseOrderLineService.compute(purchaseOrderLine, purchaseOrder);
      response.setValues(map);
      response.setAttr(
          "priceDiscounted",
          "hidden",
          map.getOrDefault("priceDiscounted", BigDecimal.ZERO)
                  .compareTo(
                      purchaseOrder.getInAti()
                          ? purchaseOrderLine.getInTaxPrice()
                          : purchaseOrderLine.getPrice())
              == 0);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getProductInformation(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

    PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);

    Product product = purchaseOrderLine.getProduct();

    if (purchaseOrder == null || product == null) {
      this.resetProductInformation(response);
      return;
    }

    try {
      Optional<TaxLine> taxLine =
          purchaseOrderLineService.getOptionalTaxLine(purchaseOrder, purchaseOrderLine);
      purchaseOrderLine.setTaxLine(taxLine.orElse(null));
      response.setValue("taxLine", taxLine.orElse(null));

      BigDecimal price =
          purchaseOrderLineService.getExTaxUnitPrice(
              purchaseOrder, purchaseOrderLine, taxLine.orElse(null));
      BigDecimal inTaxPrice =
          purchaseOrderLineService.getInTaxUnitPrice(
              purchaseOrder, purchaseOrderLine, taxLine.orElse(null));
      String productName =
          purchaseOrderLineService.getProductSupplierInfos(purchaseOrder, purchaseOrderLine)[0];
      String productCode =
          purchaseOrderLineService.getProductSupplierInfos(purchaseOrder, purchaseOrderLine)[1];

      // Do not interrupt process if product is not in catalog. Purchase price will use
      // its default value if configuration says so (handled in get*TaxUnitPrice).
      if (StringUtils.isBlank(productName) && StringUtils.isBlank(productCode)) {
        productName = product.getName();
        productCode = product.getCode();
      }

      response.setValue("unit", purchaseOrderLineService.getPurchaseUnit(purchaseOrderLine));
      BigDecimal qty = purchaseOrderLineService.getQty(purchaseOrder, purchaseOrderLine);
      purchaseOrderLine.setQty(qty);
      response.setValue("qty", qty);

      Tax tax =
          Beans.get(AccountManagementService.class)
              .getProductTax(
                  Beans.get(AccountManagementService.class)
                      .getAccountManagement(product, purchaseOrder.getCompany()),
                  true);
      TaxEquiv taxEquiv =
          Beans.get(FiscalPositionService.class)
              .getTaxEquiv(purchaseOrder.getSupplierPartner().getFiscalPosition(), tax);
      response.setValue("taxEquiv", taxEquiv);

      Map<String, Object> discounts =
          purchaseOrderLineService.getDiscountsFromPriceLists(
              purchaseOrder, purchaseOrderLine, product.getInAti() ? inTaxPrice : price);

      if (discounts != null) {
        if (discounts.get("price") != null) {
          BigDecimal discountPrice = (BigDecimal) discounts.get("price");
          if (purchaseOrderLine.getProduct().getInAti()) {
            inTaxPrice = discountPrice;
            price =
                purchaseOrderLineService.convertUnitPrice(
                    true, purchaseOrderLine.getTaxLine(), discountPrice);
          } else {
            price = discountPrice;
            inTaxPrice =
                purchaseOrderLineService.convertUnitPrice(
                    false, purchaseOrderLine.getTaxLine(), discountPrice);
          }
        }
        if (purchaseOrderLine.getProduct().getInAti() != purchaseOrder.getInAti()
            && (Integer) discounts.get("discountTypeSelect")
                != PriceListLineRepository.AMOUNT_TYPE_PERCENT) {
          response.setValue(
              "discountAmount",
              purchaseOrderLineService.convertUnitPrice(
                  purchaseOrderLine.getProduct().getInAti(),
                  purchaseOrderLine.getTaxLine(),
                  (BigDecimal) discounts.get("discountAmount")));
        } else {
          response.setValue("discountAmount", discounts.get("discountAmount"));
        }
        response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
      }

      response.setValue("price", price);
      response.setValue("inTaxPrice", inTaxPrice);

      response.setValue(
          "saleMinPrice",
          purchaseOrderLineService.getMinSalePrice(purchaseOrder, purchaseOrderLine));
      response.setValue(
          "salePrice",
          purchaseOrderLineService.getSalePrice(
              purchaseOrder,
              purchaseOrderLine.getProduct(),
              purchaseOrder.getInAti() ? inTaxPrice : price));

      if (!taxLine.isPresent()) {
        String msg =
            String.format(
                I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ACCOUNT_MANAGEMENT_3),
                product.getCode(),
                purchaseOrder.getCompany().getName());

        response.setFlash(msg);
      }

    } catch (Exception e) {
      this.resetProductInformation(response);
      response.setFlash(e.getMessage());
    }
  }

  public void resetProductInformation(ActionResponse response) {

    response.setValue("taxLine", null);
    response.setValue("productName", null);
    response.setValue("unit", null);
    response.setValue("discountAmount", null);
    response.setValue("discountTypeSelect", null);
    response.setValue("price", null);
    response.setValue("inTaxPrice", null);
    response.setValue("saleMinPrice", null);
    response.setValue("salePrice", null);
    response.setValue("exTaxTotal", null);
    response.setValue("inTaxTotal", null);
    response.setValue("companyInTaxTotal", null);
    response.setValue("companyExTaxTotal", null);
    response.setValue("productCode", null);
    response.setValue("qty", BigDecimal.ONE);
    response.setAttr("minQtyNotRespectedLabel", "hidden", true);
    response.setAttr("multipleQtyNotRespectedLabel", "hidden", true);
  }

  public void getTaxEquiv(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
    PurchaseOrder purchaseOrder = getPurchaseOrder(context);

    response.setValue("taxEquiv", null);

    if (purchaseOrder == null
        || purchaseOrderLine == null
        || purchaseOrder.getSupplierPartner() == null
        || purchaseOrderLine.getTaxLine() == null) return;

    response.setValue(
        "taxEquiv",
        fiscalPositionService.getTaxEquiv(
            purchaseOrder.getSupplierPartner().getFiscalPosition(),
            purchaseOrderLine.getTaxLine().getTax()));
  }

  public void updateProductInformation(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

    PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);

    if (purchaseOrder == null || purchaseOrderLine.getProduct() == null) {
      return;
    }

    try {
      BigDecimal price =
          purchaseOrderLine.getProduct().getInAti()
              ? purchaseOrderLineService.getInTaxUnitPrice(
                  purchaseOrder, purchaseOrderLine, purchaseOrderLine.getTaxLine())
              : purchaseOrderLineService.getExTaxUnitPrice(
                  purchaseOrder, purchaseOrderLine, purchaseOrderLine.getTaxLine());

      Map<String, Object> catalogInfo =
          purchaseOrderLineService.updateInfoFromCatalog(purchaseOrder, purchaseOrderLine);

      if (catalogInfo != null) {
        if (catalogInfo.get("price") != null) {
          price = (BigDecimal) catalogInfo.get("price");
        }
        response.setValue("productName", catalogInfo.get("productName"));
        response.setValue("productCode", catalogInfo.get("productCode"));
      }

      Map<String, Object> discounts =
          purchaseOrderLineService.getDiscountsFromPriceLists(
              purchaseOrder, purchaseOrderLine, price);

      if (discounts != null) {
        if (discounts.get("price") != null) {
          price = (BigDecimal) discounts.get("price");
        }
        if (purchaseOrderLine.getProduct().getInAti() != purchaseOrder.getInAti()
            && (Integer) discounts.get("discountTypeSelect")
                != PriceListLineRepository.AMOUNT_TYPE_PERCENT) {
          response.setValue(
              "discountAmount",
              purchaseOrderLineService.convertUnitPrice(
                  purchaseOrderLine.getProduct().getInAti(),
                  purchaseOrderLine.getTaxLine(),
                  (BigDecimal) discounts.get("discountAmount")));
        } else {
          response.setValue("discountAmount", discounts.get("discountAmount"));
        }
        response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
      }

      if (price.compareTo(
              purchaseOrderLine.getProduct().getInAti()
                  ? purchaseOrderLine.getInTaxPrice()
                  : purchaseOrderLine.getPrice())
          != 0) {
        if (purchaseOrderLine.getProduct().getInAti()) {
          response.setValue("inTaxPrice", price);
          response.setValue(
              "price",
              purchaseOrderLineService.convertUnitPrice(
                  true, purchaseOrderLine.getTaxLine(), price));
        } else {
          response.setValue("price", price);
          response.setValue(
              "inTaxPrice",
              purchaseOrderLineService.convertUnitPrice(
                  false, purchaseOrderLine.getTaxLine(), price));
        }
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

    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

    try {
      BigDecimal inTaxPrice = purchaseOrderLine.getInTaxPrice();
      TaxLine taxLine = purchaseOrderLine.getTaxLine();

      response.setValue(
          "price", purchaseOrderLineService.convertUnitPrice(true, taxLine, inTaxPrice));
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

    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

    try {
      BigDecimal exTaxPrice = purchaseOrderLine.getPrice();
      TaxLine taxLine = purchaseOrderLine.getTaxLine();

      response.setValue(
          "inTaxPrice", purchaseOrderLineService.convertUnitPrice(false, taxLine, exTaxPrice));
    } catch (Exception e) {
      response.setFlash(e.getMessage());
    }
  }

  public void convertUnitPrice(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

    PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);

    if (purchaseOrder == null
        || purchaseOrderLine.getProduct() == null
        || purchaseOrderLine.getPrice() == null
        || purchaseOrderLine.getInTaxPrice() == null
        || purchaseOrderLine.getTaxLine() == null) {
      return;
    }

    try {

      BigDecimal price = purchaseOrderLine.getPrice();
      BigDecimal inTaxPrice = price.add(price.multiply(purchaseOrderLine.getTaxLine().getValue()));

      response.setValue("inTaxPrice", inTaxPrice);

    } catch (Exception e) {
      response.setFlash(e.getMessage());
    }
  }

  public PurchaseOrder getPurchaseOrder(Context context) {

    Context parentContext = context.getParent();
    PurchaseOrder purchaseOrder = null;

    if (parentContext != null && parentContext.getContextClass() == PurchaseOrder.class) {

      purchaseOrder = parentContext.asType(PurchaseOrder.class);
      if (!parentContext.getContextClass().toString().equals(PurchaseOrder.class.toString())) {

        PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

        purchaseOrder = purchaseOrderLine.getPurchaseOrder();
      }

    } else {
      PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
      purchaseOrder = purchaseOrderLine.getPurchaseOrder();
    }

    return purchaseOrder;
  }

  public void emptyLine(ActionRequest request, ActionResponse response) {
    PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
    if (purchaseOrderLine.getIsTitleLine()) {
      PurchaseOrderLine newPurchaseOrderLine = new PurchaseOrderLine();
      newPurchaseOrderLine.setIsTitleLine(true);
      newPurchaseOrderLine.setQty(BigDecimal.ZERO);
      newPurchaseOrderLine.setId(purchaseOrderLine.getId());
      newPurchaseOrderLine.setVersion(purchaseOrderLine.getVersion());
      response.setValues(Mapper.toMap(purchaseOrderLine));
    }
  }

  public void checkQty(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
    PurchaseOrder purchaseOrder = getPurchaseOrder(context);

    purchaseOrderLineService.checkMinQty(purchaseOrder, purchaseOrderLine, request, response);

    purchaseOrderLineService.checkMultipleQty(purchaseOrderLine, response);
  }

  /**
   * Called on supplier partner select. Set the domain for the field supplierPartner
   *
   * @param request
   * @param response
   */
  public void supplierPartnerDomain(ActionRequest request, ActionResponse response) {
    PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

    PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
    if (purchaseOrder == null) {
      purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
    }
    Company company = purchaseOrder.getCompany();

    String domain = "";
    if (purchaseOrderLine.getProduct() != null
        && !purchaseOrderLine.getProduct().getSupplierCatalogList().isEmpty()) {
      domain +=
          "self.id != "
              + company.getPartner().getId()
              + " AND self.id IN "
              + purchaseOrderLine
                  .getProduct()
                  .getSupplierCatalogList()
                  .stream()
                  .map(s -> s.getSupplierPartner().getId())
                  .collect(Collectors.toList())
                  .toString()
                  .replace('[', '(')
                  .replace(']', ')');

      String blockedPartnerQuery =
          Beans.get(BlockingService.class)
              .listOfBlockedPartner(company, BlockingRepository.PURCHASE_BLOCKING);

      if (!Strings.isNullOrEmpty(blockedPartnerQuery)) {
        domain += String.format(" AND self.id NOT in (%s)", blockedPartnerQuery);
      }
    } else {
      domain += "self.id = 0";
    }

    response.setAttr("supplierPartner", "domain", domain);
  }
}
