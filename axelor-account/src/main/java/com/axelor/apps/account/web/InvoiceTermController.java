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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermAccountRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.ContextTool;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InvoiceTermController {

  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void computeCustomizedAmount(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);

      if (request.getContext().getParent() != null
          && request.getContext().getParent().containsKey("_model")) {
        List<InvoiceTerm> invoiceTermList =
            this.getInvoiceTermList(request.getContext().getParent());
        invoiceTermList.remove(invoiceTerm);

        this.setParentContext(request, invoiceTerm);
        BigDecimal total = this.getCustomizedTotal(request.getContext().getParent());

        if (Beans.get(InvoiceTermService.class)
            .setCustomizedAmounts(invoiceTerm, invoiceTermList, total)) {
          response.setValue("amount", invoiceTerm.getAmount());
          response.setValue("amountRemaining", invoiceTerm.getAmountRemaining());
          response.setValue("companyAmount", invoiceTerm.getCompanyAmount());
          response.setValue("companyAmountRemaining", invoiceTerm.getCompanyAmountRemaining());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeCustomizedPercentage(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);

      if (request.getContext().getParent() != null
          && request.getContext().getParent().containsKey("_model")) {
        InvoiceTermService invoiceTermService = Beans.get(InvoiceTermService.class);
        BigDecimal total = this.getCustomizedTotal(request.getContext().getParent());

        if (total.compareTo(BigDecimal.ZERO) == 0) {
          return;
        }

        this.setParentContext(request, invoiceTerm);

        BigDecimal percentage =
            invoiceTermService.computeCustomizedPercentage(invoiceTerm.getAmount(), total);

        invoiceTerm.setPercentage(percentage);
        invoiceTerm.setAmountRemaining(invoiceTerm.getAmount());
        invoiceTermService.computeCompanyAmounts(invoiceTerm, true, false);

        response.setValue("percentage", percentage);
        response.setValue("amountRemaining", invoiceTerm.getAmountRemaining());
        response.setValue("companyAmount", invoiceTerm.getCompanyAmount());
        response.setValue("companyAmountRemaining", invoiceTerm.getCompanyAmountRemaining());
        response.setValue(
            "isCustomized",
            invoiceTerm.getPaymentConditionLine() == null
                || percentage.compareTo(
                        invoiceTerm.getPaymentConditionLine().getPaymentPercentage())
                    != 0);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected BigDecimal getCustomizedTotal(Context parentContext) {
    if (parentContext.get("_model").equals(Invoice.class.getName())) {
      Invoice invoice = parentContext.asType(Invoice.class);
      return invoice.getInTaxTotal();
    } else if (parentContext.get("_model").equals(MoveLine.class.getName())) {
      MoveLine moveLine = parentContext.asType(MoveLine.class);
      return Beans.get(InvoiceTermService.class).getTotalInvoiceTermsAmount(moveLine);
    } else {
      return BigDecimal.ZERO;
    }
  }

  protected BigDecimal getCustomizedCompanyTotal(Context parentContext) {
    if (parentContext.get("_model").equals(Invoice.class.getName())) {
      Invoice invoice = parentContext.asType(Invoice.class);
      return invoice.getCompanyInTaxTotal();
    } else if (parentContext.get("_model").equals(MoveLine.class.getName())) {
      MoveLine moveLine = parentContext.asType(MoveLine.class);
      return Beans.get(InvoiceTermService.class).getTotalInvoiceTermsAmount(moveLine);
    } else {
      return BigDecimal.ZERO;
    }
  }

  protected List<InvoiceTerm> getInvoiceTermList(Context parentContext) {
    if (parentContext.get("_model").equals(Invoice.class.getName())) {
      Invoice invoice = parentContext.asType(Invoice.class);
      return invoice.getInvoiceTermList();
    } else if (parentContext.get("_model").equals(MoveLine.class.getName())) {
      MoveLine moveLine = parentContext.asType(MoveLine.class);
      return moveLine.getInvoiceTermList();
    } else {
      return new ArrayList<>();
    }
  }

  public void initInvoiceTerm(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
      InvoiceTermService invoiceTermService = Beans.get(InvoiceTermService.class);

      this.setParentContext(request, invoiceTerm);

      Invoice invoice = invoiceTerm.getInvoice();
      MoveLine moveLine = invoiceTerm.getMoveLine();
      Move move = moveLine != null ? moveLine.getMove() : null;

      if (invoice == null && request.getContext().containsKey("_invoiceId")) {
        invoice =
            Beans.get(InvoiceRepository.class)
                .find(Long.valueOf((Integer) request.getContext().get("_invoiceId")));
        invoiceTerm.setInvoice(invoice);
      }

      if (invoice == null && moveLine != null && move != null) {
        invoiceTermService.initCustomizedInvoiceTerm(moveLine, invoiceTerm, move);
      } else if (invoice != null) {
        invoiceTermService.initCustomizedInvoiceTerm(invoice, invoiceTerm);
      }

      invoiceTermService.setParentFields(invoiceTerm, move, moveLine, invoice);
      response.setValues(invoiceTerm);
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

        if (invoiceTerm.getCompany() != null && invoiceTerm.getReasonOfRefusalToPay() != null) {
          Beans.get(InvoiceTermPfpService.class)
              .refusalToPay(
                  Beans.get(InvoiceTermRepository.class).find(invoiceTerm.getId()),
                  invoiceTerm.getReasonOfRefusalToPay(),
                  invoiceTerm.getReasonOfRefusalToPayStr());

          response.setCanClose(true);
        }
      } else if (ObjectUtils.isEmpty(invoiceTermId)) {
        if (ObjectUtils.isEmpty(invoiceTermIds)) {
          response.setError(
              I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_MASS_UPDATE_NO_RECORD));
          return;
        }
        Integer recordsSelected = invoiceTermIds.size();
        Integer recordsRefused =
            Beans.get(InvoiceTermPfpService.class)
                .massRefusePfp(
                    invoiceTermIds,
                    invoiceTerm.getReasonOfRefusalToPay(),
                    invoiceTerm.getReasonOfRefusalToPayStr());
        response.setInfo(
            String.format(
                I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_MASS_REFUSAL_SUCCESSFUL),
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
          Beans.get(InvoiceTermService.class)
              .getPfpValidatorUserDomain(invoiceTerm.getPartner(), invoiceTerm.getCompany()));
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
                    Beans.get(InvoiceTermService.class)
                        .getPfpValidatorUser(invoiceTerm.getPartner(), invoiceTerm.getCompany())));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validatePfp(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceterm =
          Beans.get(InvoiceTermRepository.class)
              .find(request.getContext().asType(InvoiceTerm.class).getId());
      Beans.get(InvoiceTermPfpService.class).validatePfp(invoiceterm, AuthUtils.getUser());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void massValidatePfp(ActionRequest request, ActionResponse response) {
    try {
      List<Long> invoiceTermIds = (List<Long>) request.getContext().get("_ids");
      if (ObjectUtils.isEmpty(invoiceTermIds)) {
        response.setError(
            I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_MASS_UPDATE_NO_RECORD));
        return;
      }
      Integer recordsSelected = invoiceTermIds.size();
      Integer recordsUpdated =
          Beans.get(InvoiceTermPfpService.class).massValidatePfp(invoiceTermIds);
      response.setInfo(
          String.format(
              I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_MASS_VALIDATION_SUCCESSFUL),
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
        response.setError(I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_NOT_SAVED));
        return;
      }

      InvoiceTerm originalInvoiceTerm =
          Beans.get(InvoiceTermRepository.class)
              .find(Long.valueOf((Integer) request.getContext().get("_id")));

      BigDecimal grantedAmount = new BigDecimal((String) request.getContext().get("grantedAmount"));
      if (grantedAmount.signum() == 0) {
        response.setError(
            I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_PFP_GRANTED_AMOUNT_ZERO));
        return;
      }

      BigDecimal invoiceAmount = originalInvoiceTerm.getAmount();
      if (grantedAmount.compareTo(invoiceAmount) >= 0) {
        response.setValue("$grantedAmount", originalInvoiceTerm.getAmountRemaining());
        response.setInfo(
            I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_INVALID_GRANTED_AMOUNT));
        return;
      }

      PfpPartialReason partialReason =
          (PfpPartialReason) request.getContext().get("pfpPartialReason");
      if (ObjectUtils.isEmpty(partialReason)) {
        response.setError(
            I18n.get(AccountExceptionMessage.INVOICE_INVOICE_TERM_PARTIAL_REASON_EMPTY));
        return;
      }

      Beans.get(InvoiceTermPfpService.class)
          .generateInvoiceTerm(originalInvoiceTerm, invoiceAmount, grantedAmount, partialReason);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void selectTerm(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
      invoiceTerm = Beans.get(InvoiceTermRepository.class).find(invoiceTerm.getId());
      Beans.get(InvoiceTermService.class).toggle(invoiceTerm, true);
      response.setValues(invoiceTerm);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void selectPartnerTerm(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
      if (invoiceTerm.getMoveLine().getPartner() != null
          && invoiceTerm.getPaymentSession() != null) {
        List<InvoiceTerm> invoiceTermList =
            Beans.get(InvoiceTermAccountRepository.class)
                .findByPaymentSessionAndPartner(
                    invoiceTerm.getPaymentSession(), invoiceTerm.getMoveLine().getPartner());
        if (!CollectionUtils.isEmpty(invoiceTermList)) {
          InvoiceTermService invoiceTermService = Beans.get(InvoiceTermService.class);
          InvoiceTermRepository invoiceTermRepository = Beans.get(InvoiceTermRepository.class);
          for (InvoiceTerm invoiceTermTemp : invoiceTermList) {
            invoiceTermTemp = invoiceTermRepository.find(invoiceTermTemp.getId());
            invoiceTermService.toggle(invoiceTermTemp, true);
          }
        }
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void unselectTerm(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
      invoiceTerm = Beans.get(InvoiceTermRepository.class).find(invoiceTerm.getId());
      Beans.get(InvoiceTermService.class).toggle(invoiceTerm, false);
      response.setValues(invoiceTerm);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void unselectPartnerTerm(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
      if (invoiceTerm.getMoveLine().getPartner() != null
          && invoiceTerm.getPaymentSession() != null) {
        List<InvoiceTerm> invoiceTermList =
            Beans.get(InvoiceTermAccountRepository.class)
                .findByPaymentSessionAndPartner(
                    invoiceTerm.getPaymentSession(), invoiceTerm.getMoveLine().getPartner());
        if (!CollectionUtils.isEmpty(invoiceTermList)) {
          InvoiceTermService invoiceTermService = Beans.get(InvoiceTermService.class);
          InvoiceTermRepository invoiceTermRepository = Beans.get(InvoiceTermRepository.class);
          for (InvoiceTerm invoiceTermTemp : invoiceTermList) {
            invoiceTermTemp = invoiceTermRepository.find(invoiceTermTemp.getId());
            invoiceTermService.toggle(invoiceTermTemp, false);
          }
        }
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeFinancialDiscount(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);

      this.setParentContext(request, invoiceTerm);

      MoveLine moveLine = invoiceTerm.getMoveLine();
      if (moveLine != null && moveLine.getFinancialDiscount() != null) {
        Beans.get(InvoiceTermService.class)
            .computeFinancialDiscount(
                invoiceTerm,
                moveLine.getCredit().max(moveLine.getDebit()),
                moveLine.getFinancialDiscount(),
                moveLine.getFinancialDiscountTotalAmount(),
                moveLine.getRemainingAmountAfterFinDiscount());
      }

      response.setValue("applyFinancialDiscount", invoiceTerm.getApplyFinancialDiscount());
      response.setValue("financialDiscount", invoiceTerm.getFinancialDiscount());
      response.setValue(
          "financialDiscountDeadlineDate", invoiceTerm.getFinancialDiscountDeadlineDate());
      response.setValue("financialDiscountAmount", invoiceTerm.getFinancialDiscountAmount());
      response.setValue(
          "remainingAmountAfterFinDiscount", invoiceTerm.getRemainingAmountAfterFinDiscount());
      response.setValue(
          "amountRemainingAfterFinDiscount", invoiceTerm.getAmountRemainingAfterFinDiscount());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setPfpStatus(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);

      this.setParentContext(request, invoiceTerm);

      if (invoiceTerm.getMoveLine() == null && request.getContext().containsKey("_moveLineId")) {
        invoiceTerm.setMoveLine(
            Beans.get(MoveLineRepository.class)
                .find(Long.valueOf((Integer) request.getContext().get("_moveLineId"))));
      }

      Beans.get(InvoiceTermService.class).setPfpStatus(invoiceTerm, null);
      response.setValue("pfpValidateStatusSelect", invoiceTerm.getPfpValidateStatusSelect());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void isMultiCurrency(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);

      this.setParentContext(request, invoiceTerm);

      boolean isMultiCurrency = Beans.get(InvoiceTermService.class).isMultiCurrency(invoiceTerm);

      response.setValue("$isMultiCurrency", isMultiCurrency);
      MoveLine moveLine = invoiceTerm.getMoveLine();
      Invoice invoice = invoiceTerm.getInvoice();

      if (Beans.get(InvoiceToolService.class).isMultiCurrency(invoice)
          || (invoice == null
              && moveLine != null
              && Beans.get(MoveToolService.class).isMultiCurrency(moveLine.getMove()))) {
        response.setAttr("amount", "title", I18n.get("Amount in currency"));
        response.setAttr("amountRemaining", "title", I18n.get("Amount remaining in currency"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  protected void setParentContext(ActionRequest request, InvoiceTerm invoiceTerm) {
    this.setInvoice(request, invoiceTerm);

    this.setMoveLine(request, invoiceTerm);

    this.setMove(request, invoiceTerm.getMoveLine());
  }

  protected void setInvoice(ActionRequest request, InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() == null) {
      invoiceTerm.setInvoice(ContextTool.getContextParent(request.getContext(), Invoice.class, 1));
    }
  }

  protected void setMove(ActionRequest request, MoveLine moveLine) {
    if (moveLine != null && (moveLine.getMove() == null || moveLine.getId() == null)) {
      moveLine.setMove(ContextTool.getContextParent(request.getContext(), Move.class, 2));
    }
  }

  protected void setMoveLine(ActionRequest request, InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getMoveLine() == null) {
      invoiceTerm.setMoveLine(
          ContextTool.getContextParent(request.getContext(), MoveLine.class, 1));
    }
  }

  public void addLinkedFiles(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
      if (invoiceTerm.getMoveLine() != null
          && invoiceTerm.getMoveLine().getMove() != null
          && invoiceTerm.getMoveLine().getMove().getId() != null) {
        List<DMSFile> dmsFileList =
            Beans.get(DMSFileRepository.class)
                .all()
                .filter(
                    "self.isDirectory = false AND self.relatedId = "
                        + invoiceTerm.getMoveLine().getMove().getId()
                        + " AND self.relatedModel = 'com.axelor.apps.account.db.Move'")
                .fetch();
        response.setValue("$invoiceTermMoveFile", dmsFileList);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
