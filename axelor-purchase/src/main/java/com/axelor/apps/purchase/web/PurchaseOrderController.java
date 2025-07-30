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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PricedOrderDomainService;
import com.axelor.apps.base.service.TradingNameService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.PurchaseOrderDomainService;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderSequenceService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.PurchaseOrderWorkflowService;
import com.axelor.apps.purchase.service.attributes.PurchaseOrderAttrsService;
import com.axelor.apps.purchase.service.print.PurchaseOrderPrintService;
import com.axelor.apps.purchase.service.split.PurchaseOrderSplitService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PurchaseOrderController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void setSequence(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

      if (purchaseOrder != null && purchaseOrder.getCompany() != null) {

        response.setValue(
            "purchaseOrderSeq",
            Beans.get(PurchaseOrderSequenceService.class)
                .getSequence(purchaseOrder.getCompany(), purchaseOrder));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void compute(ActionRequest request, ActionResponse response) {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    if (purchaseOrder != null) {
      try {
        purchaseOrder = Beans.get(PurchaseOrderService.class).computePurchaseOrder(purchaseOrder);
        response.setValues(purchaseOrder);
        response.setAttrs(
            Beans.get(PurchaseOrderAttrsService.class).onChangePurchaseOrderLine(purchaseOrder));
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      }
    }
  }

  public void validateSupplier(ActionRequest request, ActionResponse response) {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    response.setValue(
        "supplierPartner", Beans.get(PurchaseOrderService.class).validateSupplier(purchaseOrder));
  }

  /**
   * Called from grid or form purchase order view, print selected purchase order.
   *
   * @param request
   * @param response
   * @return
   */
  @SuppressWarnings("unchecked")
  public void showPurchaseOrder(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    String fileLink;
    String title;
    PurchaseOrderPrintService purchaseOrderPrintService =
        Beans.get(PurchaseOrderPrintService.class);

    try {
      if (!ObjectUtils.isEmpty(request.getContext().get("_ids"))) {
        List<Long> ids =
            Lists.transform(
                (List) request.getContext().get("_ids"),
                new Function<Object, Long>() {
                  @Nullable
                  @Override
                  public Long apply(@Nullable Object input) {
                    return Long.parseLong(input.toString());
                  }
                });
        fileLink = purchaseOrderPrintService.printPurchaseOrders(ids);
        title = I18n.get("Purchase orders");
      } else if (context.get("id") != null) {
        PurchaseOrder purchaseOrder =
            Beans.get(PurchaseOrderRepository.class)
                .find(Long.parseLong(context.get("id").toString()));
        title = purchaseOrderPrintService.getFileName(purchaseOrder);
        fileLink = purchaseOrderPrintService.printPurchaseOrder(purchaseOrder);
        logger.debug("Printing " + title);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(PurchaseExceptionMessage.NO_PURCHASE_ORDER_SELECTED_FOR_PRINTING));
      }
      response.setView(ActionView.define(title).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void requestPurchaseOrder(ActionRequest request, ActionResponse response) {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    try {
      Beans.get(PurchaseOrderService.class)
          .requestPurchaseOrder(
              Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateCostPrice(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      Beans.get(PurchaseOrderService.class)
          .updateCostPrice(Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called on partner, company or payment change. Fill the bank details with a default value.
   *
   * @param request
   * @param response
   */
  public void fillCompanyBankDetails(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      PaymentMode paymentMode = (PaymentMode) request.getContext().get("paymentMode");
      Company company = purchaseOrder.getCompany();
      Partner partner = purchaseOrder.getSupplierPartner();
      if (company == null) {
        return;
      }
      if (partner != null) {
        partner = Beans.get(PartnerRepository.class).find(partner.getId());
      }
      BankDetails defaultBankDetails =
          Beans.get(BankDetailsService.class)
              .getDefaultCompanyBankDetails(company, paymentMode, partner, null);
      response.setValue("companyBankDetails", defaultBankDetails);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validate(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
      Beans.get(PurchaseOrderWorkflowService.class).validatePurchaseOrder(purchaseOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
      Beans.get(PurchaseOrderWorkflowService.class).cancelPurchaseOrder(purchaseOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called on trading name change. Set the default value for {@link PurchaseOrder#printingSettings}
   *
   * @param request
   * @param response
   */
  public void fillDefaultPrintingSettings(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      response.setValue(
          "printingSettings",
          Beans.get(TradingNameService.class)
              .getDefaultPrintingSettings(
                  purchaseOrder.getTradingName(), purchaseOrder.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from purchase order form view on partner change. Get the default price list for the
   * purchase order. Call {@link PartnerPriceListService#getDefaultPriceList(Partner, int)}.
   *
   * @param request
   * @param response
   */
  public void fillPriceList(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      response.setValue(
          "priceList",
          Beans.get(PartnerPriceListService.class)
              .getDefaultPriceList(
                  purchaseOrder.getSupplierPartner(), PriceListRepository.TYPE_PURCHASE));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void changePriceListDomain(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      String domain =
          Beans.get(PartnerPriceListService.class)
              .getPriceListDomain(
                  purchaseOrder.getSupplierPartner(), PriceListRepository.TYPE_PURCHASE);
      response.setAttr("priceList", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void finishPurchaseOrder(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());

      Beans.get(PurchaseOrderWorkflowService.class).finishPurchaseOrder(purchaseOrder);
      response.setReload(true);
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
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      Company company = purchaseOrder.getCompany();
      String domain = Beans.get(PurchaseOrderDomainService.class).getPartnerBaseDomain(company);

      String blockedPartnerQuery =
          Beans.get(BlockingService.class)
              .listOfBlockedPartner(company, BlockingRepository.PURCHASE_BLOCKING);

      if (!Strings.isNullOrEmpty(blockedPartnerQuery)) {
        domain += String.format(" AND self.id NOT in (%s)", blockedPartnerQuery);
      }

      List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();
      if (!(purchaseOrderLineList == null || purchaseOrderLineList.isEmpty())) {
        domain =
            Beans.get(PricedOrderDomainService.class)
                .getPartnerDomain(purchaseOrder, domain, PriceListRepository.TYPE_PURCHASE);
      }

      response.setAttr("supplierPartner", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Empty the fiscal position field if its value is no longer compatible with the new taxNumber
   * after a change
   *
   * @param request
   * @param response
   */
  public void emptyFiscalPositionIfNotCompatible(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      FiscalPosition poFiscalPosition = purchaseOrder.getFiscalPosition();
      if (poFiscalPosition == null) {
        return;
      }
      if (purchaseOrder.getTaxNumber() == null) {
        if (purchaseOrder.getSupplierPartner() != null
            && purchaseOrder.getFiscalPosition()
                == purchaseOrder.getSupplierPartner().getFiscalPosition()) {
          return;
        }
      } else {
        for (FiscalPosition fiscalPosition : purchaseOrder.getTaxNumber().getFiscalPositionSet()) {
          if (fiscalPosition.getId().equals(poFiscalPosition.getId())) {
            return;
          }
        }
      }
      response.setValue("fiscalPosition", null);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from purchase order form view upon changing the fiscalPosition (directly or via changing
   * the taxNumber) Updates taxLine, taxEquiv and prices by calling {@link
   * PurchaseOrderLineService#fill(PurchaseOrderLine, PurchaseOrder)} and {@link
   * PurchaseOrderLineService#compute(PurchaseOrderLine, PurchaseOrder)}.
   *
   * @param request
   * @param response
   */
  public void updateLinesAfterFiscalPositionChange(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      if (purchaseOrder.getPurchaseOrderLineList() != null) {
        Beans.get(PurchaseOrderLineService.class)
            .updateLinesAfterFiscalPositionChange(purchaseOrder);
      }
      response.setValue("purchaseOrderLineList", purchaseOrder.getPurchaseOrderLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void draftPurchaseOrder(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());

      Beans.get(PurchaseOrderWorkflowService.class).draftPurchaseOrder(purchaseOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void separateInNewQuotation(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Set<Map.Entry<String, Object>> contextEntry = request.getContext().entrySet();
    Optional<Map.Entry<String, Object>> purchaseOrderLineEntries =
        contextEntry.stream()
            .filter(entry -> entry.getKey().equals("purchaseOrderLineList"))
            .findFirst();
    if (purchaseOrderLineEntries.isEmpty()) {
      return;
    }

    Map.Entry<String, Object> entry = purchaseOrderLineEntries.get();
    @SuppressWarnings("unchecked")
    ArrayList<LinkedHashMap<String, Object>> purchaseOrderLines =
        (ArrayList<LinkedHashMap<String, Object>>) entry.getValue();

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    PurchaseOrder copiePO =
        Beans.get(PurchaseOrderSplitService.class)
            .separateInNewQuotation(purchaseOrder, purchaseOrderLines);

    response.setReload(true);
    response.setView(
        ActionView.define(I18n.get("Purchase order"))
            .model(PurchaseOrder.class.getName())
            .add("form", "purchase-order-form")
            .add("grid", "purchase-order-grid")
            .param("forceEdit", "true")
            .context("_showRecord", copiePO.getId())
            .map());
  }
}
