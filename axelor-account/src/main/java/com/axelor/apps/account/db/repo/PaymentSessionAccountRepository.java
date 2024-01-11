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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;

public class PaymentSessionAccountRepository extends PaymentSessionRepository {

  @Inject PaymentSessionService paymentSessionService;
  @Inject SequenceService sequenceService;
  @Inject AppBaseService appBaseService;

  @Override
  public PaymentSession save(PaymentSession paymentSession) {
    try {
      paymentSession.setName(paymentSessionService.computeName(paymentSession));
      if (StringUtils.isEmpty(paymentSession.getSequence())) {
        paymentSession.setSequence(getSequence(paymentSession));
      }
      return super.save(paymentSession);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e);
    }
  }

  protected String getSequence(PaymentSession paymentSession) throws AxelorException {
    Company company = paymentSession.getCompany();
    String seq =
        sequenceService.getSequenceNumber(
            SequenceRepository.PAYMENT_SESSION, company, PaymentSession.class, "sequence");
    if (seq == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.PAYMENT_SESSION_NO_SEQ),
          company.getName());
    }
    return seq;
  }

  @Override
  public PaymentSession copy(PaymentSession entity, boolean deep) {

    PaymentSession copy = super.copy(entity, deep);
    copy.setPaymentDate(appBaseService.getTodayDate(copy.getCompany()));
    copy.setAssignedToUser(AuthUtils.getUser());
    copy.setSessionTotalAmount(BigDecimal.ZERO);
    copy.setHasEmailsSent(false);
    copy.setStatusSelect(PaymentSessionRepository.STATUS_ONGOING);
    copy.setValidatedByUser(null);
    copy.setValidatedDate(null);
    copy.setName(null);
    copy.setSequence(null);
    copy.setPartnerForEmail(null);
    return copy;
  }
}
