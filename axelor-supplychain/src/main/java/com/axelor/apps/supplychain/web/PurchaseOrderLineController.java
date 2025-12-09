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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.model.AnalyticLineModel;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticGroupService;
import com.axelor.apps.account.service.analytic.AnalyticLineModelService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.cart.CartProductService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.analytic.AnalyticAttrsSupplychainService;
import com.axelor.apps.supplychain.service.analytic.AnalyticLineModelInitSupplychainService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.ContextHelper;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class PurchaseOrderLineController {

  public void computeAnalyticDistribution(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder =
          ContextHelper.getContextParent(request.getContext(), PurchaseOrder.class, 1);

      if (Beans.get(AppAccountService.class).getAppAccount().getManageAnalyticAccounting()) {
        AnalyticLineModel analyticLineModel =
            AnalyticLineModelInitSupplychainService.castAsAnalyticLineModel(
                purchaseOrderLine, purchaseOrder);

        Beans.get(AnalyticLineModelService.class).computeAnalyticDistribution(analyticLineModel);

        response.setValue(
            "analyticDistributionTemplate", analyticLineModel.getAnalyticDistributionTemplate());
        response.setValue("analyticMoveLineList", analyticLineModel.getAnalyticMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void createAnalyticDistributionWithTemplate(
      ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder =
          ContextHelper.getContextParent(request.getContext(), PurchaseOrder.class, 1);
      AnalyticLineModel analyticLineModel =
          AnalyticLineModelInitSupplychainService.castAsAnalyticLineModel(
              purchaseOrderLine, purchaseOrder);

      Beans.get(AnalyticLineModelService.class)
          .createAnalyticDistributionWithTemplate(analyticLineModel);

      response.setValue("analyticMoveLineList", analyticLineModel.getAnalyticMoveLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setAxisDomains(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      PurchaseOrder purchaseOrder =
          ContextHelper.getContextParent(request.getContext(), PurchaseOrder.class, 1);

      if (purchaseOrder == null) {
        return;
      }

      response.setAttrs(
          Beans.get(AnalyticGroupService.class)
              .getAnalyticAxisDomainAttrsMap(
                  AnalyticLineModelInitSupplychainService.castAsAnalyticLineModel(
                      purchaseOrderLine, purchaseOrder)));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnalyticAccountLines(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder =
          ContextHelper.getContextParent(request.getContext(), PurchaseOrder.class, 1);

      if (purchaseOrder == null) {
        return;
      }

      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      AnalyticLineModel analyticLineModel =
          AnalyticLineModelInitSupplychainService.castAsAnalyticLineModel(
              purchaseOrderLine, purchaseOrder);

      if (Beans.get(AnalyticLineModelService.class).analyzeAnalyticLineModel(analyticLineModel)) {
        response.setValue("analyticMoveLineList", analyticLineModel.getAnalyticMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageAxis(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder =
          ContextHelper.getContextParent(request.getContext(), PurchaseOrder.class, 1);

      if (purchaseOrder == null || purchaseOrder.getCompany() == null) {
        return;
      }

      Map<String, Map<String, Object>> attrsMap = new HashMap<>();
      Beans.get(AnalyticAttrsService.class)
          .addAnalyticAxisAttrs(purchaseOrder.getCompany(), null, attrsMap);

      response.setAttrs(attrsMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printAnalyticAccounts(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder =
          ContextHelper.getContextParent(request.getContext(), PurchaseOrder.class, 1);

      if (purchaseOrder == null || purchaseOrder.getCompany() == null) {
        return;
      }

      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

      response.setValues(
          Beans.get(AnalyticGroupService.class)
              .getAnalyticAccountValueMap(
                  (AnalyticLine) purchaseOrderLine, purchaseOrder.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDomainAnalyticDistributionTemplate(
      ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder =
          ContextHelper.getContextParent(request.getContext(), PurchaseOrder.class, 1);

      if (purchaseOrder == null || purchaseOrder.getCompany() == null) {
        return;
      }

      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

      response.setAttr(
          "analyticDistributionTemplate",
          "domain",
          Beans.get(AnalyticAttrsService.class)
              .getAnalyticDistributionTemplateDomain(
                  purchaseOrder.getSupplierPartner(),
                  purchaseOrderLine.getProduct(),
                  purchaseOrder.getCompany(),
                  purchaseOrder.getTradingName(),
                  null,
                  true));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAnalyticDistributionPanelHidden(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder =
          ContextHelper.getContextParent(request.getContext(), PurchaseOrder.class, 1);

      if (purchaseOrder == null || purchaseOrder.getCompany() == null) {
        return;
      }

      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      AnalyticLineModel analyticLineModel =
          AnalyticLineModelInitSupplychainService.castAsAnalyticLineModel(
              purchaseOrderLine, purchaseOrder);
      Map<String, Map<String, Object>> attrsMap = new HashMap<>();

      Beans.get(AnalyticAttrsSupplychainService.class)
          .addAnalyticDistributionPanelHiddenAttrs(analyticLineModel, attrsMap);
      response.setAttrs(attrsMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void addToCart(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
      Product product = purchaseOrderLine.getProduct();
      Beans.get(CartProductService.class).addToCart(product);
      response.setNotify(
          String.format(I18n.get(SaleExceptionMessage.PRODUCT_ADDED_TO_CART), product.getName()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkTaxLineSet(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

      if (purchaseOrderLine != null && purchaseOrderLine.getTaxLineSet() != null) {
        TaxAccountService taxAccountService = Beans.get(TaxAccountService.class);
        taxAccountService.checkTaxLinesNotOnlyNonDeductibleTaxes(purchaseOrderLine.getTaxLineSet());
        taxAccountService.checkSumOfNonDeductibleTaxesOnTaxLines(purchaseOrderLine.getTaxLineSet());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void validateRealizedQty(ActionRequest request, ActionResponse response) {
    PurchaseOrder purchaseOrder =
        ContextHelper.getContextParent(request.getContext(), PurchaseOrder.class, 1);
    PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

    boolean qtyValid =
        Beans.get(PurchaseOrderLineServiceSupplyChain.class)
            .validateRealizedQty(purchaseOrder, purchaseOrderLine);

    response.setValue("$qtyValid", qtyValid);
    if (!qtyValid) {
      response.setError(
          I18n.get(
              SupplychainExceptionMessage.PURCHASE_ORDER_LINE_QTY_UPDATE_NOT_ALLOWED_REALIZED));
    }
  }

  public void validateInvoicedQty(ActionRequest request, ActionResponse response) {
    PurchaseOrder purchaseOrder =
        ContextHelper.getContextParent(request.getContext(), PurchaseOrder.class, 1);
    PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

    boolean qtyValid =
        Beans.get(PurchaseOrderLineServiceSupplyChain.class)
            .validateInvoicedQty(purchaseOrder, purchaseOrderLine);

    response.setValue("$qtyValid", qtyValid);
    if (!qtyValid) {
      response.setError(
          I18n.get(
              SupplychainExceptionMessage.PURCHASE_ORDER_LINE_QTY_UPDATE_NOT_ALLOWED_INVOICED));
    }
  }
}
