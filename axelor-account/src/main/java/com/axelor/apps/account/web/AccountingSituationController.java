/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.DebtRecovery;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class AccountingSituationController {

  /**
   * return the domain of the field companyInBankDetails in the view.
   *
   * @see AccountingSituationService#createDomainForBankDetails(AccountingSituation, boolean)
   * @param request
   * @param response
   */
  public void createInBankDetailsDomain(ActionRequest request, ActionResponse response) {
    AccountingSituation accountingSituation =
        request.getContext().asType(AccountingSituation.class);
    String domain =
        Beans.get(AccountingSituationService.class)
            .createDomainForBankDetails(accountingSituation, true);
    if (!domain.equals("")) {
      response.setAttr("companyInBankDetails", "domain", domain);
    } else {
      response.setAttr("companyInBankDetails", "domain", "self.id in (0)");
    }
  }

  /**
   * return the domain of the field companyOutBankDetails in the view.
   *
   * @see AccountingSituationService#createDomainForBankDetails(AccountingSituation, boolean)
   * @param request
   * @param response
   */
  public void createOutBankDetailsDomain(ActionRequest request, ActionResponse response) {
    AccountingSituation accountingSituation =
        request.getContext().asType(AccountingSituation.class);
    String domain =
        Beans.get(AccountingSituationService.class)
            .createDomainForBankDetails(accountingSituation, false);
    if (!domain.equals("")) {
      response.setAttr("companyOutBankDetails", "domain", domain);
    } else {
      response.setAttr("companyOutBankDetails", "domain", "self.id in (0)");
    }
  }

  /**
   * set default value for automatic invoice printing
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void setDefaultMail(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AccountingSituation accountingSituation =
        request.getContext().asType(AccountingSituation.class);
    Company company = accountingSituation.getCompany();
    if (company != null) {
      AccountConfig accountConfig = Beans.get(AccountConfigService.class).getAccountConfig(company);
      response.setValue("invoiceAutomaticMail", accountConfig.getInvoiceAutomaticMail());
      response.setValue("invoiceMessageTemplate", accountConfig.getInvoiceMessageTemplate());
    }
  }

  /**
   * Open Debt Recovery record in form view
   *
   * @param request
   * @param response
   */
  public void openDebtRecovery(ActionRequest request, ActionResponse response) {
    AccountingSituation accountingSituation =
        request.getContext().asType(AccountingSituation.class);
    DebtRecovery debtRecovery = accountingSituation.getDebtRecovery();

    if (debtRecovery != null) {
      response.setView(
          ActionView.define(I18n.get("Debt Recovery"))
              .model(DebtRecovery.class.getName())
              .add("grid", "debt-recovery-grid")
              .add("form", "debt-recovery-form")
              .param("forceEdit", "true")
              .context("_showRecord", debtRecovery.getId())
              .map());
      response.setCanClose(true);
    }
  }
}
