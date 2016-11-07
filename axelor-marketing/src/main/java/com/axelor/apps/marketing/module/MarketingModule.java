package com.axelor.apps.marketing.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.marketing.service.CampaignService;
import com.axelor.apps.marketing.service.CampaignServiceImpl;

public class MarketingModule extends AxelorModule{

	@Override
	protected void configure() {
		bind(CampaignService.class).to(CampaignServiceImpl.class);
	}
	
}
