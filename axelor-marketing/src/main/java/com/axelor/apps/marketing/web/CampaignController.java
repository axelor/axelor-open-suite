package com.axelor.apps.marketing.web;

import com.axelor.apps.marketing.db.Campaign;
import com.axelor.apps.marketing.db.repo.CampaignRepository;
import com.axelor.apps.marketing.service.CampaignService;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class CampaignController {
	
	@Inject
	private CampaignRepository campaignRepo;
	
	@Inject
	private CampaignService campaignService;
	
	public void sendEmail(ActionRequest request, ActionResponse response) {
		
		Campaign campaign = request.getContext().asType(Campaign.class);
		campaign = campaignRepo.find(campaign.getId());
		
		if (campaign.getTargetListSet().isEmpty()) {
			response.setFlash(I18n.get("Please select target"));
			return;
		}
		
		MetaFile logFile = campaignService.sendEmail(campaign);
		
		if (logFile == null) {
			response.setFlash(I18n.get("Emails sent successfully"));
		}
		else {
			response.setFlash(I18n.get("Error in sending emails. Please check the log file generated."));
		}
		
		response.setValue("emailLog", logFile);
	}
}
