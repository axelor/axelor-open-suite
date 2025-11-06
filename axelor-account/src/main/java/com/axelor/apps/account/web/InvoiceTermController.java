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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.account.db.repo.InvoiceTermAccountRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.BankDetailsDomainServiceAccount;
import com.axelor.apps.account.service.invoice.BankDetailsServiceAccount;
import com.axelor.apps.account.service.invoice.InvoiceTermDateComputeService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpValidateService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoiceterm.InvoiceTermGroupService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.exception.ErrorException;
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
import com.axelor.utils.helpers.ContextHelper;
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

  public void onNew(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
      this.setParentContext(request, invoiceTerm);

      InvoiceTermGroupService invoiceTermGroupService = Beans.get(InvoiceTermGroupService.class);

      response.setValues(invoiceTermGroupService.getOnNewValuesMap(invoiceTerm));
      response.setAttrs(invoiceTermGroupService.getOnNewAttrsMap(invoiceTerm));

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  @ErrorException
  public void onLoad(ActionRequest request, ActionResponse response) {
    InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
    this.setParentContext(request, invoiceTerm);

    InvoiceTermGroupService invoiceTermGroupService = Beans.get(InvoiceTermGroupService.class);

    response.setValues(invoiceTermGroupService.getOnLoadValuesMap(invoiceTerm));
    response.setAttrs(invoiceTermGroupService.getOnLoadAttrsMap(invoiceTerm));
  }

  public void checkPfpValidatorUser(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
      this.setParentContext(request, invoiceTerm);

      InvoiceTermGroupService invoiceTermGroupService = Beans.get(InvoiceTermGroupService.class);

      response.setValues(invoiceTermGroupService.checkPfpValidatorUser(invoiceTerm));
      response.setAttrs(invoiceTermGroupService.getOnNewAttrsMap(invoiceTerm));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void computeAmountPaid(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
      if (invoiceTerm == null
          || (invoiceTerm.getApplyFinancialDiscount()
              && invoiceTerm.getIsSelectedOnPaymentSession())) {
        return;
      }

      BigDecimal amountPaid = BigDecimal.ZERO;
      if (invoiceTerm.getApplyFinancialDiscount() && !invoiceTerm.getIsSelectedOnPaymentSession()) {
        amountPaid =
            invoiceTerm.getPaymentAmount().subtract(invoiceTerm.getFinancialDiscountAmount());
      } else if (!invoiceTerm.getApplyFinancialDiscount()
          && invoiceTerm.getIsSelectedOnPaymentSession()) {
        amountPaid = invoiceTerm.getPaymentAmount();
      }
      response.setValue("amountPaid", amountPaid);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @ErrorException
  public void amountOnChange(ActionRequest request, ActionResponse response) {
    InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
    this.setParentContext(request, invoiceTerm);

    InvoiceTermGroupService invoiceTermGroupService = Beans.get(InvoiceTermGroupService.class);

    response.setValues(invoiceTermGroupService.getAmountOnChangeValuesMap(invoiceTerm));
  }

  @ErrorException
  public void percentageOnChange(ActionRequest request, ActionResponse response) {
    InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
    this.setParentContext(request, invoiceTerm);

    InvoiceTermGroupService invoiceTermGroupService = Beans.get(InvoiceTermGroupService.class);

    response.setValues(invoiceTermGroupService.getPercentageOnChangeValuesMap(invoiceTerm));
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

      InvoiceTermGroupService invoiceTermGroupService = Beans.get(InvoiceTermGroupService.class);

      response.setValues(invoiceTermGroupService.setPfpValidatorUserDomainValuesMap(invoiceTerm));
      response.setAttrs(invoiceTermGroupService.setPfpValidatorUserDomainAttrsMap(invoiceTerm));

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validatePfp(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceterm = request.getContext().asType(InvoiceTerm.class);
      Beans.get(InvoiceTermPfpValidateService.class).validatePfp(invoiceterm, AuthUtils.getUser());
      response.setValues(invoiceterm);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validatePfpProcess(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTerm invoiceterm = request.getContext().asType(InvoiceTerm.class);
      invoiceterm = Beans.get(InvoiceTermRepository.class).find(invoiceterm.getId());
      Beans.get(InvoiceTermPfpValidateService.class).validatePfp(invoiceterm, AuthUtils.getUser());
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
          Beans.get(InvoiceTermPfpValidateService.class).massValidatePfp(invoiceTermIds);
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

      Beans.get(InvoiceTermPfpValidateService.class)
          .initPftPartialValidation(originalInvoiceTerm, grantedAmount, partialReason);

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

  protected void setParentContext(ActionRequest request, InvoiceTerm invoiceTerm) {
    this.setInvoice(request, invoiceTerm);

    this.setMoveLine(request, invoiceTerm);

    this.setMove(request, invoiceTerm.getMoveLine());
  }

  protected void setInvoice(ActionRequest request, InvoiceTerm invoiceTerm) {
    Invoice invoice = ContextHelper.getContextParent(request.getContext(), Invoice.class, 1);
    if (invoice != null) {
      invoiceTerm.setInvoice(invoice);
    }
  }

  protected void setMove(ActionRequest request, MoveLine moveLine) {
    if (moveLine != null) {
      Move move = ContextHelper.getContextParent(request.getContext(), Move.class, 2);
      if (move != null) {
        moveLine.setMove(move);
      }
    }
  }

  protected void setMoveLine(ActionRequest request, InvoiceTerm invoiceTerm) {
    MoveLine moveLine = ContextHelper.getContextParent(request.getContext(), MoveLine.class, 1);
    if (moveLine != null) {
      invoiceTerm.setMoveLine(moveLine);
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

  @ErrorException
  public void setDueDate(ActionRequest request, ActionResponse response) throws AxelorException {
    InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
    InvoiceTermDateComputeService invoiceTermDateComputeService =
        Beans.get(InvoiceTermDateComputeService.class);

    invoiceTermDateComputeService.resetDueDate(invoiceTerm);
    response.setValues(invoiceTerm);
  }

  public void refreshInvoicePfpStatus(ActionRequest request, ActionResponse response)
      throws AxelorException {
    InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
    Beans.get(InvoiceTermPfpService.class).refreshInvoicePfpStatus(invoiceTerm.getInvoice());
  }

  @ErrorException
  public void setBankDetailsDomain(ActionRequest request, ActionResponse response) {
    InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);

    String domain =
        Beans.get(BankDetailsDomainServiceAccount.class)
            .createDomainForBankDetails(
                invoiceTerm.getPartner(), invoiceTerm.getPaymentMode(), invoiceTerm.getCompany());

    response.setAttr("bankDetails", "domain", domain);
  }

  @ErrorException
  public void getDefaultBankDetails(ActionRequest request, ActionResponse response) {
    InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);
    BankDetails bankDetails =
        Beans.get(BankDetailsServiceAccount.class)
            .getDefaultBankDetails(
                invoiceTerm.getPartner(), invoiceTerm.getCompany(), invoiceTerm.getPaymentMode());
    response.setValue("bankDetails", bankDetails);
  }
}
