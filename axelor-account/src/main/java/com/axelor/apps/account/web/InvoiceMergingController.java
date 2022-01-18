package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceMergingService;
import com.axelor.apps.account.service.invoice.InvoiceMergingService.InvoiceMergingResult;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.tool.MapTools;
import com.axelor.auth.db.AuditableModel;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceMergingController {

  private String getViewName(InvoiceMergingResult result) {
    if (result.getInvoiceType() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE) {
      return "supplier-invoices-merge-confirm-form";
    }
    return "customer-invoices-merge-confirm-form";
  }

  /**
   * A controller to generate a single invoice from several.
   *
   * @param request
   * @param response
   */
  public void mergeInvoice(ActionRequest request, ActionResponse response) {
    try {
      InvoiceMergingService invoiceMergingService = Beans.get(InvoiceMergingService.class);
      List<Invoice> invoicesToMerge =
          MapTools.makeList(Invoice.class, request.getContext().get("invoiceToMerge"));
      if (CollectionUtils.isNotEmpty(invoicesToMerge)) {
        InvoiceMergingResult result = invoiceMergingService.mergeInvoices(invoicesToMerge);
        if (result.isConfirmationNeeded()) {
          // Need to display intermediate screen to select some values
          ActionView.ActionViewBuilder confirmView =
              ActionView.define("Confirm merge invoice")
                  .model(Wizard.class.getName())
                  .add("form", getViewName(result))
                  .param("popup", Boolean.TRUE.toString())
                  .param("show-toolbar", Boolean.FALSE.toString())
                  .param("show-confirm", Boolean.FALSE.toString())
                  .param("popup-save", Boolean.FALSE.toString())
                  .param("forceEdit", Boolean.TRUE.toString());

          if (invoiceMergingService.getChecks(result).isExistContactPartnerDiff()) {
            confirmView.context("contextContactPartnerToCheck", Boolean.TRUE.toString());
            confirmView.context(
                "contextPartnerId",
                Optional.ofNullable(
                        invoiceMergingService.getCommonFields(result).getCommonPartner())
                    .map(AuditableModel::getId)
                    .map(Objects::toString)
                    .orElse(null));
          }
          if (invoiceMergingService.getChecks(result).isExistPriceListDiff()) {
            confirmView.context("contextPriceListToCheck", Boolean.TRUE.toString());
          }
          if (invoiceMergingService.getChecks(result).isExistPaymentModeDiff()) {
            confirmView.context("contextPaymentModeToCheck", Boolean.TRUE.toString());
          }
          if (invoiceMergingService.getChecks(result).isExistPaymentConditionDiff()) {
            confirmView.context("contextPaymentConditionToCheck", Boolean.TRUE.toString());
          }
          if (result.getInvoiceType().equals(InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)) {
            if (invoiceMergingService.getChecks(result).isExistSupplierInvoiceNbDiff()) {
              confirmView.context("contextSupplierInvoiceNbToCheck", Boolean.TRUE.toString());
            }
            if (invoiceMergingService.getChecks(result).isExistOriginDateDiff()) {
              confirmView.context("contextOriginDateToCheck", Boolean.TRUE.toString());
            }
          }
          confirmView.context("invoiceToMerge", invoicesToMerge);

          response.setView(confirmView.map());
          return;
        }
        if (result.getInvoice() != null) {
          // Open the generated invoice in a new tab
          response.setView(
              ActionView.define("Invoice")
                  .model(Invoice.class.getName())
                  .add("grid", "invoice-grid")
                  .add("form", "invoice-form")
                  .param("search-filters", "customer-invoices-filters")
                  .param("forceEdit", Boolean.TRUE.toString())
                  .context("_showRecord", String.valueOf(result.getInvoice().getId()))
                  .map());
          response.setCanClose(true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * A controller to generate a single invoice from several from confirmation popup.
   *
   * @param request
   * @param response
   */
  public void mergeInvoiceFromPopup(ActionRequest request, ActionResponse response) {
    try {
      InvoiceMergingService invoiceMergingService = Beans.get(InvoiceMergingService.class);
      List<Invoice> invoicesToMerge =
          MapTools.makeList(Invoice.class, request.getContext().get("invoiceToMerge"));
      Partner contactPartner =
          MapTools.findObject(Partner.class, request.getContext().get("contactPartner"));
      PriceList priceList =
          MapTools.findObject(PriceList.class, request.getContext().get("priceList"));
      PaymentMode paymentMode =
          MapTools.findObject(PaymentMode.class, request.getContext().get("paymentMode"));
      PaymentCondition paymentCondition =
          MapTools.findObject(PaymentCondition.class, request.getContext().get("paymentCondition"));
      if (CollectionUtils.isNotEmpty(invoicesToMerge)) {
        InvoiceMergingResult result =
            invoiceMergingService.mergeInvoices(
                invoicesToMerge, contactPartner, priceList, paymentMode, paymentCondition);
        if (result.getInvoice() != null) {
          // Open the generated invoice in a new tab
          response.setView(
              ActionView.define("Invoice")
                  .model(Invoice.class.getName())
                  .add("grid", "invoice-grid")
                  .add("form", "invoice-form")
                  .param("search-filters", "customer-invoices-filters")
                  .param("forceEdit", Boolean.TRUE.toString())
                  .context("_showRecord", String.valueOf(result.getInvoice().getId()))
                  .map());
          response.setCanClose(true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * A controller to generate a single invoice from several from confirmation popup.
   *
   * @param request
   * @param response
   */
  public void mergeSupplInvoiceFromPopup(ActionRequest request, ActionResponse response) {
    try {
      InvoiceMergingService invoiceMergingService = Beans.get(InvoiceMergingService.class);
      List<Invoice> invoicesToMerge =
          MapTools.makeList(Invoice.class, request.getContext().get("invoiceToMerge"));
      Partner contactPartner =
          MapTools.findObject(Partner.class, request.getContext().get("contactPartner"));
      PriceList priceList =
          MapTools.findObject(PriceList.class, request.getContext().get("priceList"));
      PaymentMode paymentMode =
          MapTools.findObject(PaymentMode.class, request.getContext().get("paymentMode"));
      PaymentCondition paymentCondition =
          MapTools.findObject(PaymentCondition.class, request.getContext().get("paymentCondition"));
      String supplierInvoiceNb = request.getContext().get("supplierInvoiceNb").toString();
      LocalDate originDate = LocalDate.parse(request.getContext().get("originDate").toString());
      if (CollectionUtils.isNotEmpty(invoicesToMerge)) {
        InvoiceMergingResult result =
            invoiceMergingService.mergeInvoices(
                invoicesToMerge,
                contactPartner,
                priceList,
                paymentMode,
                paymentCondition,
                supplierInvoiceNb,
                originDate);
        if (result.getInvoice() != null) {
          // Open the generated invoice in a new tab
          response.setView(
              ActionView.define("Invoice")
                  .model(Invoice.class.getName())
                  .add("grid", "invoice-grid")
                  .add("form", "invoice-form")
                  .param("search-filters", "customer-invoices-filters")
                  .param("forceEdit", Boolean.TRUE.toString())
                  .context("_showRecord", String.valueOf(result.getInvoice().getId()))
                  .map());
          response.setCanClose(true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
