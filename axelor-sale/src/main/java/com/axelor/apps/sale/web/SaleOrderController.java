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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PricedOrderDomainService;
import com.axelor.apps.base.service.TradingNameService;
import com.axelor.apps.base.service.exception.HandleExceptionResponse;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.sale.db.Pack;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.PackRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.SaleOrderDomainService;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderPrintService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SaleOrderController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void compute(ActionRequest request, ActionResponse response) {

    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

    try {
      saleOrder = Beans.get(SaleOrderComputeService.class).computeSaleOrder(saleOrder);
      response.setValues(saleOrder);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeMargin(ActionRequest request, ActionResponse response) {

    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

    try {
      Beans.get(SaleOrderMarginService.class).computeMarginSaleOrder(saleOrder);

      response.setValue("accountedRevenue", saleOrder.getAccountedRevenue());
      response.setValue("totalCostPrice", saleOrder.getTotalCostPrice());
      response.setValue("totalGrossMargin", saleOrder.getTotalGrossMargin());
      response.setValue("marginRate", saleOrder.getMarginRate());
      response.setValue("markup", saleOrder.getMarkup());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Print the sale order as a PDF.
   *
   * @param request
   * @param response
   */
  public void showSaleOrder(ActionRequest request, ActionResponse response) {
    this.exportSaleOrder(request, response, false, ReportSettings.FORMAT_PDF);
  }

  /**
   * Print a proforma invoice as a PDF.
   *
   * @param request
   * @param response
   */
  public void printProformaInvoice(ActionRequest request, ActionResponse response) {
    this.exportSaleOrder(request, response, true, ReportSettings.FORMAT_PDF);
  }

  public void exportSaleOrderExcel(ActionRequest request, ActionResponse response) {
    this.exportSaleOrder(request, response, false, ReportSettings.FORMAT_XLSX);
  }

  public void exportSaleOrderWord(ActionRequest request, ActionResponse response) {
    this.exportSaleOrder(request, response, false, ReportSettings.FORMAT_DOC);
  }

  @SuppressWarnings("unchecked")
  public void exportSaleOrder(
      ActionRequest request, ActionResponse response, boolean proforma, String format) {

    Context context = request.getContext();
    String fileLink;
    String title;
    SaleOrderPrintService saleOrderPrintService = Beans.get(SaleOrderPrintService.class);

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
        fileLink = saleOrderPrintService.printSaleOrders(ids);
        title = I18n.get("Sale orders");

      } else if (context.get("id") != null) {

        SaleOrder saleOrder =
            Beans.get(SaleOrderRepository.class).find(Long.parseLong(context.get("id").toString()));
        title = Beans.get(SaleOrderService.class).getFileName(saleOrder);
        fileLink = saleOrderPrintService.printSaleOrder(saleOrder, proforma, format);
        response.setCanClose(true);

        logger.debug("Printing " + title);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(SaleExceptionMessage.SALE_ORDER_PRINT));
      }
      response.setView(ActionView.define(title).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelSaleOrder(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

      Beans.get(SaleOrderWorkflowService.class)
          .cancelSaleOrder(
              Beans.get(SaleOrderRepository.class).find(saleOrder.getId()),
              saleOrder.getCancelReason(),
              saleOrder.getCancelReasonStr());

      response.setInfo(I18n.get("The sale order was canceled"));
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void finalizeQuotation(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());

    try {
      Beans.get(SaleOrderWorkflowService.class).finalizeQuotation(saleOrder);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }

    response.setReload(true);
  }

  public void completeSaleOrder(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());

    try {
      Beans.get(SaleOrderWorkflowService.class).completeSaleOrder(saleOrder);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }

    response.setReload(true);
  }

  public void confirmSaleOrder(ActionRequest request, ActionResponse response) {

    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

      Beans.get(SaleOrderWorkflowService.class)
          .confirmSaleOrder(Beans.get(SaleOrderRepository.class).find(saleOrder.getId()));

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  @SuppressWarnings("unchecked")
  public void generateViewSaleOrder(ActionRequest request, ActionResponse response) {
    LinkedHashMap<String, Object> saleOrderTemplateContext =
        (LinkedHashMap<String, Object>) request.getContext().get("_saleOrderTemplate");
    Integer saleOrderId = (Integer) saleOrderTemplateContext.get("id");
    SaleOrder context = Beans.get(SaleOrderRepository.class).find(Long.valueOf(saleOrderId));

    response.setView(
        ActionView.define(I18n.get("Sale order"))
            .model(SaleOrder.class.getName())
            .add("form", "sale-order-form-wizard")
            .context("_idCopy", context.getId().toString())
            .context("_wizardCurrency", request.getContext().get("currency"))
            .context("_wizardPriceList", request.getContext().get("priceList"))
            .map());

    response.setCanClose(true);
  }

  public void generateViewTemplate(ActionRequest request, ActionResponse response) {
    SaleOrder context = request.getContext().asType(SaleOrder.class);
    response.setView(
        ActionView.define(I18n.get("Template"))
            .model(SaleOrder.class.getName())
            .add("form", "sale-order-template-form-wizard")
            .context("_idCopy", context.getId().toString())
            .map());
  }

  public void generateSaleOrderWizard(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrderTemplate = request.getContext().asType(SaleOrder.class);
    Partner clientPartner = saleOrderTemplate.getClientPartner();

    response.setView(
        ActionView.define(I18n.get("Create the quotation"))
            .model(Wizard.class.getName())
            .add("form", "sale-order-template-wizard-form")
            .param("popup", "reload")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("width", "large")
            .param("popup-save", "false")
            .context("_saleOrderTemplate", saleOrderTemplate)
            .context("_clientPartnerCurrency", clientPartner.getCurrency())
            .map());
  }

  @SuppressWarnings("unchecked")
  public void createSaleOrder(ActionRequest request, ActionResponse response)
      throws AxelorException {
    SaleOrder origin =
        Beans.get(SaleOrderRepository.class)
            .find(Long.parseLong(request.getContext().get("_idCopy").toString()));

    if (origin != null) {
      LinkedHashMap<String, Object> wizardCurrencyContext =
          (LinkedHashMap<String, Object>) request.getContext().get("_wizardCurrency");
      Integer wizardCurrencyId = (Integer) wizardCurrencyContext.get("id");
      Currency wizardCurrency =
          Beans.get(CurrencyRepository.class).find(Long.valueOf(wizardCurrencyId));

      PriceList wizardPriceList = null;
      if (request.getContext().get("_wizardPriceList") != null) {
        LinkedHashMap<String, Object> wizardPriceListContext =
            (LinkedHashMap<String, Object>) request.getContext().get("_wizardPriceList");
        Integer wizardPriceListId = (Integer) wizardPriceListContext.get("id");
        wizardPriceList =
            Beans.get(PriceListRepository.class).find(Long.valueOf(wizardPriceListId));
      }

      SaleOrder copy =
          Beans.get(SaleOrderCreateService.class)
              .createSaleOrder(origin, wizardCurrency, wizardPriceList);
      response.setValues(Mapper.toMap(copy));
    }
  }

  public void createTemplate(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    if (context.get("_idCopy") != null) {
      String idCopy = context.get("_idCopy").toString();
      SaleOrder origin = Beans.get(SaleOrderRepository.class).find(Long.parseLong(idCopy));
      SaleOrder copy = Beans.get(SaleOrderCreateService.class).createTemplate(origin);
      response.setValues(Mapper.toMap(copy));
    }
  }

  public void computeEndOfValidityDate(ActionRequest request, ActionResponse response) {

    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

    try {
      saleOrder = Beans.get(SaleOrderService.class).computeEndOfValidityDate(saleOrder);
      response.setValue("endOfValidityDate", saleOrder.getEndOfValidityDate());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Set the address string with their values.
   *
   * @param request
   * @param response
   */
  public void computeAddressStr(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    Beans.get(SaleOrderService.class).computeAddressStr(saleOrder);

    response.setValues(saleOrder);
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

    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    PaymentMode paymentMode = (PaymentMode) request.getContext().get("paymentMode");
    Company company = saleOrder.getCompany();
    Partner partner = saleOrder.getClientPartner();
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

  public void enableEditOrder(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder =
        Beans.get(SaleOrderRepository.class)
            .find(request.getContext().asType(SaleOrder.class).getId());

    try {
      boolean checkAvailabiltyRequest =
          Beans.get(SaleOrderService.class).enableEditOrder(saleOrder);
      response.setReload(true);
      if (checkAvailabiltyRequest) {
        response.setNotify(I18n.get(SaleExceptionMessage.SALE_ORDER_EDIT_ORDER_NOTIFY));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from sale order form view, on clicking validate change button. Call {@link
   * SaleOrderService#validateChanges(SaleOrder)}.
   *
   * @param request
   * @param response
   */
  public void validateChanges(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrderView = request.getContext().asType(SaleOrder.class);
      SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrderView.getId());
      Beans.get(SaleOrderService.class).validateChanges(saleOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called on trading name change. Set the default value for {@link SaleOrder#printingSettings}
   *
   * @param request
   * @param response
   */
  public void fillDefaultPrintingSettings(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      response.setValue(
          "printingSettings",
          Beans.get(TradingNameService.class)
              .getDefaultPrintingSettings(saleOrder.getTradingName(), saleOrder.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from sale order form view on partner change. Get the default price list for the sale
   * order. Call {@link PartnerPriceListService#getDefaultPriceList(Partner, int)}.
   *
   * @param request
   * @param response
   */
  @SuppressWarnings("unchecked")
  public void fillPriceList(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder;
    if (request.getContext().get("_saleOrderTemplate") != null) {
      LinkedHashMap<String, Object> saleOrderTemplateContext =
          (LinkedHashMap<String, Object>) request.getContext().get("_saleOrderTemplate");
      Integer saleOrderId = (Integer) saleOrderTemplateContext.get("id");
      saleOrder = Beans.get(SaleOrderRepository.class).find(Long.valueOf(saleOrderId));
    } else {
      saleOrder = request.getContext().asType(SaleOrder.class);
    }
    response.setValue(
        "priceList",
        saleOrder.getClientPartner() != null
            ? Beans.get(PartnerPriceListService.class)
                .getDefaultPriceList(saleOrder.getClientPartner(), PriceListRepository.TYPE_SALE)
            : null);
  }

  /**
   * Called from sale order view on price list select. Call {@link
   * PartnerPriceListService#getPriceListDomain(Partner, int)}.
   *
   * @param request
   * @param response
   */
  @SuppressWarnings("unchecked")
  public void changePriceListDomain(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder;
    if (request.getContext().get("_saleOrderTemplate") != null) {
      LinkedHashMap<String, Object> saleOrderTemplateContext =
          (LinkedHashMap<String, Object>) request.getContext().get("_saleOrderTemplate");
      Integer saleOrderId = (Integer) saleOrderTemplateContext.get("id");
      saleOrder = Beans.get(SaleOrderRepository.class).find(Long.valueOf(saleOrderId));
    } else {
      saleOrder = request.getContext().asType(SaleOrder.class);
    }
    String domain =
        Beans.get(PartnerPriceListService.class)
            .getPriceListDomain(saleOrder.getClientPartner(), PriceListRepository.TYPE_SALE);
    response.setAttr("priceList", "domain", domain);
  }

  public void updateSaleOrderLineList(ActionRequest request, ActionResponse response)
      throws AxelorException {

    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      Beans.get(SaleOrderCreateService.class).updateSaleOrderLineList(saleOrder);
      response.setValue("saleOrderLineList", saleOrder.getSaleOrderLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void addPack(ActionRequest request, ActionResponse response) {
    try {

      Context context = request.getContext();

      String saleOrderId = context.get("_id").toString();
      SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(Long.parseLong(saleOrderId));

      @SuppressWarnings("unchecked")
      LinkedHashMap<String, Object> packMap =
          (LinkedHashMap<String, Object>) request.getContext().get("pack");
      String packId = packMap.get("id").toString();
      Pack pack = Beans.get(PackRepository.class).find(Long.parseLong(packId));

      String qty = context.get("qty").toString();
      BigDecimal packQty = new BigDecimal(qty);

      saleOrder = Beans.get(SaleOrderService.class).addPack(saleOrder, pack, packQty);

      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getSaleOrderPartnerDomain(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
      String domain =
          Beans.get(SaleOrderDomainService.class).getPartnerBaseDomain(saleOrder.getCompany());

      if (!(saleOrderLineList == null || saleOrderLineList.isEmpty())) {
        domain =
            Beans.get(PricedOrderDomainService.class)
                .getPartnerDomain(saleOrder, domain, PriceListRepository.TYPE_SALE);
      }

      response.setAttr("clientPartner", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void handleComplementaryProducts(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

    try {
      List<SaleOrderLine> saleOrderLineList =
          Beans.get(SaleOrderService.class).handleComplementaryProducts(saleOrder);
      response.setValue("saleOrderLineList", saleOrderLineList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateProductQtyWithPackHeaderQty(ActionRequest request, ActionResponse response) {
    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    if (Boolean.FALSE.equals(Beans.get(AppSaleService.class).getAppSale().getEnablePackManagement())
        || !Beans.get(SaleOrderLineService.class)
            .isStartOfPackTypeLineQtyChanged(saleOrder.getSaleOrderLineList())) {
      return;
    }
    try {
      Beans.get(SaleOrderService.class).updateProductQtyWithPackHeaderQty(saleOrder);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
    response.setReload(true);
  }

  @HandleExceptionResponse
  public void separateInNewQuotation(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Set<Entry<String, Object>> contextEntry = request.getContext().entrySet();
    Optional<Entry<String, Object>> saleOrderLineEntries =
        contextEntry.stream()
            .filter(entry -> entry.getKey().equals("saleOrderLineList"))
            .findFirst();
    if (!saleOrderLineEntries.isPresent()) {
      return;
    }

    Entry<String, Object> entry = saleOrderLineEntries.get();
    @SuppressWarnings("unchecked")
    ArrayList<LinkedHashMap<String, Object>> saleOrderLines =
        (ArrayList<LinkedHashMap<String, Object>>) entry.getValue();

    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    SaleOrder copiedSO =
        Beans.get(SaleOrderService.class).separateInNewQuotation(saleOrder, saleOrderLines);
    response.setView(
        ActionView.define(I18n.get("Sale order"))
            .model(SaleOrder.class.getName())
            .add("form", "sale-order-form")
            .add("grid", "sale-order-grid")
            .param("forceEdit", "true")
            .context("_showRecord", copiedSO.getId())
            .map());
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
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      FiscalPosition soFiscalPosition = saleOrder.getFiscalPosition();
      if (soFiscalPosition == null) {
        return;
      }
      if (saleOrder.getTaxNumber() == null) {
        if (saleOrder.getClientPartner() != null
            && saleOrder.getFiscalPosition() == saleOrder.getClientPartner().getFiscalPosition()) {
          return;
        }
      } else {
        for (FiscalPosition fiscalPosition : saleOrder.getTaxNumber().getFiscalPositionSet()) {
          if (fiscalPosition.getId().equals(soFiscalPosition.getId())) {
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
   * Called from sale order form view upon changing the fiscalPosition (directly or via changing the
   * taxNumber) Updates taxLine, taxEquiv and prices by calling {@link
   * SaleOrderLineService#fillPrice(SaleOrderLine, SaleOrder)}.
   *
   * @param request
   * @param response
   */
  public void updateLinesAfterFiscalPositionChange(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      if (saleOrder.getSaleOrderLineList() != null) {
        Beans.get(SaleOrderLineService.class).updateLinesAfterFiscalPositionChange(saleOrder);
      }
      response.setValue("saleOrderLineList", saleOrder.getSaleOrderLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
