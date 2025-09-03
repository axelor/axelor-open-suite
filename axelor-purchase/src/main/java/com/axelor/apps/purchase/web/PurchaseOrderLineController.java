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
package com.axelor.apps.purchase.web;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderLineWarningService;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class PurchaseOrderLineController {

  public void compute(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);

      Map<String, BigDecimal> map =
          Beans.get(PurchaseOrderLineService.class).compute(purchaseOrderLine, purchaseOrder);
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
    try {
      PurchaseOrderLineService service = Beans.get(PurchaseOrderLineService.class);

      Context context = request.getContext();
      PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);
      Product product = purchaseOrderLine.getProduct();

      this.resetProductInformation(response);
      response.setValues(service.reset(purchaseOrderLine));

      if (purchaseOrder == null || product == null) {
        return;
      }

      purchaseOrderLine.setPurchaseOrder(purchaseOrder);
      service.fill(purchaseOrderLine, purchaseOrder);
      response.setValues(purchaseOrderLine);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void resetProductInformation(ActionResponse response) {
    response.setAttr("qtyLimitNotRespectedLabel", "hidden", true);
    response.setAttr("multipleQtyNotRespectedLabel", "hidden", true);
  }

  public void getTaxEquiv(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder = getPurchaseOrder(context);

      response.setValues(
          Beans.get(PurchaseOrderLineService.class).recomputeTax(purchaseOrder, purchaseOrderLine));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateProductInformation(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    AppBaseService appBaseService = Beans.get(AppBaseService.class);

    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

    PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);

    if (purchaseOrder == null || purchaseOrderLine.getProduct() == null) {
      return;
    }

    try {
      PurchaseOrderLineService purchaseOrderLineService = Beans.get(PurchaseOrderLineService.class);
      TaxService taxService = Beans.get(TaxService.class);

      BigDecimal price =
          purchaseOrderLine.getProduct().getInAti()
              ? purchaseOrderLineService.getInTaxUnitPrice(
                  purchaseOrder, purchaseOrderLine, purchaseOrderLine.getTaxLineSet())
              : purchaseOrderLineService.getExTaxUnitPrice(
                  purchaseOrder, purchaseOrderLine, purchaseOrderLine.getTaxLineSet());

      Map<String, Object> catalogInfo =
          purchaseOrderLineService.updateInfoFromCatalog(purchaseOrder, purchaseOrderLine);

      Product product = purchaseOrderLine.getProduct();
      String productName = null;
      String productCode = null;
      ProductCompanyService productCompanyService = Beans.get(ProductCompanyService.class);
      if (catalogInfo != null) {
        productName =
            catalogInfo.get("productName") != null
                ? (String) catalogInfo.get("productName")
                : (String) productCompanyService.get(product, "name", purchaseOrder.getCompany());
        productCode =
            catalogInfo.get("productCode") != null
                ? (String) catalogInfo.get("productCode")
                : (String) productCompanyService.get(product, "code", purchaseOrder.getCompany());
      } else {
        productName =
            (String) productCompanyService.get(product, "name", purchaseOrder.getCompany());
        productCode =
            (String) productCompanyService.get(product, "code", purchaseOrder.getCompany());
      }
      if (purchaseOrderLine.getProductName() == null) {
        response.setValue("productName", productName);
      }
      if (purchaseOrderLine.getProductCode() == null) {
        response.setValue("productCode", productCode);
      }

      Map<String, Object> discounts =
          purchaseOrderLineService.getDiscountsFromPriceLists(
              purchaseOrder, purchaseOrderLine, price);

      if (discounts != null) {
        if (purchaseOrderLine.getProduct().getInAti() != purchaseOrder.getInAti()
            && (Integer) discounts.get("discountTypeSelect")
                != PriceListLineRepository.AMOUNT_TYPE_PERCENT) {
          response.setValue(
              "discountAmount",
              taxService.convertUnitPrice(
                  purchaseOrderLine.getProduct().getInAti(),
                  purchaseOrderLine.getTaxLineSet(),
                  (BigDecimal) discounts.get("discountAmount"),
                  appBaseService.getNbDecimalDigitForUnitPrice()));
        } else {
          response.setValue("discountAmount", discounts.get("discountAmount"));
        }
        response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
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
  public void updatePrice(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

    try {
      BigDecimal inTaxPrice = purchaseOrderLine.getInTaxPrice();
      Set<TaxLine> taxLineSet = purchaseOrderLine.getTaxLineSet();

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

    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

    try {
      BigDecimal exTaxPrice = purchaseOrderLine.getPrice();
      Set<TaxLine> taxLineSet = purchaseOrderLine.getTaxLineSet();

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

    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

    PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);

    if (purchaseOrder == null
        || purchaseOrderLine.getProduct() == null
        || purchaseOrderLine.getPrice() == null
        || purchaseOrderLine.getInTaxPrice() == null
        || CollectionUtils.isEmpty(purchaseOrderLine.getTaxLineSet())) {
      return;
    }

    try {
      BigDecimal price = purchaseOrderLine.getPrice();
      BigDecimal inTaxPrice =
          price.add(
              price.multiply(
                  Beans.get(TaxService.class).getTotalTaxRate(purchaseOrderLine.getTaxLineSet())));

      response.setValue("inTaxPrice", inTaxPrice);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
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
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      if (purchaseOrderLine.getIsTitleLine()) {
        Map<String, Object> newPurchaseOrderLine = Mapper.toMap(new PurchaseOrderLine());
        newPurchaseOrderLine.put("qty", BigDecimal.ZERO);
        newPurchaseOrderLine.put("id", purchaseOrderLine.getId());
        newPurchaseOrderLine.put("version", purchaseOrderLine.getVersion());
        newPurchaseOrderLine.put("isTitleLine", true);
        response.setValues(newPurchaseOrderLine);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkQty(ActionRequest request, ActionResponse response) {
    try {
      SupplierCatalogService supplierCatalogService = Beans.get(SupplierCatalogService.class);
      Context context = request.getContext();
      PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder = getPurchaseOrder(context);
      Company company = purchaseOrder.getCompany();
      Partner supplierPartner = purchaseOrder.getSupplierPartner();

      if (!supplierCatalogService.checkMinQty(
          purchaseOrderLine.getProduct(),
          supplierPartner,
          company,
          purchaseOrderLine.getQty(),
          request,
          response)) {
        supplierCatalogService.checkMaxQty(
            purchaseOrderLine.getProduct(),
            supplierPartner,
            company,
            purchaseOrderLine.getQty(),
            request,
            response);
      }

      Beans.get(PurchaseOrderLineService.class)
          .checkMultipleQty(company, supplierPartner, purchaseOrderLine, response);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called on supplier partner select. Set the domain for the field supplierPartner
   *
   * @param request
   * @param response
   */
  public void supplierPartnerDomain(ActionRequest request, ActionResponse response) {
    PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

    PurchaseOrder purchaseOrder = request.getContext().getParent().asType(PurchaseOrder.class);
    if (purchaseOrder.getId() != null) {
      purchaseOrder = purchaseOrderLine.getPurchaseOrder();
    }
    Company company = purchaseOrder.getCompany();

    String domain = "";
    if (Beans.get(AppPurchaseService.class).getAppPurchase().getManageSupplierCatalog()
        && purchaseOrderLine.getProduct() != null
        && !purchaseOrderLine.getProduct().getSupplierCatalogList().isEmpty()) {
      if (company.getPartner() != null) {
        domain += "self.id != " + company.getPartner().getId() + " AND ";
      }
      domain +=
          "self.id IN "
              + purchaseOrderLine.getProduct().getSupplierCatalogList().stream()
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

    domain += " AND " + company.getId() + " in (SELECT id FROM self.companySet)";

    response.setAttr("supplierPartner", "domain", domain);
  }

  public void checkDifferentSupplier(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder = getPurchaseOrder(context);
      PurchaseOrderLineService service = Beans.get(PurchaseOrderLineService.class);

      service.checkDifferentSupplier(purchaseOrder, purchaseOrderLine, response);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void translateProductDescriptionAndName(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      InternationalService internationalService = Beans.get(InternationalService.class);
      PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
      PurchaseOrder parent = this.getPurchaseOrder(context);
      Partner partner = parent.getSupplierPartner();
      Company company = parent.getCompany();
      Product product = purchaseOrderLine.getProduct();

      SupplierCatalog supplierCatalog =
          Beans.get(SupplierCatalogService.class).getSupplierCatalog(product, partner, company);

      if (supplierCatalog == null && product != null) {
        Map<String, String> translation =
            internationalService.getProductDescriptionAndNameTranslation(product, partner);

        String description = translation.get("description");
        String productName = translation.get("productName");

        if (description != null
            && !description.isEmpty()
            && productName != null
            && !productName.isEmpty()) {
          response.setValue("description", description);
          response.setValue("productName", productName);
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void differentUnitMessage(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
    PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);

    response.setAttr(
        "$unitWarningMessage",
        "hidden",
        !Beans.get(PurchaseOrderLineWarningService.class)
            .checkSupplierCatalogUnit(purchaseOrderLine, purchaseOrder));
  }
}
