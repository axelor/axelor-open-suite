package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Optional;
import java.util.Set;

public class AccountManagementController {
  public void checkTaxesNotOnlyNonDeductibleTaxes(ActionRequest request, ActionResponse response)
      throws AxelorException {
    String sourceFieldName = (String) request.getContext().get("_source");
    AccountManagement accountManagement = request.getContext().asType(AccountManagement.class);
    TaxAccountService taxAccountService = Beans.get(TaxAccountService.class);
    Account account;
    Set<Tax> taxes;
    if ("purchaseTaxSet".equals(sourceFieldName)) {
      account =
          Optional.of(accountManagement).map(AccountManagement::getPurchaseAccount).orElse(null);
      taxes = accountManagement.getPurchaseTaxSet();
    } else {
      // "saleTaxSet"
      account = Optional.of(accountManagement).map(AccountManagement::getSaleAccount).orElse(null);
      taxes = accountManagement.getSaleTaxSet();
    }

    boolean checkResult = taxAccountService.checkTaxesNotOnlyNonDeductibleTaxes(taxes);
    if (!checkResult) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              "Only one non-deductible tax is configured on the product/account %s. A non deductible tax should always be paired with at least one other deductible tax."),
          Optional.of(account).map(Account::getLabel).orElse(null));
    }
  }
}
