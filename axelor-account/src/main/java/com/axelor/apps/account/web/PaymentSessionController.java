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

import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.PaymentSessionCancelService;
import com.axelor.apps.account.service.PaymentSessionEmailService;
import com.axelor.apps.account.service.PaymentSessionService;
import com.axelor.apps.account.service.PaymentSessionValidateService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
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

  public void computeTotal(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());
      Beans.get(PaymentSessionService.class).computeTotalPaymentSession(paymentSession);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateInvoiceTerms(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());
      response.setValue("hasReleasePopup", false);

      int errorCode =
          Beans.get(PaymentSessionValidateService.class).validateInvoiceTerms(paymentSession);
      if (errorCode == 1) {
        response.setAlert(I18n.get(IExceptionMessage.PAYMENT_SESSION_INVALID_INVOICE_TERMS));
      } else if (errorCode == 2) {
        ActionView.ActionViewBuilder actionViewBuilder =
            ActionView.define(I18n.get("Invoice terms"))
                .model(PaymentSession.class.getName())
                .add("form", "payment-session-validate-confirm-wizard")
                .param("popup", "true")
                .param("popup-save", "false")
                .context("_showRecord", paymentSession.getId());

        response.setValue("hasReleasePopup", true);
        response.setView(actionViewBuilder.map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validatePaymentSession(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());
      PaymentSessionValidateService paymentSessionValidateService =
          Beans.get(PaymentSessionValidateService.class);

      int moveCount = paymentSessionValidateService.processPaymentSession(paymentSession);

      response.setReload(true);

      StringBuilder flashMessage =
          paymentSessionValidateService.generateFlashMessage(paymentSession, moveCount);

      if (flashMessage.length() > 0) {
        response.setFlash(flashMessage.toString());
      }
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
        response.setFlash(I18n.get(IExceptionMessage.PAYMENT_SESSION_NO_EMAIL_SENT));
      } else {
        response.setFlash(
            String.format(I18n.get(IExceptionMessage.PAYMENT_SESSION_EMAIL_SENT), emailCount));
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

  public void reconciledInvoiceTermMoves(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());
      Beans.get(PaymentSessionValidateService.class).reconciledInvoiceTermMoves(paymentSession);

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkSession(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());
      List<Partner> partnerWithNegativeAmountList =
          Beans.get(PaymentSessionValidateService.class)
              .getPartnersWithNegativeAmount(paymentSession);

      if (!ObjectUtils.isEmpty(partnerWithNegativeAmountList)) {
        StringBuilder partnerFullNames = new StringBuilder("");
        partnerFullNames.append(
            partnerWithNegativeAmountList.stream()
                .map(partner -> partner.getFullName())
                .collect(Collectors.joining(",")));
        response.setError(
            String.format(
                I18n.get(IExceptionMessage.PAYMENT_SESSION_TOTAL_AMOUNT_NEGATIVE),
                partnerFullNames.toString(),
                paymentSession.getPaymentMode().getCode()));
      }

      boolean isHoldBackWithRefund =
          Beans.get(PaymentSessionValidateService.class).checkIsHoldBackWithRefund(paymentSession);
      if (isHoldBackWithRefund) {
        response.setError(
            String.format(I18n.get(IExceptionMessage.PAYMENT_SESSION_HOLD_BACK_MIXED_WITH_REFUND)));
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
}
