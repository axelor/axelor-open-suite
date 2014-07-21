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
package com.axelor.apps.base.module;

import com.axelor.app.AxelorModule;
import com.axelor.app.AxelorModuleInfo;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.AddressServiceImpl;
import com.axelor.apps.base.service.message.MailAccountServiceBaseImpl;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.AccountManagementServiceImpl;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.FiscalPositionServiceImpl;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.base.service.user.UserInfoServiceImpl;
import com.axelor.apps.message.service.MailAccountServiceImpl;
import com.axelor.apps.message.service.MessageServiceImpl;

@AxelorModuleInfo(name = "axelor-base")
public class BaseModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(AddressService.class).to(AddressServiceImpl.class);
        bind(UserInfoService.class).to(UserInfoServiceImpl.class);
//        bind(MessageServiceImpl.class).to(MessageServiceBaseImpl.class);
        bind(MailAccountServiceImpl.class).to(MailAccountServiceBaseImpl.class);
        bind(AccountManagementService.class).to(AccountManagementServiceImpl.class);
        bind(FiscalPositionService.class).to(FiscalPositionServiceImpl.class);
    }
}