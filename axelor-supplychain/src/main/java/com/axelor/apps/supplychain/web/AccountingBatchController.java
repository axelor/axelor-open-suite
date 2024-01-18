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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.service.config.AccountConfigSupplychainService;
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
