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
package com.axelor.apps.account.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PayVoucherDueElement;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherConfirmService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherControlService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherLoadService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherSequenceService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PaymentVoucherController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // Called on onSave event
  public void paymentVoucherSetNum(ActionRequest request, ActionResponse response)
      throws AxelorException {

    PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);

    if (Strings.isNullOrEmpty(paymentVoucher.getRef())) {

      response.setValue(
          "ref", Beans.get(PaymentVoucherSequenceService.class).getReference(paymentVoucher));
    }
  }

  // Loading move lines of the selected partner (1st O2M)
  public void loadInvoiceTerms(ActionRequest request, ActionResponse response) {

    PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);

    try {
      List<PayVoucherDueElement> pvDueElementList =
          Beans.get(PaymentVoucherLoadService.class).searchDueElements(paymentVoucher);
      response.setValue("payVoucherDueElementList", pvDueElementList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // Filling lines to pay (2nd O2M)
  public void loadSelectedLines(ActionRequest request, ActionResponse response) {

    PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);

    try {
      boolean generateAll =
          Beans.get(PaymentVoucherLoadService.class).loadSelectedLines(paymentVoucher);
      response.setValue("payVoucherDueElementList", paymentVoucher.getPayVoucherDueElementList());
      response.setValue(
          "payVoucherElementToPayList", paymentVoucher.getPayVoucherElementToPayList());

      if (!generateAll) {
        response.setInfo(I18n.get(AccountExceptionMessage.PAYMENT_VOUCHER_NOT_GENERATE_ALL));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // Reset imputation
  public void resetImputation(ActionRequest request, ActionResponse response) {

    PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);

    try {
      Beans.get(PaymentVoucherLoadService.class).resetImputation(paymentVoucher);

      response.setValue("payVoucherDueElementList", paymentVoucher.getPayVoucherDueElementList());
      response.setValue(
          "payVoucherElementToPayList", paymentVoucher.getPayVoucherElementToPayList());
      response.setValue("remainingAmount", paymentVoucher.getPaidAmount());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void askPaymentVoucher(ActionRequest request, ActionResponse response) {
    PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);

    if (paymentVoucher.getHasAutoInput()) {
      PaymentMode paymentMode = paymentVoucher.getPaymentMode();
      Company company = paymentVoucher.getCompany();
      BankDetails companyBankDetails = paymentVoucher.getCompanyBankDetails();
      try {
        Journal journal =
            Beans.get(PaymentModeService.class)
                .getPaymentModeJournal(paymentMode, company, companyBankDetails);
        if (journal.getExcessPaymentOk()) {
          response.setAlert(I18n.get("No items have been selected. Do you want to continue?"));
        }
        if (!Beans.get(PaymentVoucherControlService.class).controlMoveAmounts(paymentVoucher)) {
          response.setError(
              I18n.get(
                  "Some move amounts have been changed since the imputation. Please remake the imputation."));
        }
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      }
    }
  }

  // Confirm the payment voucher
  public void confirmPaymentVoucher(ActionRequest request, ActionResponse response) {

    PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
    paymentVoucher = Beans.get(PaymentVoucherRepository.class).find(paymentVoucher.getId());

    try {
      Beans.get(PaymentVoucherConfirmService.class).confirmPaymentVoucher(paymentVoucher);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printPaymentVoucher(ActionRequest request, ActionResponse response)
      throws AxelorException {

    PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);

    String name = I18n.get("Payment voucher");
    if (!Strings.isNullOrEmpty(paymentVoucher.getReceiptNo())) {
      name += " " + paymentVoucher.getReceiptNo();
    }

    String fileLink =
        ReportFactory.createReport(IReport.PAYMENT_VOUCHER, name + "-${date}")
            .addParam("PaymentVoucherId", paymentVoucher.getId())
            .addParam(
                "Timezone",
                paymentVoucher.getCompany() != null
                    ? paymentVoucher.getCompany().getTimezone()
                    : null)
            .generate()
            .getFileLink();

    logger.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
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
    PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
    PaymentMode paymentMode = paymentVoucher.getPaymentMode();
    Company company = paymentVoucher.getCompany();
    Partner partner = paymentVoucher.getPartner();
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

  public void initFromInvoice(ActionRequest request, ActionResponse response) {
    PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
    @SuppressWarnings("unchecked")
    Invoice invoice =
        Mapper.toBean(Invoice.class, (Map<String, Object>) request.getContext().get("_invoice"));
    invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());

    try {
      Beans.get(PaymentVoucherLoadService.class).initFromInvoice(paymentVoucher, invoice);
      response.setValues(paymentVoucher);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void reloadPaymentVoucher(ActionRequest request, ActionResponse response) {
    try {
      PaymentVoucher contextPaymentVoucher = request.getContext().asType(PaymentVoucher.class);
      PaymentVoucher paymentVoucher =
          Beans.get(PaymentVoucherRepository.class).find(contextPaymentVoucher.getId());
      if (paymentVoucher != null) {

        Beans.get(PaymentVoucherLoadService.class)
            .reloadElementToPayList(paymentVoucher, contextPaymentVoucher);
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setReceiptDisplay(ActionRequest request, ActionResponse response) {
    try {
      PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
      boolean displayReceipt =
          Beans.get(PaymentVoucherControlService.class).isReceiptDisplayed(paymentVoucher);

      response.setAttr("receiptNo", "hidden", !displayReceipt);
      response.setAttr("printPaymentVoucherBtn", "hidden", !displayReceipt);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
