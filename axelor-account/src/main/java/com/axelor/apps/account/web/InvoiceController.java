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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.BankDetailsDomainServiceAccount;
import com.axelor.apps.account.service.InvoiceVisibilityService;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.AdvancePaymentRefundService;
import com.axelor.apps.account.service.invoice.BankDetailsServiceAccount;
import com.axelor.apps.account.service.invoice.InvoiceControlService;
import com.axelor.apps.account.service.invoice.InvoiceDomainService;
import com.axelor.apps.account.service.invoice.InvoiceFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceGlobalDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.account.service.invoice.InvoiceLineGroupService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceNoteService;
import com.axelor.apps.account.service.invoice.InvoicePfpValidateService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpToolService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.LatePaymentInterestInvoiceService;
import com.axelor.apps.account.service.invoice.print.InvoicePrintService;
import com.axelor.apps.account.service.invoice.tax.InvoiceLineTaxGroupService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.LocalizationRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PrintingTemplateRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PricedOrderDomainService;
import com.axelor.apps.base.service.TradingNameService;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import com.google.common.base.Function;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InvoiceController {

  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Called from invoice form view, to recompute all values linked to the invoice.
   *
   * @param request
   * @param response
   * @return
   */
  public void compute(ActionRequest request, ActionResponse response) {

    Invoice invoice = request.getContext().asType(Invoice.class);

    try {
      response.setValues(Beans.get(InvoiceService.class).compute(invoice));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  /**
   * Called from invoice form view, on clicking validate button.
   *
   * @param request
   * @param response
   * @return
   */
  public void validate(ActionRequest request, ActionResponse response) throws AxelorException {

    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

    try {
      // we have to inject TraceBackService to use non static methods
      TraceBackService traceBackService = Beans.get(TraceBackService.class);
      long tracebackCount = traceBackService.countMessageTraceBack(invoice);
      Beans.get(InvoiceService.class).validate(invoice);
      response.setReload(true);
      if (traceBackService.countMessageTraceBack(invoice) > tracebackCount) {
        traceBackService
            .findLastMessageTraceBack(invoice)
            .ifPresent(
                traceback ->
                    response.setNotify(
                        String.format(
                            I18n.get(
                                com.axelor.message.exception.MessageExceptionMessage
                                    .SEND_EMAIL_EXCEPTION),
                            traceback.getMessage())));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from invoice form view, on clicking ventilate button. Call {@link
   * InvoiceService#ventilate(Invoice)}.
   *
   * @param request
   * @param response
   */
  public void ventilate(ActionRequest request, ActionResponse response) {

    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

    try {
      // we have to inject TraceBackService to use non static methods
      TraceBackService traceBackService = Beans.get(TraceBackService.class);
      long tracebackCount = traceBackService.countMessageTraceBack(invoice);
      Beans.get(InvoiceService.class).ventilate(invoice);
      response.setReload(true);
      if (traceBackService.countMessageTraceBack(invoice) > tracebackCount) {
        traceBackService
            .findLastMessageTraceBack(invoice)
            .ifPresent(
                traceback ->
                    response.setNotify(
                        String.format(
                            I18n.get(
                                com.axelor.message.exception.MessageExceptionMessage
                                    .SEND_EMAIL_EXCEPTION),
                            traceback.getMessage())));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called by the validate button, if the ventilation is skipped in invoice config
   *
   * @param request
   * @param response
   */
  public void validateAndVentilate(ActionRequest request, ActionResponse response) {

    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

    try {
      // we have to inject TraceBackService to use non static methods
      TraceBackService traceBackService = Beans.get(TraceBackService.class);
      long tracebackCount = traceBackService.countMessageTraceBack(invoice);
      Beans.get(InvoiceService.class).validateAndVentilate(invoice);
      response.setReload(true);
      if (traceBackService.countMessageTraceBack(invoice) > tracebackCount) {
        traceBackService
            .findLastMessageTraceBack(invoice)
            .ifPresent(
                traceback ->
                    response.setNotify(
                        String.format(
                            I18n.get(
                                com.axelor.message.exception.MessageExceptionMessage
                                    .SEND_EMAIL_EXCEPTION),
                            traceback.getMessage())));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Passe l'état de la facture à "annulée"
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void cancel(ActionRequest request, ActionResponse response) throws AxelorException {

    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

    Beans.get(InvoiceService.class).cancel(invoice);
    response.setInfo(I18n.get(AccountExceptionMessage.INVOICE_1));
    response.setReload(true);
  }

  public void validateInvoiceTermsBeforeSave(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    try {
      InvoiceTermService invoiceTermService = Beans.get(InvoiceTermService.class);

      if (invoiceTermService.checkInvoiceTermCreationConditions(invoice)) {
        response.setError(
            I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_CREATION_PROHIBITED));
        return;
      }
      if (invoiceTermService.checkIfThereIsDeletedHoldbackInvoiceTerms(invoice)) {
        response.setError(
            I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_HOLD_BACK_DELETION_PROHIBITED));
        return;
      }
      if (invoiceTermService.checkInvoiceTermDeletionConditions(invoice)) {
        response.setError(
            I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_DELETION_PROHIBITED));
        return;
      }
      if (invoiceTermService.checkIfCustomizedInvoiceTerms(invoice.getInvoiceTermList())) {
        if (!invoiceTermService.checkInvoiceTermsSum(invoice)) {
          response.setError(I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_AMOUNT_MISMATCH));
          return;
        }
        if (!invoiceTermService.checkInvoiceTermsPercentageSum(invoice)) {
          response.setError(
              I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_PERCENTAGE_MISMATCH));
          return;
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeInvoiceTerms(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    try {

      InvoiceTermService invoiceTermService = Beans.get(InvoiceTermService.class);

      invoiceTermService.checkAndComputeInvoiceTerms(invoice);

      if (invoice != null) {
        response.setValues(invoice);
      } else {
        response.setValue("invoiceTermList", null);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeInvoiceTermsDueDates(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    try {
      InvoiceTermService invoiceTermService = Beans.get(InvoiceTermService.class);
      invoiceTermService.computeInvoiceTermsDueDates(invoice);
      response.setValue("invoiceTermList", invoice.getInvoiceTermList());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void backToDraft(ActionRequest request, ActionResponse response) throws AxelorException {
    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());
    Beans.get(InvoiceService.class).backToDraft(invoice);
    response.setReload(true);
  }

  /**
   * Function returning both the paymentMode and the paymentCondition
   *
   * @param request
   * @param response
   */
  public void fillPaymentModeAndCondition(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    try {
      if (invoice.getOperationTypeSelect() == null || invoice.getOperationTypeSelect() == 0) {
        return;
      }
      PaymentMode paymentMode = InvoiceToolService.getPaymentMode(invoice);
      PaymentCondition paymentCondition = InvoiceToolService.getPaymentCondition(invoice);
      response.setValue("paymentMode", paymentMode);
      response.setValue("paymentCondition", paymentCondition);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkNotImputedRefunds(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

    try {
      String msg = Beans.get(InvoiceService.class).checkNotImputedRefunds(invoice);
      if (msg != null) {
        response.setInfo(msg);
      }
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkNotLetteredAdvancePaymentMoveLines(
      ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

    try {
      String msg = Beans.get(InvoiceService.class).checkNotLetteredAdvancePaymentMoveLines(invoice);
      if (msg != null) {
        response.setInfo(msg);
      }
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from invoice form view, on clicking create refund button.
   *
   * @param request
   * @param response
   */
  public void createRefund(ActionRequest request, ActionResponse response) {

    Invoice invoice = request.getContext().asType(Invoice.class);

    try {

      invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());
      Invoice refund = Beans.get(InvoiceService.class).createRefund(invoice);
      response.setReload(true);
      response.setNotify(I18n.get(AccountExceptionMessage.INVOICE_2));

      int refundType =
          invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
              ? InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND
              : InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND;

      String viewTitle = AccountExceptionMessage.INVOICE_GENERATED_INVOICE_REFUND;
      if (InvoiceToolService.isRefund(refund)) {
        if (refund.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
          viewTitle = AccountExceptionMessage.INVOICE_GENERATED_REFUND_ADVANCE_PAYMENT;
        } else {
          viewTitle = AccountExceptionMessage.INVOICE_GENERATED_REFUND;
        }
      }

      response.setView(
          ActionView.define(String.format(I18n.get(viewTitle), invoice.getInvoiceId()))
              .model(Invoice.class.getName())
              .add("form", "invoice-form")
              .add("grid", "invoice-grid")
              .param("search-filters", "customer-invoices-filters")
              .param("forceTitle", "true")
              .context("_showRecord", refund.getId().toString())
              .context("_operationTypeSelect", refundType)
              .domain("self.originalInvoice.id = " + invoice.getId())
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void usherProcess(ActionRequest request, ActionResponse response) {

    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

    try {
      Beans.get(InvoiceService.class).usherProcess(invoice);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void passInIrrecoverable(ActionRequest request, ActionResponse response) {

    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

    try {
      Beans.get(IrrecoverableService.class).passInIrrecoverable(invoice, true);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void notPassInIrrecoverable(ActionRequest request, ActionResponse response) {

    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

    try {
      Beans.get(IrrecoverableService.class).notPassInIrrecoverable(invoice);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /** Method to generate invoice as a Pdf */
  @SuppressWarnings("unchecked")
  public void showInvoice(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    String fileLink;
    String title;

    try {
      if (!ObjectUtils.isEmpty(context.get("_ids"))) {
        List<Long> ids =
            (List)
                (((List) context.get("_ids"))
                    .stream()
                        .filter(ObjectUtils::notEmpty)
                        .map(input -> Long.parseLong(input.toString()))
                        .collect(Collectors.toList()));
        fileLink = Beans.get(InvoicePrintService.class).printInvoices(ids);
        title = I18n.get("Invoices");
      } else if ((context.get("_invoiceId") != null || context.get("id") != null)
          && (Wizard.class.equals(context.getContextClass())
              || Invoice.class.equals(context.getContextClass()))) {

        Map<String, Object> map = getParamsMap(request);
        Invoice invoice = (Invoice) map.get("invoice");
        Integer reportType = (Integer) map.get("reportType");

        Map localizationMap =
            reportType != null
                    && (reportType == 1 || reportType == 3)
                    && context.get("localization") != null
                ? (Map<String, Object>) context.get("localization")
                : null;
        String localizationCode =
            localizationMap != null && localizationMap.get("id") != null
                ? Beans.get(LocalizationRepository.class)
                    .find(Long.parseLong(localizationMap.get("id").toString()))
                    .getCode()
                : null;

        PrintingTemplate invoicePrintTemplate = getPrintingTemplate(context, invoice);

        fileLink =
            Beans.get(InvoicePrintService.class)
                .printInvoice(invoice, false, invoicePrintTemplate, reportType, localizationCode);
        title = I18n.get("Invoice");
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(AccountExceptionMessage.INVOICE_3));
      }
      response.setView(ActionView.define(title).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected PrintingTemplate getPrintingTemplate(Context context, Invoice invoice)
      throws AxelorException {
    if (context.get("invoicePrintTemplate") == null) {
      return Beans.get(AccountConfigService.class).getInvoicePrintTemplate(invoice.getCompany());
    }
    PrintingTemplate invoicePrintTemplate =
        Mapper.toBean(
            PrintingTemplate.class, (Map<String, Object>) context.get("invoicePrintTemplate"));
    return Beans.get(PrintingTemplateRepository.class).find(invoicePrintTemplate.getId());
  }

  public void regenerateAndShowInvoice(ActionRequest request, ActionResponse response) {
    Map<String, Object> map = getParamsMap(request);
    Invoice invoice = (Invoice) map.get("invoice");
    Integer reportType = (Integer) map.get("reportType");

    try {
      response.setCanClose(true);
      response.setView(
          ActionView.define(I18n.get("Invoice"))
              .add(
                  "html",
                  Beans.get(InvoicePrintService.class)
                      .printInvoice(
                          invoice,
                          true,
                          Beans.get(AccountConfigService.class)
                              .getInvoicePrintTemplate(invoice.getCompany()),
                          reportType,
                          null))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected String buildMassMessage(int doneCount, int errorCount) {
    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            I18n.get(
                BaseExceptionMessage.ABSTRACT_BATCH_DONE_SINGULAR,
                BaseExceptionMessage.ABSTRACT_BATCH_DONE_PLURAL,
                doneCount),
            doneCount));
    sb.append(" ");
    sb.append(
        String.format(
            I18n.get(
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL,
                errorCount),
            errorCount));
    return sb.toString();
  }

  protected void massProcess(
      ActionRequest request,
      ActionResponse response,
      Function<Collection<? extends Number>, Pair<Integer, Integer>> function) {

    try {
      @SuppressWarnings("unchecked")
      List<Number> ids = (List<Number>) request.getContext().get("_ids");

      if (ObjectUtils.isEmpty(ids)) {
        response.setError(BaseExceptionMessage.RECORD_NONE_SELECTED);
        return;
      }

      Pair<Integer, Integer> massCount = function.apply(ids);

      String message = buildMassMessage(massCount.getLeft(), massCount.getRight());
      response.setInfo(message);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void massValidation(ActionRequest request, ActionResponse response) {
    try {
      Function<Collection<? extends Number>, Pair<Integer, Integer>> function;

      if (Beans.get(AppAccountService.class).getAppInvoice().getIsVentilationSkipped()) {
        function = Beans.get(InvoiceService.class)::massValidateAndVentilate;
      } else {
        function = Beans.get(InvoiceService.class)::massValidate;
      }

      massProcess(request, response, function);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void massVentilation(ActionRequest request, ActionResponse response) {
    try {
      massProcess(request, response, Beans.get(InvoiceService.class)::massVentilate);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeAddressStr(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    response.setValue(
        "addressStr", Beans.get(AddressService.class).computeAddressStr(invoice.getAddress()));
  }

  public void computeDeliveryAddressStr(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    response.setValue(
        "deliveryAddressStr",
        Beans.get(AddressService.class).computeAddressStr(invoice.getDeliveryAddress()));
  }

  /**
   * Called on load and in partner, company or payment mode change. Fill the bank details with a
   * default value.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void fillCompanyBankDetails(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Invoice invoice = request.getContext().asType(Invoice.class);
    PaymentMode paymentMode = invoice.getPaymentMode();
    Company company = invoice.getCompany();
    Partner partner = invoice.getPartner();
    if (company == null) {
      return;
    }
    if (partner != null) {
      partner = Beans.get(PartnerRepository.class).find(partner.getId());
    }
    BankDetails defaultBankDetails =
        Beans.get(BankDetailsService.class)
            .getDefaultCompanyBankDetails(
                company, paymentMode, partner, invoice.getOperationTypeSelect());
    response.setValue("companyBankDetails", defaultBankDetails);
  }

  /**
   * Called on load and on new, create the domain for the field {@link
   * Invoice#advancePaymentInvoiceSet}
   *
   * @param request
   * @param response
   */
  public void fillAdvancePaymentInvoiceSetDomain(ActionRequest request, ActionResponse response) {

    Invoice invoice = request.getContext().asType(Invoice.class);
    try {
      String domain = "";
      if (invoice.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE
          && InvoiceToolService.isRefund(invoice)) {
        domain =
            Beans.get(AdvancePaymentRefundService.class)
                .createAdvancePaymentInvoiceSetDomain(invoice);
      } else {
        domain = Beans.get(InvoiceService.class).createAdvancePaymentInvoiceSetDomain(invoice);
      }

      response.setAttr("advancePaymentInvoiceSet", "domain", domain);

    } catch (Exception e) {
      TraceBackService.trace(e);
      response.setError(e.getMessage());
    }
  }

  /**
   * Called on partner and currency change, fill the domain of the field {@link
   * Invoice#advancePaymentInvoiceSet} with default values. The default values are every invoices
   * found in the domain.
   *
   * @param request
   * @param response
   */
  public void fillAdvancePaymentInvoiceSet(ActionRequest request, ActionResponse response) {

    Invoice invoice = request.getContext().asType(Invoice.class);
    try {
      if (invoice.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE
          && InvoiceToolService.isRefund(invoice)) {
        Set<Invoice> invoices =
            Beans.get(AdvancePaymentRefundService.class).getDefaultAdvancePaymentInvoice(invoice);
        response.setValue("advancePaymentInvoiceSet", invoices);
      } else {
        Set<Invoice> invoices =
            Beans.get(InvoiceService.class).getDefaultAdvancePaymentInvoice(invoice);
        response.setValue("advancePaymentInvoiceSet", invoices);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * set default value for automatic invoice printing
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void setDefaultMail(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    Company company = invoice.getCompany();
    Partner partner = invoice.getPartner();
    if (company != null && partner != null) {
      AccountingSituation accountingSituation =
          Beans.get(AccountingSituationService.class).getAccountingSituation(partner, company);
      if (accountingSituation != null) {
        response.setValue("invoiceAutomaticMail", accountingSituation.getInvoiceAutomaticMail());
        response.setValue(
            "invoiceMessageTemplate", accountingSituation.getInvoiceMessageTemplate());
        response.setValue(
            "invoiceAutomaticMailOnValidate",
            accountingSituation.getInvoiceAutomaticMailOnValidate());
        response.setValue(
            "invoiceMessageTemplateOnValidate",
            accountingSituation.getInvoiceMessageTemplateOnValidate());
      }
    }
  }

  /**
   * Called on trading name change. Set the default value for {@link Invoice#printingSettings}
   *
   * @param request
   * @param response
   */
  public void fillDefaultPrintingSettings(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      response.setValue(
          "printingSettings",
          Beans.get(TradingNameService.class)
              .getDefaultPrintingSettings(invoice.getTradingName(), invoice.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from invoice form view on partner change. Get the default price list for the invoice.
   * Call {@link PartnerPriceListService#getDefaultPriceList(Partner, int)}.
   *
   * @param request
   * @param response
   */
  public void fillPriceList(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      Partner partner = invoice.getPartner();
      if (partner == null) {
        response.setValue("priceList", null);
        return;
      }
      int priceListTypeSelect = Beans.get(InvoiceService.class).getPurchaseTypeOrSaleType(invoice);
      response.setValue(
          "priceList",
          Beans.get(PartnerPriceListService.class)
              .getDefaultPriceList(partner, priceListTypeSelect));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from invoice view on price list select. Call {@link
   * PartnerPriceListService#getPriceListDomain(Partner, int)}.
   *
   * @param request
   * @param response
   */
  public void changePriceListDomain(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      int priceListTypeSelect = Beans.get(InvoiceService.class).getPurchaseTypeOrSaleType(invoice);
      String domain =
          Beans.get(PartnerPriceListService.class)
              .getPriceListDomain(invoice.getPartner(), priceListTypeSelect);
      response.setAttr("priceList", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void massPaymentOnSupplierInvoices(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      if (!ObjectUtils.isEmpty(context.get("_ids"))) {
        List<Long> invoiceIdList =
            (List)
                (((List) context.get("_ids"))
                    .stream()
                        .filter(ObjectUtils::notEmpty)
                        .map(input -> Long.parseLong(input.toString()))
                        .collect(Collectors.toList()));

        List<Long> invoiceToPay =
            Beans.get(InvoicePaymentCreateService.class).getInvoiceIdsToPay(invoiceIdList);

        if (invoiceToPay.isEmpty()) {
          response.setError(I18n.get(AccountExceptionMessage.INVOICE_NO_INVOICE_TO_PAY));
        }

        response.setView(
            ActionView.define(I18n.get("Register a mass payment"))
                .model(InvoicePayment.class.getName())
                .add("form", "invoice-payment-mass-form")
                .param("popup", "reload")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("popup-save", "false")
                .param("forceEdit", "true")
                .context("_invoices", invoiceToPay)
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkPartnerBankDetailsList(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    response.setAttr(
        "$partnerBankDetailsListWarning",
        "hidden",
        Beans.get(InvoiceService.class).checkPartnerBankDetailsList(invoice));
  }

  public void refusalToPay(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      Beans.get(InvoiceService.class)
          .refusalToPay(
              Beans.get(InvoiceRepository.class).find(invoice.getId()),
              invoice.getReasonOfRefusalToPay(),
              invoice.getReasonOfRefusalToPayStr());
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setPfpValidatorUser(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      response.setValue(
          "pfpValidatorUser",
          Beans.get(InvoiceTermPfpToolService.class)
              .getPfpValidatorUser(invoice.getPartner(), invoice.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setPfpValidatorUserDomain(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      response.setAttr(
          "pfpValidatorUser",
          "domain",
          Beans.get(InvoiceTermService.class)
              .getPfpValidatorUserDomain(invoice.getPartner(), invoice.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void hideSendEmailPfpBtn(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    if (invoice.getPfpValidatorUser() == null) {
      return;
    }
    response.setAttr(
        "$isSelectedPfpValidatorEqualsPartnerPfpValidator",
        "value",
        Beans.get(InvoiceService.class).isSelectedPfpValidatorEqualsPartnerPfpValidator(invoice));
  }

  public void getInvoicePartnerDomain(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      Company company = invoice.getCompany();
      List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();
      int invoiceTypeSelect = Beans.get(InvoiceService.class).getPurchaseTypeOrSaleType(invoice);

      String domain =
          Beans.get(InvoiceDomainService.class)
              .getPartnerBaseDomain(company, invoice, invoiceTypeSelect);

      if (!(invoiceLineList == null || invoiceLineList.isEmpty())) {
        domain =
            Beans.get(PricedOrderDomainService.class)
                .getPartnerDomain(invoice, domain, invoiceTypeSelect);
      }

      response.setAttr("partner", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkDuplicateInvoice(ActionRequest request, ActionResponse response) {
    try {

      Invoice invoice = request.getContext().asType(Invoice.class);
      if (invoice != null) {
        Boolean isDuplicate = Beans.get(InvoiceControlService.class).isDuplicate(invoice);
        response.setAttr("$duplicateInvoiceNbrSameYear", "hidden", !isDuplicate);
        response.setAttr("$duplicateInvoiceNbrSameYear", "value", isDuplicate);

        if (isDuplicate) {
          response.setAttr("$supplierInvoiceNbStatic", "value", invoice.getSupplierInvoiceNb());
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  /**
   * Called from invoice form view upon changing the fiscalPosition Updates taxLine, taxEquiv and
   * prices by calling {@link InvoiceLineService#fillProductInformation(Invoice, InvoiceLine)} and
   * {@link InvoiceLineService#compute(Invoice, InvoiceLine)}
   *
   * @param request
   * @param response
   */
  public void updateLinesAfterFiscalPositionChange(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      if (invoice.getInvoiceLineList() != null) {
        Beans.get(InvoiceLineService.class).updateLinesAfterFiscalPositionChange(invoice);
      }
      response.setValue("invoiceLineList", invoice.getInvoiceLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void applyCutOffDates(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      InvoiceService invoiceService = Beans.get(InvoiceService.class);

      LocalDate cutOffStartDate =
          LocalDate.parse((String) request.getContext().get("cutOffStartDate"));
      LocalDate cutOffEndDate = LocalDate.parse((String) request.getContext().get("cutOffEndDate"));

      if (invoiceService.checkManageCutOffDates(invoice)) {
        invoiceService.applyCutOffDates(invoice, cutOffStartDate, cutOffEndDate);

        response.setValue("invoiceLineList", invoice.getInvoiceLineList());
      } else {
        response.setInfo(I18n.get(AccountExceptionMessage.NO_CUT_OFF_TO_APPLY));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setPaymentVisibility(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      User user = request.getUser();
      InvoiceVisibilityService invoiceVisibilityService = Beans.get(InvoiceVisibilityService.class);
      boolean paymentBtnVisible = invoiceVisibilityService.isPaymentButtonVisible(invoice);
      boolean paymentVouchersStatus = invoiceVisibilityService.getPaymentVouchersStatus(invoice);

      response.setAttr(
          "passedForPaymentValidationBtn",
          "hidden",
          !invoiceVisibilityService.isPfpButtonVisible(invoice, user, true));

      response.setAttr(
          "refusalToPayBtn",
          "hidden",
          !invoiceVisibilityService.isPfpButtonVisible(invoice, user, false));

      response.setAttr(
          "addPaymentBtn",
          "hidden",
          (paymentBtnVisible) ? paymentVouchersStatus : !paymentBtnVisible);

      response.setAttr(
          "registerPaymentBtn",
          "hidden",
          (paymentBtnVisible) ? !paymentVouchersStatus : !paymentBtnVisible);

      response.setAttr(
          "pfpValidatorUser", "hidden", !invoiceVisibilityService.isValidatorUserVisible(invoice));

      response.setAttr(
          "decisionPfpTakenDateTime",
          "hidden",
          !invoiceVisibilityService.isDecisionPfpVisible(invoice));

      response.setAttr(
          "sendPfpNotifyEmailBtn",
          "hidden",
          !invoiceVisibilityService.isSendNotifyVisible(invoice));

      response.setValue("$paymentVouchersOnInvoice", paymentBtnVisible);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validatePfp(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      Beans.get(InvoicePfpValidateService.class).validatePfp(invoice.getId());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void updateUnpaidInvoiceTerms(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);

      Beans.get(InvoiceService.class).updateUnpaidInvoiceTerms(invoice);

      response.setValue("invoiceTermList", invoice.getInvoiceTermList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showCustomerInvoiceLines(ActionRequest request, ActionResponse response) {
    try {
      String idList = getIdListString(request);
      response.setView(
          ActionView.define(I18n.get("Customer Invoice Line"))
              .model(InvoiceLine.class.getName())
              .add("grid", "invoice-line-menu-grid")
              .add("form", "invoice-line-menu-form")
              .param("search-filters", "invoice-line-filters")
              .domain(
                  "self.invoice.operationTypeSelect in (3,4) AND self.invoice.id in ("
                      + idList
                      + ")")
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  protected String getIdListString(ActionRequest request) {

    String idListString =
        Optional.ofNullable((List<Integer>) request.getContext().get("_ids"))
            .map(idList -> idList.stream().map(String::valueOf).collect(Collectors.joining(",")))
            .orElseGet(
                () ->
                    request
                        .getCriteria()
                        .createQuery(Invoice.class)
                        .select("id")
                        .fetch(0, 0)
                        .stream()
                        .map(m -> (Long) m.get("id"))
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")));

    if (idListString.isBlank()) {
      return "0";
    }

    return idListString;
  }

  public void checkInvoiceLinesAnalyticDistribution(
      ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

      if (!Beans.get(InvoiceService.class).checkInvoiceLinesAnalyticDistribution(invoice)) {
        response.setError(I18n.get(AccountExceptionMessage.INVOICE_WRONG_ANALYTIC_DISTRIBUTION));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkInvoiceLinesCutOffDates(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

      if (Beans.get(AppAccountService.class).getAppAccount().getManageCutOffPeriod()
          && !Beans.get(InvoiceService.class).checkInvoiceLinesCutOffDates(invoice)) {
        response.setError(I18n.get(AccountExceptionMessage.INVOICE_MISSING_CUT_OFF_DATE));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkInvoiceTerms(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

      if (!Beans.get(InvoiceService.class).checkInvoiceTerms(invoice)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.RECONCILE_NO_AVAILABLE_INVOICE_TERM));
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      response.setError(e.getLocalizedMessage());
    }
  }

  public void showSupplierInvoiceLines(ActionRequest request, ActionResponse response) {
    try {
      String idList = getIdListString(request);
      response.setView(
          ActionView.define(I18n.get("Supplier Invoice Line"))
              .model(InvoiceLine.class.getName())
              .add("grid", "invoice-line-menu-grid")
              .add("form", "invoice-line-menu-form")
              .param("search-filters", "invoice-line-filters")
              .domain(
                  "self.invoice.operationTypeSelect in (1,2) AND self.invoice.id in ("
                      + idList
                      + ")")
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void roundInvoiceTermPercentages(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());
      Beans.get(InvoiceTermService.class)
          .roundPercentages(invoice.getInvoiceTermList(), invoice.getInTaxTotal());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void updateInvoiceTerms(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      Beans.get(InvoiceService.class).updateInvoiceTermsParentFields(invoice);
      response.setValues(invoice);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateInvoiceTermPaymentMode(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      InvoiceTermToolService invoiceTermToolService = Beans.get(InvoiceTermToolService.class);

      invoice.getInvoiceTermList().stream()
          .filter(invoiceTermToolService::isNotReadonly)
          .forEach(it -> it.setPaymentMode(invoice.getPaymentMode()));

      response.setValue("invoiceTermList", invoice.getInvoiceTermList());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void updateInvoiceTermBankDetails(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);

      if (Beans.get(AppAccountService.class).getAppAccount().getAllowMultiInvoiceTerms()
          || CollectionUtils.isEmpty(invoice.getInvoiceTermList())
          || !Beans.get(InvoiceTermToolService.class)
              .isNotReadonly(invoice.getInvoiceTermList().get(0))) {
        return;
      }

      invoice.getInvoiceTermList().get(0).setBankDetails(invoice.getBankDetails());
      response.setValue("invoiceTermList", invoice.getInvoiceTermList());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkMultiCurrency(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);

      if (invoice.getPartner() != null
          && invoice.getPartner().getFinancialDiscount() != null
          && InvoiceToolService.isMultiCurrency(invoice)) {
        String partnerType =
            InvoiceToolService.isPurchase(invoice) ? I18n.get("Supplier") : I18n.get("Customer");

        response.setInfo(
            String.format(
                I18n.get(AccountExceptionMessage.INVOICE_MULTI_CURRENCY_FINANCIAL_DISCOUNT_PARTNER),
                partnerType.toLowerCase()));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void updateFinancialDiscount(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      InvoiceFinancialDiscountService invoiceFinancialDiscountService =
          Beans.get(InvoiceFinancialDiscountService.class);

      invoiceFinancialDiscountService.setFinancialDiscountInformations(invoice);
      invoiceFinancialDiscountService.updateFinancialDiscount(invoice);

      response.setValue("invoiceTermList", invoice.getInvoiceTermList());
      response.setValue("financialDiscount", invoice.getFinancialDiscount());
      response.setValue("legalNotice", invoice.getLegalNotice());
      response.setValue("financialDiscountRate", invoice.getFinancialDiscountRate());
      response.setValue("financialDiscountTotalAmount", invoice.getFinancialDiscountTotalAmount());
      response.setValue(
          "remainingAmountAfterFinDiscount", invoice.getRemainingAmountAfterFinDiscount());
      response.setValue(
          "financialDiscountDeadlineDate", invoice.getFinancialDiscountDeadlineDate());

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void generatePfpPartialTerms(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

      if (invoice != null && !CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
        if (Beans.get(InvoiceTermPfpService.class)
            .generateInvoiceTermsAfterPfpPartial(invoice.getInvoiceTermList())) {
          response.setReload(true);
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillEstimatedPaymentDate(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    try {
      if (invoice.getDueDate() == null) {
        return;
      }
      invoice = Beans.get(InvoiceService.class).computeEstimatedPaymentDate(invoice);
      response.setValues(invoice);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void managePfpStatusOnInvoiceTerm(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    try {
      if (invoice == null || CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
        return;
      }
      Integer pfpStatus =
          Beans.get(InvoiceTermPfpService.class)
              .checkOtherInvoiceTerms(invoice.getInvoiceTermList());
      if (pfpStatus != null) {
        response.setValue("pfpValidateStatusSelect", pfpStatus);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @ErrorException
  public void updateThirdPartyPayerPartner(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);

    Beans.get(InvoiceService.class).updateThirdPartyPayerPartner(invoice);

    response.setValue("invoiceTermList", invoice.getInvoiceTermList());
  }

  public void setInvoiceLinesScale(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    try {
      if (invoice == null || CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
        return;
      }

      Map<String, Map<String, Object>> attrsMap = new HashMap<>();
      Beans.get(InvoiceLineGroupService.class)
          .setInvoiceLineScale(invoice, attrsMap, "invoiceLineList.");
      Beans.get(InvoiceLineTaxGroupService.class)
          .setInvoiceLineTaxScale(invoice, attrsMap, "invoiceLineTaxList.");

      response.setAttrs(attrsMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageRefundFields(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    if (invoice == null) {
      return;
    }

    String createRefundBtn = "";
    String refundInvoiceList = "";
    String originalInvoice = "";

    try {
      if (InvoiceToolService.isRefund(invoice)) {
        if (invoice.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_DEFAULT) {
          createRefundBtn = AccountExceptionMessage.CREATE_REFUND_BTN_INVOICE;
          refundInvoiceList = AccountExceptionMessage.REFUND_INVOICE_LIST_INVOICE;
          originalInvoice = AccountExceptionMessage.ORIGINAL_INVOICE_INVOICE;
        } else {
          createRefundBtn = AccountExceptionMessage.CREATE_REFUND_BTN_ADVANCE_PAYMENT_REFUND;
          refundInvoiceList = AccountExceptionMessage.REFUND_INVOICE_LIST_ADVANCE_PAYMENT_REFUND;
          originalInvoice = AccountExceptionMessage.ORIGINAL_INVOICE_ADVANCE_PAYMENT_REFUND;
        }
      } else {
        createRefundBtn = AccountExceptionMessage.CREATE_REFUND_BTN_CLASSIC_REFUND;
        refundInvoiceList = AccountExceptionMessage.REFUND_INVOICE_LIST_CLASSIC_REFUND;
        originalInvoice = AccountExceptionMessage.ORIGINAL_INVOICE_CLASSIC_REFUND;
      }

      response.setAttr("createRefundBtn", "title", I18n.get(createRefundBtn));
      response.setAttr("refundInvoiceList", "title", I18n.get(refundInvoiceList));
      response.setAttr("originalInvoice", "title", I18n.get(originalInvoice));

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected Map<String, Object> getParamsMap(ActionRequest request) {
    Context context = request.getContext();
    Map<String, Object> params = new HashMap<>();
    Object invoiceId = Optional.ofNullable(context.get("_invoiceId")).orElse(context.get("id"));
    Invoice invoice = Beans.get(InvoiceRepository.class).find(Long.parseLong(invoiceId.toString()));
    Integer reportType =
        context.get("reportType") != null
            ? Integer.parseInt(context.get("reportType").toString())
            : InvoiceRepository.REPORT_TYPE_ORIGINAL_INVOICE;
    params.put("reportType", reportType);
    params.put("invoice", invoice);
    return params;
  }

  public void setInvoicePrintTemplate(ActionRequest request, ActionResponse response) {
    try {
      Map<String, Object> map = getParamsMap(request);
      Invoice invoice = (Invoice) map.get("invoice");
      Integer reportType = (Integer) map.get("reportType");
      if (reportType != null && reportType == 1) {
        return;
      }
      PrintingTemplate invoicePrintTemplate =
          Beans.get(AccountConfigService.class).getInvoicePrintTemplate(invoice.getCompany());
      response.setValue("$invoicePrintTemplate", invoicePrintTemplate);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void computeInvoiceAmounts(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);

    try {
      response.setValues(InvoiceToolService.computeInvoiceAmounts(invoice));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setSupplierInvoiceNbAndOriginDate(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      String supplierInvoiceNb = invoice.getSupplierInvoiceNb();
      LocalDate originDate = invoice.getOriginDate();

      if ((invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
              || invoice.getOperationTypeSelect()
                  == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND)
          && (supplierInvoiceNb == null || originDate == null)) {
        response.setView(
            ActionView.define(I18n.get("Supplier invoice"))
                .model(Invoice.class.getName())
                .add("form", "supplier-invoice-wizard-form")
                .param("popup", "reload")
                .param("forceEdit", "true")
                .param("show-toolbar", "false")
                .context("_showRecord", invoice.getId())
                .context("_supplierInvoiceNb", supplierInvoiceNb)
                .context("_originDate", originDate)
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void generateLatePaymentInvoice(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Invoice invoice = request.getContext().asType(Invoice.class);

    Invoice lateInvoice =
        Beans.get(LatePaymentInterestInvoiceService.class)
            .generateLatePaymentInterestInvoice(JPA.find(Invoice.class, invoice.getId()));

    response.setView(
        ActionView.define(I18n.get("Invoice"))
            .model(Invoice.class.getName())
            .add("form", "invoice-form")
            .add("grid", "invoice-grid")
            .context("_showRecord", lateInvoice.getId())
            .map());
  }

  public void applyGlobalDiscount(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Invoice invoice = request.getContext().asType(Invoice.class);
    Beans.get(InvoiceGlobalDiscountService.class).applyGlobalDiscountOnLines(invoice);
    response.setValues(invoice);
  }

  public void setDiscountDummies(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    response.setAttrs(Beans.get(InvoiceGlobalDiscountService.class).setDiscountDummies(invoice));
  }

  public void checkAnalyticAxis(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    try {
      Beans.get(InvoiceLineAnalyticService.class).checkAnalyticAxisByCompany(invoice);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  @ErrorException
  public void setCompanyTaxNumberDomain(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    Company company = invoice.getCompany();
    String domain = Beans.get(InvoiceDomainService.class).getCompanyTaxNumberDomain(company);
    response.setAttr("companyTaxNumber", "domain", domain);
  }

  @ErrorException
  public void setDefaultCompanyTaxNumber(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    TaxNumber taxNumber = Beans.get(InvoiceService.class).getDefaultCompanyTaxNumber(invoice);
    response.setValue("companyTaxNumber", taxNumber);
  }

  @ErrorException
  public void manageFiscalPositionFromCompanyTaxNumber(
      ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    FiscalPosition fiscalPosition =
        Beans.get(InvoiceService.class).manageFiscalPositionFromCompanyTaxNumber(invoice);
    response.setValue("fiscalPosition", fiscalPosition);
  }

  @ErrorException
  public void setFiscalPositionDomain(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    String domain = Beans.get(InvoiceDomainService.class).getFiscalPositionDomain(invoice);
    response.setAttr("fiscalPosition", "domain", domain);
  }

  @ErrorException
  public void generateInvoiceNote(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    Beans.get(InvoiceNoteService.class).generateInvoiceNote(invoice);
    response.setValues(invoice);
  }

  @ErrorException
  public void setBankDetailsDomain(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    String domain =
        Beans.get(BankDetailsDomainServiceAccount.class)
            .createDomainForBankDetails(
                invoice.getPartner(), invoice.getPaymentMode(), invoice.getCompany());
    response.setAttr("bankDetails", "domain", domain);
  }

  @ErrorException
  public void getDefaultBankDetails(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    BankDetails bankDetails =
        Beans.get(BankDetailsServiceAccount.class)
            .getDefaultBankDetails(
                invoice.getPartner(), invoice.getCompany(), invoice.getPaymentMode());
    response.setValue("bankDetails", bankDetails);
  }
}
