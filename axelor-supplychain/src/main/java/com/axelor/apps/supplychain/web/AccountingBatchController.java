package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.supplychain.service.config.AccountConfigSupplychainService;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AccountingBatchController {

  public void setDefaultCutOffAccount(ActionRequest request, ActionResponse response) {
    AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
    try {
      if (accountingBatch.getCompany() != null
          && (accountingBatch.getAccountingCutOffTypeSelect()
                  == AccountingBatchRepository.ACCOUNTING_CUT_OFF_TYPE_SUPPLIER_INVOICES
              || accountingBatch.getAccountingCutOffTypeSelect()
                  == AccountingBatchRepository.ACCOUNTING_CUT_OFF_TYPE_CUSTOMER_INVOICES)) {
        AccountConfigSupplychainService accountConfigSupplychainService =
            Beans.get(AccountConfigSupplychainService.class);
        AccountConfig accountConfig =
            Beans.get(AccountConfigService.class).getAccountConfig(accountingBatch.getCompany());
        response.setValue(
            "forecastedInvCustAccount",
            accountConfigSupplychainService.getForecastedInvCustAccount(accountConfig));
        response.setValue(
            "forecastedInvSuppAccount",
            accountConfigSupplychainService.getForecastedInvSuppAccount(accountConfig));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
