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
package com.axelor.apps.marketing.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.marketing.db.Campaign;
import com.axelor.apps.marketing.db.repo.CampaignRepository;
import com.axelor.apps.marketing.exception.MarketingExceptionMessage;
import com.axelor.apps.marketing.service.CampaignService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class CampaignController {

  public void sendEmail(ActionRequest request, ActionResponse response) {

    Campaign campaign = request.getContext().asType(Campaign.class);

    try {
      campaign = Beans.get(CampaignRepository.class).find(campaign.getId());

      if (campaign.getLeadSet().isEmpty() && campaign.getPartnerSet().isEmpty()) {
        response.setInfo(I18n.get(MarketingExceptionMessage.EMPTY_TARGET));
        return;
      }
      MetaFile logFile = Beans.get(CampaignService.class).sendEmail(campaign);

      if (logFile == null) {
        response.setInfo(I18n.get(MarketingExceptionMessage.EMAIL_SUCCESS));
      } else {
        response.setInfo(I18n.get(MarketingExceptionMessage.EMAIL_ERROR2));
      }

      response.setValue("emailLog", logFile);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void sendReminderEmail(ActionRequest request, ActionResponse response) {

    Campaign campaign = request.getContext().asType(Campaign.class);

    try {
      campaign = Beans.get(CampaignRepository.class).find(campaign.getId());

      if (CollectionUtils.isEmpty(campaign.getInvitedPartnerSet())
          && CollectionUtils.isEmpty(campaign.getInvitedLeadSet())) {
        response.setInfo(I18n.get(MarketingExceptionMessage.REMINDER_EMAIL1));
        return;
      }

      MetaFile logFile = Beans.get(CampaignService.class).sendReminderEmail(campaign);

      if (logFile == null) {
        response.setInfo(I18n.get(MarketingExceptionMessage.EMAIL_SUCCESS));
      } else {
        response.setInfo(I18n.get(MarketingExceptionMessage.EMAIL_ERROR2));
      }

      response.setValue("emailLog", logFile);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateEvents(ActionRequest request, ActionResponse response) {

    Campaign campaign = request.getContext().asType(Campaign.class);

    try {
      campaign = Beans.get(CampaignRepository.class).find(campaign.getId());
      Beans.get(CampaignService.class).generateEvents(campaign);
      response.setAttr("plannedEventsPanel", "refresh", true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateTargets(ActionRequest request, ActionResponse response) {

    Campaign campaign = request.getContext().asType(Campaign.class);

    try {
      campaign = Beans.get(CampaignRepository.class).find(campaign.getId());
      Beans.get(CampaignService.class).generateTargets(campaign);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }

    response.setReload(true);
  }

  public void inviteSelectedTargets(ActionRequest request, ActionResponse response) {

    Campaign campaignContext = request.getContext().asType(Campaign.class);

    try {
      Beans.get(CampaignService.class)
          .inviteSelectedTargets(
              Beans.get(CampaignRepository.class).find(campaignContext.getId()), campaignContext);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void inviteAllTargets(ActionRequest request, ActionResponse response) {

    Campaign campaign = request.getContext().asType(Campaign.class);

    try {
      Beans.get(CampaignService.class)
          .inviteAllTargets(Beans.get(CampaignRepository.class).find(campaign.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void addParticipatingTargets(ActionRequest request, ActionResponse response) {

    Campaign campaignContext = request.getContext().asType(Campaign.class);

    try {
      Beans.get(CampaignService.class)
          .addParticipatingTargets(
              Beans.get(CampaignRepository.class).find(campaignContext.getId()), campaignContext);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void addNotParticipatingTargets(ActionRequest request, ActionResponse response) {

    Campaign campaignContext = request.getContext().asType(Campaign.class);

    try {
      Beans.get(CampaignService.class)
          .addNotParticipatingTargets(
              Beans.get(CampaignRepository.class).find(campaignContext.getId()), campaignContext);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void markPartnerPresent(ActionRequest request, ActionResponse response) {

    Campaign campaign = request.getContext().getParent().asType(Campaign.class);
    Partner partner = request.getContext().asType(Partner.class);
    partner = Beans.get(PartnerRepository.class).find(partner.getId());

    try {
      Beans.get(CampaignService.class)
          .markPartnerPresent(Beans.get(CampaignRepository.class).find(campaign.getId()), partner);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void markLeadPresent(ActionRequest request, ActionResponse response) {

    Campaign campaign = request.getContext().getParent().asType(Campaign.class);
    Lead lead = request.getContext().asType(Lead.class);
    lead = Beans.get(LeadRepository.class).find(lead.getId());

    try {
      Beans.get(CampaignService.class)
          .markLeadPresent(Beans.get(CampaignRepository.class).find(campaign.getId()), lead);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
