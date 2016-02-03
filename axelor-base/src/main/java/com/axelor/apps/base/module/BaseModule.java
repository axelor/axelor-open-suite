/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.repo.AddressBaseRepository;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.DurationBaseRepository;
import com.axelor.apps.base.db.repo.DurationRepository;
import com.axelor.apps.base.db.repo.PartnerBaseRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductBaseRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.SequenceBaseRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.YearBaseRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.AddressServiceImpl;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.DurationServiceImpl;
import com.axelor.apps.base.service.MailServiceBaseImpl;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.ProductServiceImpl;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.base.service.message.MailAccountServiceBaseImpl;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.base.service.message.TemplateMessageServiceBaseImpl;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.AccountManagementServiceImpl;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.FiscalPositionServiceImpl;
import com.axelor.apps.base.service.template.TemplateBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.base.service.user.UserServiceImpl;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningServiceImp;
import com.axelor.apps.message.service.MailAccountServiceImpl;
import com.axelor.apps.message.service.MailServiceMessageImpl;
import com.axelor.apps.message.service.MessageServiceImpl;
import com.axelor.apps.message.service.TemplateMessageServiceImpl;
import com.axelor.apps.message.service.TemplateService;


public class BaseModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(AddressService.class).to(AddressServiceImpl.class);
        bind(UserService.class).to(UserServiceImpl.class);
        bind(MessageServiceImpl.class).to(MessageServiceBaseImpl.class);
        bind(MailAccountServiceImpl.class).to(MailAccountServiceBaseImpl.class);
        bind(AccountManagementService.class).to(AccountManagementServiceImpl.class);
        bind(FiscalPositionService.class).to(FiscalPositionServiceImpl.class);
        bind(ProductService.class).to(ProductServiceImpl.class);
        bind(TemplateService.class).to(TemplateBaseService.class);
        bind(TemplateMessageServiceImpl.class).to(TemplateMessageServiceBaseImpl.class);
        bind(PartnerRepository.class).to(PartnerBaseRepository.class);
        bind(DurationRepository.class).to(DurationBaseRepository.class);
        bind(DurationService.class).to(DurationServiceImpl.class);
        bind(GeneralService.class).to(GeneralServiceImpl.class);
        bind(SequenceRepository.class).to(SequenceBaseRepository.class);
        bind(ProductRepository.class).to(ProductBaseRepository.class);
        bind(WeeklyPlanningService.class).to(WeeklyPlanningServiceImp.class);
        bind(MailServiceMessageImpl.class).to(MailServiceBaseImpl.class);
        bind(AddressRepository.class).to(AddressBaseRepository.class);
        bind(YearRepository.class).to(YearBaseRepository.class);
    }
}