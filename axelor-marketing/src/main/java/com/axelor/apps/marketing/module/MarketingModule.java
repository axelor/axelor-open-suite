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
package com.axelor.apps.marketing.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.base.service.message.TemplateMessageServiceBaseImpl;
import com.axelor.apps.marketing.service.CampaignService;
import com.axelor.apps.marketing.service.CampaignServiceImpl;
import com.axelor.apps.marketing.service.TargetListService;
import com.axelor.apps.marketing.service.TargetListServiceImpl;
import com.axelor.apps.marketing.service.TemplateMessageMarketingService;
import com.axelor.apps.marketing.service.TemplateMessageServiceMarketingImpl;

public class MarketingModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(CampaignService.class).to(CampaignServiceImpl.class);
    bind(TargetListService.class).to(TargetListServiceImpl.class);
    bind(TemplateMessageServiceBaseImpl.class).to(TemplateMessageServiceMarketingImpl.class);
    bind(TemplateMessageMarketingService.class).to(TemplateMessageServiceMarketingImpl.class);
  }
}
