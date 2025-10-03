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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.MoveTemplateLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticDistributionTemplateService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.translation.ITranslation;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.MassUpdateHelper;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            I18n.get(AccountExceptionMessage.ACCOUNT_CODE_ALREADY_IN_USE_FOR_COMPANY),
            account.getCode(),
            account.getCompany().getName());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageAnalytic(ActionRequest request, ActionResponse response) {
    try {
      Account account = request.getContext().asType(Account.class);
      response.setAttr("analyticSettingsPanel", "hidden", false);

      if (account.getCompany() == null
          || !Beans.get(AppAccountService.class).getAppAccount().getManageAnalyticAccounting()
          || !Beans.get(AccountConfigService.class)
              .getAccountConfig(account.getCompany())
              .getManageAnalyticAccounting()
          || account.getAccountType() == null
          || AccountTypeRepository.TYPE_VIEW.equals(
              account.getAccountType().getTechnicalTypeSelect())) {
        response.setAttr("analyticSettingsPanel", "hidden", true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnalyticDistTemplate(ActionRequest request, ActionResponse response) {
    try {
      Account account = request.getContext().asType(Account.class);
      AnalyticDistributionTemplate specificAnalyticDistributionTemplate =
          Beans.get(AnalyticDistributionTemplateService.class)
              .createSpecificDistributionTemplate(account.getCompany(), account.getName());

      if (specificAnalyticDistributionTemplate != null) {
        response.setValue("analyticDistributionTemplate", specificAnalyticDistributionTemplate);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void personalizeAnalyticDistTemplate(ActionRequest request, ActionResponse response) {
    try {
      Account account = request.getContext().asType(Account.class);
      if (account.getAnalyticDistributionTemplate() != null) {
        AnalyticDistributionTemplate specificAnalyticDistributionTemplate =
            Beans.get(AnalyticDistributionTemplateService.class)
                .personalizeAnalyticDistributionTemplate(
                    account.getAnalyticDistributionTemplate(),
                    account.getAnalyticDistributionTemplate().getCompany());
        if (specificAnalyticDistributionTemplate != null) {
          response.setValue("analyticDistributionTemplate", specificAnalyticDistributionTemplate);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void toggleStatus(ActionRequest request, ActionResponse response) {
    try {
      Account account = request.getContext().asType(Account.class);
      account = Beans.get(AccountRepository.class).find(account.getId());

      Beans.get(AccountService.class).toggleStatusSelect(account);

      response.setReload(true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void massUpdateSelected(ActionRequest request, ActionResponse response) {
    try {

      final String fieldName = "statusSelect";
      Object statusObj = request.getContext().get(fieldName);

      if (ObjectUtils.isEmpty(statusObj)) {
        response.setError(I18n.get(AccountExceptionMessage.MASS_UPDATE_NO_STATUS));
        return;
      }

      Object selectedIdObj = request.getContext().get("_selectedIds");
      if (ObjectUtils.isEmpty(selectedIdObj)) {
        response.setError(I18n.get(AccountExceptionMessage.MASS_UPDATE_NO_RECORD_SELECTED));
        return;
      }

      String metaModel = (String) request.getContext().get("_metaModel");
      Integer statusSelect = (Integer) statusObj;
      List<Integer> selectedIds = (List<Integer>) selectedIdObj;
      final Class<? extends Model> modelClass = (Class<? extends Model>) Class.forName(metaModel);
      Integer recordsUpdated =
          MassUpdateHelper.update(modelClass, fieldName, statusSelect, selectedIds);
      String message = null;
      if (recordsUpdated > 0) {
        message =
            String.format(I18n.get(AccountExceptionMessage.MASS_UPDATE_SUCCESSFUL), recordsUpdated);
      } else {
        message = I18n.get(AccountExceptionMessage.MASS_UPDATE_SELECTED_NO_RECORD);
      }
      response.setInfo(message);
      response.setCanClose(true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void massUpdateAll(ActionRequest request, ActionResponse response) {
    try {
      final String fieldName = "statusSelect";
      Object statusObj = request.getContext().get(fieldName);

      if (ObjectUtils.isEmpty(statusObj)) {
        response.setError(I18n.get(AccountExceptionMessage.MASS_UPDATE_NO_STATUS));
        return;
      }

      String metaModel = (String) request.getContext().get("_metaModel");
      Integer statusSelect = (Integer) statusObj;
      final Class<? extends Model> modelClass = (Class<? extends Model>) Class.forName(metaModel);
      Integer recordsUpdated = MassUpdateHelper.update(modelClass, fieldName, statusSelect, null);
      String message = null;
      if (recordsUpdated > 0) {
        message =
            String.format(I18n.get(AccountExceptionMessage.MASS_UPDATE_SUCCESSFUL), recordsUpdated);
      } else {
        message = I18n.get(AccountExceptionMessage.MASS_UPDATE_ALL_NO_RECORD);
      }

      response.setInfo(message);
      response.setCanClose(true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillAccountCode(ActionRequest request, ActionResponse response) {
    Account account = request.getContext().asType(Account.class);
    try {
      account = Beans.get(AccountService.class).fillAccountCode(account);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setValue("code", account.getCode());
    }
  }

  public void verifyTemplateValues(ActionRequest request, ActionResponse response) {
    Account account = request.getContext().asType(Account.class);
    try {
      AnalyticDistributionTemplateService analyticDistributionTemplateService =
          Beans.get(AnalyticDistributionTemplateService.class);
      analyticDistributionTemplateService.verifyTemplateValues(
          account.getAnalyticDistributionTemplate());
      analyticDistributionTemplateService.validateTemplatePercentages(
          account.getAnalyticDistributionTemplate());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setAmountRemainingReconciliableMoveLines(
      ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      Beans.get(MoveLineToolService.class).setAmountRemainingReconciliableMoveLines(context);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setParentAccountDomain(ActionRequest request, ActionResponse response) {
    try {
      Account account = request.getContext().asType(Account.class);
      String domain =
          "self.company.id = "
              + Optional.ofNullable(account.getCompany()).map(Company::getId).orElse(null);
      if (account.getId() != null) {
        List<Long> allAccountsSubAccountIncluded =
            Beans.get(AccountService.class)
                .getAllAccountsSubAccountIncluded(List.of(account.getId()));
        domain +=
            " AND self.id NOT IN ("
                + allAccountsSubAccountIncluded.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","))
                + ")";
      }
      response.setAttr("parentAccount", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void viewMoveTemplates(ActionRequest request, ActionResponse response) {
    Account account = request.getContext().asType(Account.class);

    List<Long> moveTemplateIdList =
        Beans.get(MoveTemplateLineRepository.class).findByAccount(account).fetch().stream()
            .map(MoveTemplateLine::getMoveTemplate)
            .map(MoveTemplate::getId)
            .distinct()
            .collect(Collectors.toList());

    ActionView.ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Move templates"));
    actionViewBuilder.model(MoveTemplate.class.getName());
    actionViewBuilder.add("grid", "move-template-grid");
    actionViewBuilder.add("form", "move-template-form");
    actionViewBuilder.domain("self.id IN (:moveTemplateIds)");
    actionViewBuilder.context("moveTemplateIds", moveTemplateIdList);

    response.setView(actionViewBuilder.map());
  }

  public void checkDefaultTaxSet(ActionRequest request, ActionResponse response) {
    try {
      Account account = request.getContext().asType(Account.class);
      if (account != null && account.getDefaultTaxSet() != null) {
        TaxAccountService taxAccountService = Beans.get(TaxAccountService.class);
        taxAccountService.checkTaxesNotOnlyNonDeductibleTaxes(account.getDefaultTaxSet());
        taxAccountService.checkSumOfNonDeductibleTaxes(account.getDefaultTaxSet());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  @ErrorException
  public void originAnalyticDistTemplate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Account account = request.getContext().asType(Account.class);
    if (Boolean.TRUE.equals(account.getAnalyticDistributionAuthorized())) {
      Company accountCompany = account.getCompany();
      if (accountCompany != null && accountCompany.getAccountConfig() != null) {
        Integer analyticDistSelect =
            Beans.get(AccountConfigService.class)
                .getAccountConfig(accountCompany)
                .getAnalyticDistributionTypeSelect();
        if (analyticDistSelect == AccountConfigRepository.DISTRIBUTION_TYPE_PARTNER) {
          response.setAttr("analyticDistributionTemplateLabel", "hidden", false);
        } else if (analyticDistSelect == AccountConfigRepository.DISTRIBUTION_TYPE_PRODUCT) {
          response.setAttr("analyticDistributionTemplate", "required", true);
        }
      }
    }
  }

  @ErrorException
  public void setDomainAnalyticDistributionTemplate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    Account account = context.asType(Account.class);

    response.setAttr(
        "analyticDistributionTemplate",
        "domain",
        Beans.get(AnalyticAttrsService.class)
            .getAnalyticDistributionTemplateDomain(
                null, null, account.getCompany(), null, account, false));
  }
}
