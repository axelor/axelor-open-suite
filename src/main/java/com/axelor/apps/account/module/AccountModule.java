/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.module;

import com.axelor.app.AxelorModule;
import com.axelor.app.AxelorModuleInfo;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.AddressServiceAccountImpl;
import com.axelor.apps.account.service.FiscalPositionServiceAccountImpl;
import com.axelor.apps.account.service.ProductServiceAccountImpl;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.account.service.user.UserInfoServiceAccountImpl;
import com.axelor.apps.base.service.AddressServiceImpl;
import com.axelor.apps.base.service.ProductServiceImpl;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.tax.AccountManagementServiceImpl;
import com.axelor.apps.base.service.tax.FiscalPositionServiceImpl;
import com.axelor.apps.base.service.user.UserInfoServiceImpl;

@AxelorModuleInfo(name = "axelor-account")
public class AccountModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(AddressServiceImpl.class).to(AddressServiceAccountImpl.class);
        
        bind(ProductServiceImpl.class).to(ProductServiceAccountImpl.class);
        
        bind(UserInfoServiceImpl.class).to(UserInfoServiceAccountImpl.class);
        
        bind(GeneralService.class).to(GeneralServiceAccount.class);
        
        bind(AccountManagementServiceImpl.class).to(AccountManagementServiceAccountImpl.class);
        
        bind(FiscalPositionServiceImpl.class).to(FiscalPositionServiceAccountImpl.class);
        
    }
}