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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.tool.StringTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;

@Singleton
public class InvoicePaymentController {

  @Inject private InvoiceRepository invoiceRepo;

  public void cancelInvoicePayment(ActionRequest request, ActionResponse response) {
    InvoicePayment invoicePayment = request.getContext().asType(InvoicePayment.class);

    invoicePayment = Beans.get(InvoicePaymentRepository.class).find(invoicePayment.getId());
    try {
      Beans.get(InvoicePaymentCancelService.class).cancel(invoicePayment);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
    response.setReload(true);
  }

  // filter the payment mode depending on the target invoice

  @SuppressWarnings("unchecked")
  public void filterPaymentMode(ActionRequest request, ActionResponse response) {
    Map<String, Object> partialInvoice = (Map<String, Object>) request.getContext().get("_invoice");
    Invoice invoice = invoiceRepo.find(Long.valueOf(partialInvoice.get("id").toString()));
    PaymentMode paymentMode = invoice.getPaymentMode();
    if (invoice != null && paymentMode != null) {
      if (paymentMode.getInOutSelect() != null) {
        response.setAttr(
            "paymentMode", "domain", "self.inOutSelect = " + paymentMode.getInOutSelect());
      }
    }
  }

  /**
   * Create the domain for companyBankDetails field.
   *
   * @param request
   * @param response
   */
  @SuppressWarnings("unchecked")
  public void filterBankDetails(ActionRequest request, ActionResponse response) {
    InvoicePayment invoicePayment = request.getContext().asType(InvoicePayment.class);
    Map<String, Object> partialInvoice = (Map<String, Object>) request.getContext().get("_invoice");

    Invoice invoice = invoiceRepo.find(((Integer) partialInvoice.get("id")).longValue());
    Company company = invoice.getCompany();
    List<BankDetails> bankDetailsList =
        Beans.get(InvoicePaymentToolService.class)
            .findCompatibleBankDetails(company, invoicePayment);
    if (bankDetailsList.isEmpty()) {
      response.setAttr("companyBankDetails", "domain", "self.id IN (0)");
    } else {
      String idList = StringTool.getIdListString(bankDetailsList);
      response.setAttr("companyBankDetails", "domain", "self.id IN (" + idList + ")");
    }
  }

  /**
   * On payment mode change, fill the bank details field if we find precisely one bank details
   * available in the payment mode for the current company.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  @SuppressWarnings("unchecked")
  public void fillBankDetails(ActionRequest request, ActionResponse response)
      throws AxelorException {
    InvoicePayment invoicePayment = request.getContext().asType(InvoicePayment.class);
    Map<String, Object> partialInvoice = (Map<String, Object>) request.getContext().get("_invoice");

    Invoice invoice = invoiceRepo.find(((Integer) partialInvoice.get("id")).longValue());
    PaymentMode paymentMode = invoicePayment.getPaymentMode();
    Company company = invoice.getCompany();
    List<BankDetails> bankDetailsList =
        Beans.get(InvoicePaymentToolService.class)
            .findCompatibleBankDetails(company, invoicePayment);
    if (bankDetailsList.size() == 1) {
      response.setValue("companyBankDetails", bankDetailsList.get(0));
    } else {
      response.setValue("companyBankDetails", null);
    }
    Partner partner = invoice.getPartner();
    if (company == null) {
      return;
    }
    if (partner != null) {
      partner = Beans.get(PartnerRepository.class).find(partner.getId());
    }
    BankDetails defaultBankDetails =
        Beans.get(BankDetailsService.class)
            .getDefaultCompanyBankDetails(company, paymentMode, partner, null);
    response.setValue("bankDetails", defaultBankDetails);
  }
}
