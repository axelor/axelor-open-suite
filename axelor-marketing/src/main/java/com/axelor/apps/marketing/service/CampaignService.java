package com.axelor.apps.marketing.service;

import com.axelor.apps.marketing.db.Campaign;
import com.axelor.meta.db.MetaFile;

public interface CampaignService {
	
	public MetaFile sendEmail(Campaign campaign);
	
}
