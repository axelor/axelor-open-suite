/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.AccountManagementRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.PaymentModeControlService;
import com.axelor.apps.account.service.payment.PaymentModeInterestRateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;

public class PaymentModeController {

  public void setReadOnly(ActionRequest request, ActionResponse response) {

    try {
      PaymentMode paymentMode =
          Beans.get(PaymentModeRepository.class)
              .find(request.getContext().asType(PaymentMode.class).getId());
      if (paymentMode != null) {
        Boolean isInMove = Beans.get(PaymentModeControlService.class).isInMove(paymentMode);
        response.setAttr("name", "readonly", isInMove);
        response.setAttr("code", "readonly", isInMove);
        response.setAttr("typeSelect", "readonly", isInMove);
        response.setAttr("inOutSelect", "readonly", isInMove);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void saveInterestRateToHistory(ActionRequest request, ActionResponse response)
      throws AxelorException {
    PaymentMode paymentMode =
        Beans.get(PaymentModeRepository.class)
            .find(request.getContext().asType(PaymentMode.class).getId());

    Beans.get(PaymentModeInterestRateService.class).saveInterestRateToHistory(paymentMode);
    response.setReload(true);
  }

  @ErrorException
  public void checkAccountManagementUniqueness(ActionRequest request, ActionResponse response)
      throws AxelorException {
    PaymentMode paymentMode = request.getContext().asType(PaymentMode.class);
    List<AccountManagement> accountManagementList = paymentMode.getAccountManagementList();

    if (accountManagementList == null || accountManagementList.isEmpty()) {
      return;
    }

    AccountManagementRepository accountManagementRepository =
        Beans.get(AccountManagementRepository.class);

    for (AccountManagement accountManagement : accountManagementList) {
      if (accountManagement.getInterbankCodeLine() != null
          || accountManagement.getBankDetails() == null
          || accountManagement.getCompany() == null) {
        continue;
      }

      boolean duplicateExists =
          accountManagementRepository
                  .all()
                  .filter(
                      "self.bankDetails = :bankDetails "
                          + "AND self.paymentMode = :paymentMode "
                          + "AND self.company = :company "
                          + "AND self.interbankCodeLine IS NULL "
                          + "AND self.id != :currentId")
                  .bind("bankDetails", accountManagement.getBankDetails())
                  .bind("paymentMode", paymentMode)
                  .bind("company", accountManagement.getCompany())
                  .bind(
                      "currentId",
                      accountManagement.getId() != null ? accountManagement.getId() : 0L)
                  .count()
              > 0;

      if (duplicateExists) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.ACCOUNT_MANAGEMENT_ALREADY_EXISTS),
            accountManagement.getBankDetails().getFullName(),
            paymentMode.getName(),
            accountManagement.getCompany().getName());
      }
    }
  }
}
