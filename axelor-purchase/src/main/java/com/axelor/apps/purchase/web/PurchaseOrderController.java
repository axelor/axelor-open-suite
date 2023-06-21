/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.TradingNameService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.PurchaseOrderDomainService;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.PurchaseOrderWorkflowService;
import com.axelor.apps.purchase.service.print.PurchaseOrderPrintService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
            Beans.get(PurchaseOrderService.class).getSequence(purchaseOrder.getCompany()));
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
        fileLink =
            purchaseOrderPrintService.printPurchaseOrder(purchaseOrder, ReportSettings.FORMAT_PDF);
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

  // Generate single purchase order from several
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void mergePurchaseOrder(ActionRequest request, ActionResponse response) {
    List<PurchaseOrder> purchaseOrderList = new ArrayList<PurchaseOrder>();
    List<Long> purchaseOrderIdList = new ArrayList<Long>();
    boolean fromPopup = false;

    if (request.getContext().get("purchaseOrderToMerge") != null) {

      if (request.getContext().get("purchaseOrderToMerge") instanceof List) {
        // No confirmation popup, purchase orders are content in a parameter list
        List<Map> purchaseOrderMap = (List<Map>) request.getContext().get("purchaseOrderToMerge");
        for (Map map : purchaseOrderMap) {
          purchaseOrderIdList.add(new Long((Integer) map.get("id")));
        }
      } else {
        // After confirmation popup, purchase order's id are in a string separated by ","
        String purchaseOrderIdListStr = (String) request.getContext().get("purchaseOrderToMerge");
        for (String purchaseOrderId : purchaseOrderIdListStr.split(",")) {
          purchaseOrderIdList.add(new Long(purchaseOrderId));
        }
        fromPopup = true;
      }
    }

    // Check if currency, supplierPartner, company and tradingName are the same for all selected
    // purchase orders
    Currency commonCurrency = null;
    Partner commonSupplierPartner = null;
    Company commonCompany = null;
    Partner commonContactPartner = null;
    TradingName commonTradingName = null;
    // Useful to determine if a difference exists between contact partners of all purchase orders
    boolean existContactPartnerDiff = false;
    PriceList commonPriceList = null;
    // Useful to determine if a difference exists between price lists of all purchase orders
    boolean existPriceListDiff = false;
    PurchaseOrder purchaseOrderTemp;
    boolean allTradingNamesAreNull = true;
    int count = 1;
    for (Long purchaseOrderId : purchaseOrderIdList) {
      purchaseOrderTemp = JPA.em().find(PurchaseOrder.class, purchaseOrderId);
      purchaseOrderList.add(purchaseOrderTemp);
      if (count == 1) {
        commonCurrency = purchaseOrderTemp.getCurrency();
        commonSupplierPartner = purchaseOrderTemp.getSupplierPartner();
        commonCompany = purchaseOrderTemp.getCompany();
        commonContactPartner = purchaseOrderTemp.getContactPartner();
        commonPriceList = purchaseOrderTemp.getPriceList();
        commonTradingName = purchaseOrderTemp.getTradingName();
        allTradingNamesAreNull = commonTradingName == null;

      } else {
        if (commonCurrency != null && !commonCurrency.equals(purchaseOrderTemp.getCurrency())) {
          commonCurrency = null;
        }
        if (commonSupplierPartner != null
            && !commonSupplierPartner.equals(purchaseOrderTemp.getSupplierPartner())) {
          commonSupplierPartner = null;
        }
        if (commonCompany != null && !commonCompany.equals(purchaseOrderTemp.getCompany())) {
          commonCompany = null;
        }
        if (commonContactPartner != null
            && !commonContactPartner.equals(purchaseOrderTemp.getContactPartner())) {
          commonContactPartner = null;
          existContactPartnerDiff = true;
        }
        if (commonPriceList != null && !commonPriceList.equals(purchaseOrderTemp.getPriceList())) {
          commonPriceList = null;
          existPriceListDiff = true;
        }
        if (!Objects.equals(commonTradingName, purchaseOrderTemp.getTradingName())) {
          commonTradingName = null;
          allTradingNamesAreNull = false;
        }
      }
      count++;
    }

    StringBuilder fieldErrors = new StringBuilder();
    if (commonCurrency == null) {
      fieldErrors.append(I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_CURRENCY));
    }
    if (commonSupplierPartner == null) {
      if (fieldErrors.length() > 0) {
        fieldErrors.append("<br/>");
      }
      fieldErrors.append(
          I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_SUPPLIER_PARTNER));
    }
    if (commonCompany == null) {
      if (fieldErrors.length() > 0) {
        fieldErrors.append("<br/>");
      }
      fieldErrors.append(I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_COMPANY));
    }
    if (commonTradingName == null && !allTradingNamesAreNull) {
      fieldErrors.append(
          I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_TRADING_NAME));
    }

    if (fieldErrors.length() > 0) {
      response.setInfo(fieldErrors.toString());
      return;
    }

    // Check if priceList or contactPartner are content in parameters
    if (request.getContext().get("priceList") != null) {
      commonPriceList =
          JPA.em()
              .find(
                  PriceList.class,
                  new Long((Integer) ((Map) request.getContext().get("priceList")).get("id")));
    }
    if (request.getContext().get("contactPartner") != null) {
      commonContactPartner =
          JPA.em()
              .find(
                  Partner.class,
                  new Long((Integer) ((Map) request.getContext().get("contactPartner")).get("id")));
    }

    if (!fromPopup && (existContactPartnerDiff || existPriceListDiff)) {
      // Need to display intermediate screen to select some values
      ActionViewBuilder confirmView =
          ActionView.define(I18n.get("Confirm merge purchase order"))
              .model(Wizard.class.getName())
              .add("form", "purchase-order-merge-confirm-form")
              .param("popup", "true")
              .param("show-toolbar", "false")
              .param("show-confirm", "false")
              .param("popup-save", "false")
              .param("forceEdit", "true");

      if (existPriceListDiff) {
        confirmView.context("contextPriceListToCheck", "true");
      }
      if (existContactPartnerDiff) {
        confirmView.context("contextContactPartnerToCheck", "true");
        confirmView.context("contextPartnerId", commonSupplierPartner.getId().toString());
      }

      confirmView.context("purchaseOrderToMerge", Joiner.on(",").join(purchaseOrderIdList));

      response.setView(confirmView.map());

      return;
    }

    try {
      PurchaseOrder purchaseOrder =
          Beans.get(PurchaseOrderService.class)
              .mergePurchaseOrders(
                  purchaseOrderList,
                  commonCurrency,
                  commonSupplierPartner,
                  commonCompany,
                  commonContactPartner,
                  commonPriceList,
                  commonTradingName);
      if (purchaseOrder != null) {
        // Open the generated purchase order in a new tab
        response.setView(
            ActionView.define(I18n.get("Purchase order"))
                .model(PurchaseOrder.class.getName())
                .add("grid", "purchase-order-grid")
                .add("form", "purchase-order-form")
                .param("search-filters", "purchase-order-filters")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(purchaseOrder.getId()))
                .map());
        response.setCanClose(true);
      }
    } catch (Exception e) {
      response.setInfo(e.getLocalizedMessage());
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
}
