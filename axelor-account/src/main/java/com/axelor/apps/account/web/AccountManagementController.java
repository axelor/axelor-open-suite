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
import com.axelor.apps.account.db.repo.AccountManagementRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountManagementAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.ContextHelper;
import com.google.inject.Singleton;

@Singleton
public class AccountManagementController {

  @ErrorException
  public void setDomainAnalyticDistributionTemplate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    AccountManagement accountManagement = context.asType(AccountManagement.class);

    response.setAttr(
        "analyticDistributionTemplate",
        "domain",
        Beans.get(AnalyticAttrsService.class)
            .getAnalyticDistributionTemplateDomain(
                null,
                accountManagement.getProduct(),
                accountManagement.getCompany(),
                null,
                null,
                false));
  }

  protected ProductFamily getProductFamily(
      ActionRequest request, AccountManagement accountManagement) {
    if (accountManagement != null && accountManagement.getProductFamily() != null) {
      return accountManagement.getProductFamily();
    }

    return ContextHelper.getContextParent(request.getContext(), ProductFamily.class, 1);
  }

  @ErrorException
  public void setCompanyDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AccountManagement accountManagement = request.getContext().asType(AccountManagement.class);
    ProductFamily productFamily = getProductFamily(request, accountManagement);

    String domain =
        Beans.get(AccountManagementAttrsService.class)
            .getCompanyDomain(accountManagement, productFamily);

    response.setAttr("company", "domain", domain);
  }

  @ErrorException
  public void checkPaymentModeUniqueness(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AccountManagement accountManagement = request.getContext().asType(AccountManagement.class);

    if (accountManagement == null
        || accountManagement.getPaymentMode() == null
        || accountManagement.getInterbankCodeLine() != null
        || accountManagement.getBankDetails() == null
        || accountManagement.getCompany() == null) {
      return;
    }

    AccountManagementRepository accountManagementRepository =
        Beans.get(AccountManagementRepository.class);

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
                .bind("paymentMode", accountManagement.getPaymentMode())
                .bind("company", accountManagement.getCompany())
                .bind(
                    "currentId", accountManagement.getId() != null ? accountManagement.getId() : 0L)
                .count()
            > 0;

    if (duplicateExists) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_MANAGEMENT_ALREADY_EXISTS),
          accountManagement.getBankDetails().getFullName(),
          accountManagement.getPaymentMode().getName(),
          accountManagement.getCompany().getName());
    }
  }
}
