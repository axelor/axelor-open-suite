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
package com.axelor.apps.purchase.web;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.TradingNameService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.exception.IExceptionMessage;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.tool.StringTool;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.birt.core.exception.BirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PurchaseOrderController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private PurchaseOrderService purchaseOrderService;

  @Inject private PurchaseOrderRepository purchaseOrderRepo;

  public void setSequence(ActionRequest request, ActionResponse response) throws AxelorException {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    if (purchaseOrder != null && purchaseOrder.getCompany() != null) {

      response.setValue(
          "purchaseOrderSeq", purchaseOrderService.getSequence(purchaseOrder.getCompany()));
    }
  }

  public void compute(ActionRequest request, ActionResponse response) {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    if (purchaseOrder != null) {
      try {
        purchaseOrder = purchaseOrderService.computePurchaseOrder(purchaseOrder);
        response.setValues(purchaseOrder);
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      }
    }
  }

  public void validateSupplier(ActionRequest request, ActionResponse response) {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    response.setValue("supplierPartner", purchaseOrderService.validateSupplier(purchaseOrder));
  }

  /**
   * Called from grid or form purchase order view, print selected purchase order.
   *
   * @param request
   * @param response
   * @return
   * @throws BirtException
   * @throws IOException
   */
  public void showPurchaseOrder(ActionRequest request, ActionResponse response)
      throws AxelorException {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    List<Integer> lstSelectedMove = (List<Integer>) request.getContext().get("_ids");
    String fileLink = purchaseOrderService.printPurchaseOrder(purchaseOrder, lstSelectedMove);

    response.setView(ActionView.define(I18n.get("Purchase order")).add("html", fileLink).map());
  }

  public void requestPurchaseOrder(ActionRequest request, ActionResponse response) {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    try {
      purchaseOrderService.requestPurchaseOrder(purchaseOrderRepo.find(purchaseOrder.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateCostPrice(ActionRequest request, ActionResponse response) throws Exception {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    purchaseOrderService.updateCostPrice(purchaseOrderRepo.find(purchaseOrder.getId()));
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
        if (commonTradingName != null
            && !commonTradingName.equals(purchaseOrderTemp.getTradingName())) {
          commonTradingName = null;
        }
      }
      count++;
    }

    StringBuilder fieldErrors = new StringBuilder();
    if (commonCurrency == null) {
      fieldErrors.append(I18n.get(IExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_CURRENCY));
    }
    if (commonSupplierPartner == null) {
      if (fieldErrors.length() > 0) {
        fieldErrors.append("<br/>");
      }
      fieldErrors.append(I18n.get(IExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_SUPPLIER_PARTNER));
    }
    if (commonCompany == null) {
      if (fieldErrors.length() > 0) {
        fieldErrors.append("<br/>");
      }
      fieldErrors.append(I18n.get(IExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_COMPANY));
    }
    if (commonTradingName == null) {
      fieldErrors.append(I18n.get(IExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_TRADING_NAME));
    }

    if (fieldErrors.length() > 0) {
      response.setFlash(fieldErrors.toString());
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
          ActionView.define("Confirm merge purchase order")
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
          purchaseOrderService.mergePurchaseOrders(
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
            ActionView.define("Purchase order")
                .model(PurchaseOrder.class.getName())
                .add("grid", "purchase-order-grid")
                .add("form", "purchase-order-form")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(purchaseOrder.getId()))
                .map());
        response.setCanClose(true);
      }
    } catch (Exception e) {
      response.setFlash(e.getLocalizedMessage());
    }
  }

  /**
   * Called on partner, company or payment change. Fill the bank details with a default value.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void fillCompanyBankDetails(ActionRequest request, ActionResponse response)
      throws AxelorException {

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
  }

  public void validate(ActionRequest request, ActionResponse response) throws Exception {
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    purchaseOrder = purchaseOrderRepo.find(purchaseOrder.getId());
    purchaseOrderService.validatePurchaseOrder(purchaseOrder);
    response.setReload(true);
  }

  /**
   * Called on printing settings select. Set the domain for {@link PurchaseOrder#printingSettings}
   *
   * @param request
   * @param response
   */
  public void filterPrintingSettings(ActionRequest request, ActionResponse response) {
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    List<PrintingSettings> printingSettingsList =
        Beans.get(TradingNameService.class)
            .getPrintingSettingsList(purchaseOrder.getTradingName(), purchaseOrder.getCompany());
    String domain =
        String.format(
            "self.id IN (%s)",
            !printingSettingsList.isEmpty()
                ? StringTool.getIdListString(printingSettingsList)
                : "0");

    response.setAttr("printingSettings", "domain", domain);
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
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    response.setValue(
        "priceList",
        Beans.get(PartnerPriceListService.class)
            .getDefaultPriceList(
                purchaseOrder.getSupplierPartner(), PriceListRepository.TYPE_PURCHASE));
  }

  public void changePriceListDomain(ActionRequest request, ActionResponse response) {
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    String domain =
        Beans.get(PartnerPriceListService.class)
            .getPriceListDomain(
                purchaseOrder.getSupplierPartner(), PriceListRepository.TYPE_PURCHASE);
    response.setAttr("priceList", "domain", domain);
  }

  public void finishPurchaseOrder(ActionRequest request, ActionResponse response) {
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());

    purchaseOrderService.finishPurchaseOrder(purchaseOrder);
    response.setReload(true);
  }

  /**
   * Called on supplier partner select. Set the domain for the field supplierPartner
   *
   * @param request
   * @param response
   */
  public void supplierPartnerDomain(ActionRequest request, ActionResponse response) {
    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
    Company company = purchaseOrder.getCompany();
    long companyId = company.getPartner() == null ? 0L : company.getPartner().getId();
    String domain =
        String.format(
            "self.id != %d AND self.isContact = false AND self.isSupplier = true", companyId);
    String blockedPartnerQuery =
        Beans.get(BlockingService.class)
            .listOfBlockedPartner(company, BlockingRepository.PURCHASE_BLOCKING);

    if (!Strings.isNullOrEmpty(blockedPartnerQuery)) {
      domain += String.format(" AND self.id NOT in (%s)", blockedPartnerQuery);
    }
    response.setAttr("supplierPartner", "domain", domain);
  }
}
