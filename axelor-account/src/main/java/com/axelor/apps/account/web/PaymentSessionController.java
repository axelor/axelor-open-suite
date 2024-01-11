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

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionCancelService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionEmailService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionValidateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class PaymentSessionController {

  public void setBankDetails(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      Beans.get(PaymentSessionService.class).setBankDetails(paymentSession);
      response.setValue("bankDetails", paymentSession.getBankDetails());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setJournal(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      Beans.get(PaymentSessionService.class).setJournal(paymentSession);
      response.setValue("journal", paymentSession.getJournal());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelPaymentSession(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());

      Beans.get(PaymentSessionCancelService.class).cancelPaymentSession(paymentSession);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void sendEmails(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());

      int emailCount = Beans.get(PaymentSessionEmailService.class).sendEmails(paymentSession);

      response.setReload(true);

      if (emailCount == 0) {
        response.setInfo(I18n.get(AccountExceptionMessage.PAYMENT_SESSION_NO_EMAIL_SENT));
      } else {
        response.setInfo(
            String.format(
                I18n.get(AccountExceptionMessage.PAYMENT_SESSION_EMAIL_SENT), emailCount));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setHasInvoiceTermSelected(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      boolean hasUnselectedInvoiceTerm =
          Beans.get(PaymentSessionService.class).hasUnselectedInvoiceTerm(paymentSession);
      response.setValue("$hasUnselectedInvoiceTerm", hasUnselectedInvoiceTerm);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setHasInvoiceTerm(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      boolean hasInvoiceTerm =
          Beans.get(PaymentSessionService.class).hasInvoiceTerm(paymentSession);
      response.setValue("$hasInvoiceTerm", hasInvoiceTerm);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAndProcessInvoiceTerms(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());

      int errorCode =
          Beans.get(PaymentSessionValidateService.class).checkValidTerms(paymentSession);

      if (errorCode == 1) {
        response.setAlert(I18n.get(AccountExceptionMessage.PAYMENT_SESSION_INVALID_INVOICE_TERMS));
      } else if (errorCode == 2) {
        ActionView.ActionViewBuilder actionViewBuilder =
            ActionView.define(I18n.get("Invoice terms"))
                .model(PaymentSession.class.getName())
                .add("form", "payment-session-validate-confirm-wizard")
                .param("popup", "reload")
                .param("popup-save", "false")
                .param("show-toolbar", "false")
                .context("_showRecord", paymentSession.getId());

        response.setView(actionViewBuilder.map());
      } else {
        processInvoiceTerms(request, response);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void processInvoiceTerms(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());
      StringBuilder flashMessage =
          Beans.get(PaymentSessionValidateService.class).processInvoiceTerms(paymentSession);
      if (flashMessage.length() > 0) {
        response.setInfo(flashMessage.toString());
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkSession(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());
      PaymentSessionValidateService paymentSessionValidateService =
          Beans.get(PaymentSessionValidateService.class);

      List<Partner> partnerWithNegativeAmountList =
          paymentSessionValidateService.getPartnersWithNegativeAmount(paymentSession);

      if (!ObjectUtils.isEmpty(partnerWithNegativeAmountList)) {
        StringBuilder partnerFullNames = new StringBuilder("");
        partnerFullNames.append(
            partnerWithNegativeAmountList.stream()
                .map(partner -> partner.getFullName())
                .collect(Collectors.joining(",")));
        response.setError(
            String.format(
                I18n.get(AccountExceptionMessage.PAYMENT_SESSION_TOTAL_AMOUNT_NEGATIVE),
                partnerFullNames.toString(),
                paymentSession.getPaymentMode().getCode()));
      }

      boolean isHoldBackWithRefund =
          paymentSessionValidateService.checkIsHoldBackWithRefund(paymentSession);
      if (isHoldBackWithRefund) {
        response.setError(
            String.format(
                I18n.get(AccountExceptionMessage.PAYMENT_SESSION_HOLD_BACK_MIXED_WITH_REFUND)));
      }

      List<InvoiceTerm> invoiceTermsWithInActiveBankDetails =
          paymentSessionValidateService.getInvoiceTermsWithInActiveBankDetails(paymentSession);

      if (ObjectUtils.notEmpty(invoiceTermsWithInActiveBankDetails)) {
        String bankDetailNames =
            invoiceTermsWithInActiveBankDetails.stream()
                .map(InvoiceTerm::getBankDetails)
                .distinct()
                .map(BankDetails::getFullName)
                .collect(Collectors.joining("<br>"));

        response.setError(
            String.format(
                I18n.get(
                    AccountExceptionMessage
                        .PAYMENT_SESSION_INVOICE_TERM_WITH_IN_ACTIVE_BANK_DETAILS),
                bankDetailNames));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkIsAllInvoiceTerms(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());
      response.setValue(
          "$isAllInvoiceTerms",
          Beans.get(PaymentSessionValidateService.class).isEmpty(paymentSession));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setBankDetailsDomain(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);

      String bankDetailsIds =
          Beans.get(PaymentSessionService.class).getBankDetails(paymentSession).stream()
              .map(BankDetails::getId)
              .map(Objects::toString)
              .collect(Collectors.joining(","));

      response.setAttr(
          "bankDetails",
          "domain",
          String.format("self.id IN (%s)", bankDetailsIds.isEmpty() ? "0" : bankDetailsIds));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setJournalDomain(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);

      String journalIds =
          Beans.get(PaymentSessionService.class).getJournals(paymentSession).stream()
              .map(Journal::getId)
              .map(Objects::toString)
              .collect(Collectors.joining(","));

      response.setAttr(
          "journal",
          "domain",
          String.format("self.id IN (%s)", journalIds.isEmpty() ? "0" : journalIds));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  @SuppressWarnings("unchecked")
  public void removeMultiplePaymentSessions(ActionRequest request, ActionResponse response) {
    List<Long> paymentSessionIds = (List<Long>) request.getContext().get("_ids");
    try {
      int deletedSessions =
          Beans.get(PaymentSessionService.class).removeMultiplePaymentSessions(paymentSessionIds);
      if (paymentSessionIds.size() > deletedSessions) {
        response.setInfo(I18n.get(AccountExceptionMessage.PAYMENT_SESSION_MULTIPLE_DELETION));
      }
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
    response.setReload(true);
  }

  public void selectAll(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());
      Beans.get(PaymentSessionService.class).selectAll(paymentSession);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void searchEligibleTerms(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      PaymentSessionRepository paymentSessionRepository = Beans.get(PaymentSessionRepository.class);
      paymentSession = paymentSessionRepository.find(paymentSession.getId());
      Beans.get(PaymentSessionService.class).searchEligibleTerms(paymentSession);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void unSelectAll(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());
      Beans.get(PaymentSessionService.class).unSelectAll(paymentSession);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setButtonAttrs(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());

      List<InvoiceTerm> invoiceTermList =
          Beans.get(InvoiceTermRepository.class).findByPaymentSession(paymentSession).fetch();

      if (invoiceTermList.isEmpty()) {
        return;
      }

      boolean isSelectedReadonly = true;
      boolean isUnSelectedReadonly = true;

      if (invoiceTermList.stream().anyMatch(term -> !term.getIsSelectedOnPaymentSession())) {
        isSelectedReadonly = false;
      }

      if (invoiceTermList.stream().anyMatch(term -> term.getIsSelectedOnPaymentSession())) {
        isUnSelectedReadonly = false;
      }

      response.setAttr(
          "selectAllBtn",
          "hidden",
          paymentSession.getStatusSelect() > PaymentSessionRepository.STATUS_ONGOING);
      response.setAttr(
          "unselectAllBtn",
          "hidden",
          paymentSession.getStatusSelect() > PaymentSessionRepository.STATUS_ONGOING);
      response.setAttr("selectAllBtn", "readonly", isSelectedReadonly);
      response.setAttr("unselectAllBtn", "readonly", isUnSelectedReadonly);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showInvoiceTermDashlet(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());

      List<InvoiceTerm> invoiceTermList =
          Beans.get(InvoiceTermRepository.class).findByPaymentSession(paymentSession).fetch();
      int partnerCount =
          (int) invoiceTermList.stream().map(it -> it.getPartner()).distinct().count();
      int lineCount = partnerCount + invoiceTermList.size() - 1;
      if (lineCount > 10) {
        response.setAttr("invoiceTermPanelDashlet", "hidden", false);
      } else {
        response.setAttr("invoiceTermShorterPanelDashlet", "hidden", false);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
