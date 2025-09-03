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

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.DebtRecovery;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationGroupService;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.ContextHelper;
import com.google.inject.Singleton;

@Singleton
public class AccountingSituationController {

  /** return the domain of the field companyInBankDetails in the view. */
  public void createInBankDetailsDomain(ActionRequest request, ActionResponse response) {
    AccountingSituation accountingSituation =
        request.getContext().asType(AccountingSituation.class);
    Partner partner = getPartner(request, accountingSituation);

    response.setAttrs(
        Beans.get(AccountingSituationGroupService.class)
            .getBankDetailsOnSelectAttrsMap(accountingSituation, partner, true));
  }

  /** return the domain of the field companyOutBankDetails in the view. */
  public void createOutBankDetailsDomain(ActionRequest request, ActionResponse response) {
    AccountingSituation accountingSituation =
        request.getContext().asType(AccountingSituation.class);
    Partner partner = getPartner(request, accountingSituation);

    response.setAttrs(
        Beans.get(AccountingSituationGroupService.class)
            .getBankDetailsOnSelectAttrsMap(accountingSituation, partner, false));
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

  /** return the domain of the field company in the view. */
  @ErrorException
  public void setCompanyDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AccountingSituation accountingSituation =
        request.getContext().asType(AccountingSituation.class);
    Partner partner = getPartner(request, accountingSituation);

    response.setAttrs(
        Beans.get(AccountingSituationGroupService.class)
            .getCompanyOnSelectAttrsMap(accountingSituation, partner));
  }

  @ErrorException
  public void onNew(ActionRequest request, ActionResponse response) throws AxelorException {
    AccountingSituation accountingSituation =
        request.getContext().asType(AccountingSituation.class);
    Partner partner = getPartner(request, accountingSituation);
    AccountingSituationGroupService accountingSituationGroupService =
        Beans.get(AccountingSituationGroupService.class);

    response.setValues(
        accountingSituationGroupService.getOnNewValuesMap(accountingSituation, partner));
    response.setAttrs(
        accountingSituationGroupService.getOnNewAttrsMap(accountingSituation, partner));
  }

  @ErrorException
  public void onLoad(ActionRequest request, ActionResponse response) throws AxelorException {
    AccountingSituation accountingSituation =
        request.getContext().asType(AccountingSituation.class);
    Partner partner = getPartner(request, accountingSituation);
    AccountingSituationGroupService accountingSituationGroupService =
        Beans.get(AccountingSituationGroupService.class);

    response.setAttrs(
        accountingSituationGroupService.getOnNewAttrsMap(accountingSituation, partner));
  }

  @ErrorException
  public void companyOnChange(ActionRequest request, ActionResponse response)
      throws AxelorException {
    AccountingSituation accountingSituation =
        request.getContext().asType(AccountingSituation.class);
    Partner partner = getPartner(request, accountingSituation);
    AccountingSituationGroupService accountingSituationGroupService =
        Beans.get(AccountingSituationGroupService.class);

    response.setValues(
        accountingSituationGroupService.getCompanyOnChangeValuesMap(accountingSituation, partner));
    response.setAttrs(
        accountingSituationGroupService.getCompanyOnChangeAttrsMap(accountingSituation, partner));
  }

  protected Partner getPartner(ActionRequest request, AccountingSituation accountingSituation) {
    if (accountingSituation != null && accountingSituation.getPartner() != null) {
      return accountingSituation.getPartner();
    }

    return ContextHelper.getContextParent(request.getContext(), Partner.class, 1);
  }

  @ErrorException
  public void setDomainAnalyticDistributionTemplate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    AccountingSituation accountingSituation = context.asType(AccountingSituation.class);

    Partner partner = getPartner(request, accountingSituation);

    response.setAttr(
        "analyticDistributionTemplate",
        "domain",
        Beans.get(AnalyticAttrsService.class)
            .getAnalyticDistributionTemplateDomain(
                partner, null, accountingSituation.getCompany(), null, null, false));
  }
}
