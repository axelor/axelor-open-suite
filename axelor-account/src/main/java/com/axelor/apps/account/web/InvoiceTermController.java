/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InvoiceTermController {

  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void computeCustomizedAmount(ActionRequest request, ActionResponse response) {
    InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
    try {
      BigDecimal total;
      if (invoiceTerm.getInvoice() != null) {
        total = invoiceTerm.getInvoice().getInTaxTotal();
      } else {
        total = invoiceTerm.getMoveLine().getDebit().max(invoiceTerm.getMoveLine().getCredit());
      }
      if (total.compareTo(BigDecimal.ZERO) == 0) {
        return;
      }
      BigDecimal amount =
          invoiceTerm
              .getPercentage()
              .multiply(total)
              .divide(
                  new BigDecimal(100),
                  AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                  RoundingMode.HALF_UP);
      response.setValue("amount", amount);
      response.setValue("amountRemaining", amount);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeCustomizedPercentage(ActionRequest request, ActionResponse response) {
    InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
    try {
      BigDecimal total;
      if (invoiceTerm.getInvoice() != null) {
        total = invoiceTerm.getInvoice().getInTaxTotal();
      } else {
        total = invoiceTerm.getMoveLine().getDebit().max(invoiceTerm.getMoveLine().getCredit());
      }
      if (total.compareTo(BigDecimal.ZERO) == 0) {
        return;
      }
      BigDecimal percentage =
          Beans.get(InvoiceTermService.class)
              .computeCustomizedPercentage(invoiceTerm.getAmount(), total);
      response.setValue("percentage", percentage);
      response.setValue("amountRemaining", invoiceTerm.getAmount());
      response.setValue(
          "isCustomized",
          invoiceTerm.getPaymentConditionLine() == null
              || percentage.compareTo(invoiceTerm.getPaymentConditionLine().getPaymentPercentage())
                  != 0);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void initInvoiceTermFromInvoice(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
      InvoiceTermService invoiceTermService = Beans.get(InvoiceTermService.class);
      if (request.getContext().getParent().get("_model").toString().contains("Invoice")) {
        Invoice invoice = request.getContext().getParent().asType(Invoice.class);
        if (invoice != null) {
          invoiceTermService.initCustomizedInvoiceTerm(invoice, invoiceTerm);
          response.setValues(invoiceTerm);
        }
      } else {
        if (request.getContext().getParent().get("_model").toString().contains("MoveLine")) {
          MoveLine moveLine = request.getContext().getParent().asType(MoveLine.class);
          if (moveLine != null) {
            invoiceTermService.initCustomizedInvoiceTerm(moveLine, invoiceTerm);
            response.setValues(invoiceTerm);
          }
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void refusalToPay(ActionRequest request, ActionResponse response) {
    try {
      List<Long> invoiceTermIds = (List<Long>) request.getContext().get("_ids");
      Integer invoiceTermId = (Integer) request.getContext().get("_id");
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
      if (ObjectUtils.notEmpty(invoiceTermId) && ObjectUtils.isEmpty(invoiceTermIds)) {

        if (invoiceTerm.getInvoice() != null
            && invoiceTerm.getInvoice().getCompany() != null
            && invoiceTerm.getReasonOfRefusalToPay() != null) {
          Beans.get(InvoiceTermService.class)
              .refusalToPay(
                  Beans.get(InvoiceTermRepository.class).find(invoiceTerm.getId()),
                  invoiceTerm.getReasonOfRefusalToPay(),
                  invoiceTerm.getReasonOfRefusalToPayStr());

          response.setCanClose(true);
        }
      } else if (ObjectUtils.isEmpty(invoiceTermId)) {
        if (ObjectUtils.isEmpty(invoiceTermIds)) {
          response.setError(I18n.get(IExceptionMessage.INVOICE_INVOICE_TERM_MASS_UPDATE_NO_RECORD));
          return;
        }
        Integer recordsSelected = invoiceTermIds.size();
        Integer recordsRefused =
            Beans.get(InvoiceTermService.class)
                .massRefusePfp(
                    invoiceTermIds,
                    invoiceTerm.getReasonOfRefusalToPay(),
                    invoiceTerm.getReasonOfRefusalToPayStr());
        response.setFlash(
            String.format(
                I18n.get(IExceptionMessage.INVOICE_INVOICE_TERM_MASS_REFUSAL_SUCCESSFUL),
                recordsRefused,
                recordsSelected));
        response.setCanClose(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setPfpValidatorUserDomain(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);

      response.setAttr(
          "pfpValidatorUser",
          "domain",
          Beans.get(InvoiceService.class).getPfpValidatorUserDomain(invoiceTerm.getInvoice()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void hideSendEmailPfpBtn(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);

      if (invoiceTerm.getPfpValidatorUser() != null) {
        response.setAttr(
            "$isSelectedPfpValidatorEqualsPartnerPfpValidator",
            "value",
            invoiceTerm
                .getPfpValidatorUser()
                .equals(
                    Beans.get(InvoiceService.class).getPfpValidatorUser(invoiceTerm.getInvoice())));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void massValidatePfp(ActionRequest request, ActionResponse response) {
    try {
      List<Long> invoiceTermIds = (List<Long>) request.getContext().get("_ids");
      if (ObjectUtils.isEmpty(invoiceTermIds)) {
        response.setError(I18n.get(IExceptionMessage.INVOICE_INVOICE_TERM_MASS_UPDATE_NO_RECORD));
        return;
      }
      Integer recordsSelected = invoiceTermIds.size();
      Integer recordsUpdated = Beans.get(InvoiceTermService.class).massValidatePfp(invoiceTermIds);
      response.setFlash(
          String.format(
              I18n.get(IExceptionMessage.INVOICE_INVOICE_TERM_MASS_VALIDATION_SUCCESSFUL),
              recordsUpdated,
              recordsSelected));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void pfpPartialReasonConfirm(ActionRequest request, ActionResponse response) {
    try {
      if (ObjectUtils.isEmpty(request.getContext().get("_id"))) {
        response.setError(I18n.get(IExceptionMessage.INVOICE_INVOICE_TERM_NOT_SAVED));
        return;
      }
      InvoiceTerm originalInvoiceTerm =
          Beans.get(InvoiceTermRepository.class)
              .find(Long.valueOf((Integer) request.getContext().get("_id")));
      BigDecimal pfpGrantedAmount = (BigDecimal) request.getContext().get("pfpGrantedAmount");
      if (pfpGrantedAmount.compareTo(BigDecimal.ZERO) == 0) {
        response.setError(I18n.get(IExceptionMessage.INVOICE_INVOICE_TERM_PFP_GRANTED_AMOUNT_ZERO));
        return;
      }

      BigDecimal invoiceAmount = originalInvoiceTerm.getAmount();
      PfpPartialReason partialReason =
          (PfpPartialReason) request.getContext().get("pfpPartialReason");

      if (pfpGrantedAmount.compareTo(invoiceAmount) >= 0) {
        response.setValue("pfpGrantedAmount", 0);
        response.setFlash(I18n.get(IExceptionMessage.INVOICE_INVOICE_TERM_INVALID_GRANTED_AMOUNT));
        return;
      }
      if (ObjectUtils.isEmpty(partialReason)) {
        response.setError(I18n.get(IExceptionMessage.INVOICE_INVOICE_TERM_PARTIAL_REASON_EMPTY));
        return;
      }
      Beans.get(InvoiceTermService.class)
          .generateInvoiceTerm(originalInvoiceTerm, invoiceAmount, pfpGrantedAmount, partialReason);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
