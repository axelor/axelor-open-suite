/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

  @Inject private CampaignRepository campaignRepo;

  @Inject private CampaignService campaignService;

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
    } else {
      response.setFlash(I18n.get("Error in sending emails. Please check the log file generated."));
    }

    response.setValue("emailLog", logFile);
  }
}
