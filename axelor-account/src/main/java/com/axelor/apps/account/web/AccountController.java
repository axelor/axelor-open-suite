/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.account.service.analytic.AnalyticDistributionTemplateService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.translation.ITranslation;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;

@Singleton
public class AccountController {

  public void computeBalance(ActionRequest request, ActionResponse response) {
    try {
      Account account = request.getContext().asType(Account.class);
      if (account.getId() == null) {
        return;
      }
      account = Beans.get(AccountRepository.class).find(account.getId());

      BigDecimal balance =
          Beans.get(AccountService.class)
              .computeBalance(account, AccountService.BALANCE_TYPE_DEBIT_BALANCE);

      if (balance.compareTo(BigDecimal.ZERO) >= 0) {
        response.setAttr("$balanceBtn", "title", I18n.get(ITranslation.ACCOUNT_DEBIT_BALANCE));
      } else {
        balance = balance.multiply(new BigDecimal(-1));
        response.setAttr("$balanceBtn", "title", I18n.get(ITranslation.ACCOUNT_CREDIT_BALANCE));
      }

      response.setValue("$balanceBtn", balance);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkIfCodeAccountAlreadyExistForCompany(
      ActionRequest request, ActionResponse response) {
    try {
      Account account = request.getContext().asType(Account.class);
      Long accountId = account.getId();
      if (accountId == null) {
        accountId = 0L;
      }
      List<Account> sameAccountList =
          Beans.get(AccountRepository.class)
              .all()
              .filter(
                  "self.company = ?1 AND self.code = ?2 AND self.id != ?3",
                  account.getCompany(),
                  account.getCode(),
                  accountId)
              .fetch();
      if (!ObjectUtils.isEmpty(sameAccountList)) {

        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.ACCOUNT_CODE_ALREADY_IN_USE_FOR_COMPANY),
            account.getCode(),
            account.getCompany().getName());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnalyticAccount(ActionRequest request, ActionResponse response) {
    try {
      Account account = request.getContext().asType(Account.class);
      Beans.get(AccountService.class).checkAnalyticAxis(account);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageAnalytic(ActionRequest request, ActionResponse response) {
    try {
      Account account = request.getContext().asType(Account.class);
      if (!Beans.get(AppAccountService.class).getAppAccount().getManageAnalyticAccounting()
          || !Beans.get(AccountConfigService.class)
              .getAccountConfig(account.getCompany())
              .getManageAnalyticAccounting()) {
        response.setAttr("analyticSettingsPanel", "hidden", true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnalyticDistTemplate(ActionRequest request, ActionResponse response) {
    try {
      Account account = request.getContext().asType(Account.class);
      if (account.getAnalyticDistributionTemplate() == null
          && account.getAnalyticDistributionAuthorized()) {
        AnalyticDistributionTemplate analyticDistributionTemplate =
            Beans.get(AnalyticDistributionTemplateService.class)
                .createDistributionTemplateFromAccount(account);
        if (analyticDistributionTemplate != null) {
          response.setValue("analyticDistributionTemplate", analyticDistributionTemplate);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
