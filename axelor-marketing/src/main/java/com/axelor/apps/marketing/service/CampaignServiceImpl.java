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
package com.axelor.apps.marketing.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.marketing.db.Campaign;
import com.axelor.apps.marketing.db.repo.CampaignRepository;
import com.axelor.apps.marketing.exception.MarketingExceptionMessage;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import wslite.json.JSONException;

public class CampaignServiceImpl implements CampaignService {

  protected TemplateMessageMarketingService templateMessageMarketingService;

  protected EventRepository eventRepo;

  @Inject
  public CampaignServiceImpl(
      TemplateMessageService templateMessageService,
      EventRepository eventRepo,
      TemplateMessageMarketingService templateMessageMarketingService) {
    this.templateMessageMarketingService = templateMessageMarketingService;
    this.eventRepo = eventRepo;
  }

  public MetaFile sendEmail(Campaign campaign) {

    String errorPartners = "";
    String errorLeads = "";

    templateMessageMarketingService.setEmailAccount(campaign.getEmailAccount());

    if (campaign.getPartnerTemplate() != null) {
      errorPartners =
          sendToPartners(campaign.getPartnerSet(), campaign.getPartnerTemplate(), campaign);
    }

    if (campaign.getLeadTemplate() != null) {
      errorLeads = sendToLeads(campaign.getLeadSet(), campaign.getLeadTemplate(), campaign);
    }

    if (errorPartners.isEmpty() && errorLeads.isEmpty()) {
      return null;
    }

    return generateLog(errorPartners, errorLeads, campaign.getEmailLog(), campaign.getId());
  }

  @Override
  public MetaFile sendReminderEmail(Campaign campaign) {

    String errorPartners = "";
    String errorLeads = "";

    templateMessageMarketingService.setEmailAccount(campaign.getEmailAccount());

    if (campaign.getPartnerReminderTemplate() != null) {
      errorPartners =
          sendToPartners(
              campaign.getInvitedPartnerSet(), campaign.getPartnerReminderTemplate(), campaign);
    }

    if (campaign.getLeadReminderTemplate() != null) {
      errorLeads =
          sendToLeads(campaign.getInvitedLeadSet(), campaign.getLeadReminderTemplate(), campaign);
    }
    if (errorPartners.isEmpty() && errorLeads.isEmpty()) {
      return null;
    }
    return generateLog(errorPartners, errorLeads, campaign.getEmailLog(), campaign.getId());
  }

  protected String sendToPartners(Set<Partner> partnerSet, Template template, Campaign campaign) {

    StringBuilder errors = new StringBuilder();

    for (Partner partner : partnerSet) {

      try {
        generateAndSendMessage(campaign, partner, template);
      } catch (ClassNotFoundException | IOException | JSONException e) {
        errors.append(partner.getName() + "\n");
        e.printStackTrace();
      }
    }

    return errors.toString();
  }

  protected String sendToLeads(Set<Lead> leadSet, Template template, Campaign campaign) {

    StringBuilder errors = new StringBuilder();

    for (Lead lead : leadSet) {

      try {
        generateAndSendMessage(campaign, lead, template);
      } catch (ClassNotFoundException | IOException | JSONException e) {
        errors.append(lead.getName() + "\n");
        e.printStackTrace();
      }
    }

    return errors.toString();
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void generateAndSendMessage(Campaign campaign, Model model, Template template)
      throws ClassNotFoundException, IOException, JSONException {
    Message message = templateMessageMarketingService.generateAndSendMessage(model, template);
    Beans.get(MessageService.class)
        .addMessageRelatedTo(message, Campaign.class.getCanonicalName(), campaign.getId());
  }

  protected MetaFile generateLog(
      String errorPartners, String errorLeads, MetaFile metaFile, Long campaignId) {

    if (metaFile == null) {
      metaFile = new MetaFile();
      metaFile.setFileName("EmailLog" + campaignId + ".text");
    }

    StringBuilder builder = new StringBuilder();
    builder.append(I18n.get(MarketingExceptionMessage.EMAIL_ERROR1));
    builder.append("\n");
    if (!errorPartners.isEmpty()) {
      builder.append(I18n.get("Partners") + ":\n");
      builder.append(errorPartners);
    }
    if (!errorLeads.isEmpty()) {
      builder.append(I18n.get("Leads") + ":\n");
      builder.append(errorLeads);
    }

    ByteArrayInputStream stream = new ByteArrayInputStream(builder.toString().getBytes());

    try {
      return Beans.get(MetaFiles.class).upload(stream, metaFile.getFileName());
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Transactional
  public void generateEvents(Campaign campaign) {

    LocalDateTime eventStartDateTime = campaign.getEventStartDateTime();
    LocalDateTime eventEndDateTime = campaign.getEventEndDateTime();

    Long duration = campaign.getDuration();

    for (Partner partner : campaign.getPartnerSet()) {
      Event event = new Event();

      if (partner.getIsContact()) {
        event.setContactPartner(partner);
      } else {
        event.setPartner(partner);
      }

      event.setUser(
          campaign.getGenerateEventPerPartnerOrLead()
              ? partner.getUser()
              : campaign.getEventUser());
      event.setSubject(campaign.getSubject());
      event.setTypeSelect(campaign.getEventTypeSelect());
      event.setStartDateTime(eventStartDateTime);
      event.setEndDateTime(eventEndDateTime);
      event.setDuration(duration);
      event.setTeam(
          campaign.getGenerateEventPerPartnerOrLead() ? partner.getTeam() : campaign.getTeam());
      event.setCampaign(campaign);
      event.setStatusSelect(1);
      eventRepo.save(event);
    }

    for (Lead lead : campaign.getLeadSet()) {
      Event event = new Event();
      event.setEventLead(lead);
      event.setUser(
          campaign.getGenerateEventPerPartnerOrLead() ? lead.getUser() : campaign.getEventUser());
      event.setSubject(campaign.getSubject());
      event.setTypeSelect(campaign.getEventTypeSelect());
      event.setStartDateTime(eventStartDateTime);
      event.setEndDateTime(eventEndDateTime);
      event.setDuration(duration);
      event.setTeam(
          campaign.getGenerateEventPerPartnerOrLead() ? lead.getTeam() : campaign.getTeam());
      event.setCampaign(campaign);
      event.setStatusSelect(1);
      eventRepo.save(event);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void generateTargets(Campaign campaign) throws AxelorException {

    TargetListService targetListService = Beans.get(TargetListService.class);

    Set<Partner> partnerSet = targetListService.getAllPartners(campaign.getTargetModelSet());
    Set<Lead> leadSet = targetListService.getAllLeads(campaign.getTargetModelSet());

    campaign.setPartnerSet(partnerSet);
    campaign.setLeadSet(leadSet);
  }

  @Override
  @Transactional
  public void inviteSelectedTargets(Campaign campaign, Campaign campaignContext) {

    Set<Partner> partners = campaign.getPartners();
    Set<Partner> notParticipatingPartnerSet = campaign.getNotParticipatingPartnerSet();

    for (Partner partner : campaignContext.getPartnerSet()) {
      if (partner.isSelected()
          && !partners.contains(partner)
          && !notParticipatingPartnerSet.contains(partner)) {
        campaign.addInvitedPartnerSetItem(partner);
      }
    }

    Set<Lead> leads = campaign.getLeads();
    Set<Lead> notParticipatingLeadSet = campaign.getNotParticipatingLeadSet();

    for (Lead lead : campaignContext.getLeadSet()) {
      if (lead.isSelected() && !leads.contains(lead) && !notParticipatingLeadSet.contains(lead)) {
        campaign.addInvitedLeadSetItem(lead);
      }
    }

    Beans.get(CampaignRepository.class).save(campaign);
  }

  @Override
  @Transactional
  public void inviteAllTargets(Campaign campaign) {

    Set<Partner> partners = campaign.getPartners();
    Set<Partner> notParticipatingPartnerSet = campaign.getNotParticipatingPartnerSet();

    for (Partner partner : campaign.getPartnerSet()) {
      if (!partners.contains(partner) && !notParticipatingPartnerSet.contains(partner)) {
        campaign.addInvitedPartnerSetItem(partner);
      }
    }

    Set<Lead> leads = campaign.getLeads();
    Set<Lead> notParticipatingLeadSet = campaign.getNotParticipatingLeadSet();

    for (Lead lead : campaign.getLeadSet()) {
      if (!leads.contains(lead) && !notParticipatingLeadSet.contains(lead)) {
        campaign.addInvitedLeadSetItem(lead);
      }
    }
    Beans.get(CampaignRepository.class).save(campaign);
  }

  @Override
  @Transactional
  public void addParticipatingTargets(Campaign campaign, Campaign campaignContext) {

    for (Partner partner : campaignContext.getInvitedPartnerSet()) {
      if (partner.isSelected()) {
        campaign.addPartner(partner);
        campaign.removeInvitedPartnerSetItem(partner);
      }
    }

    for (Lead lead : campaignContext.getInvitedLeadSet()) {
      if (lead.isSelected()) {
        campaign.addLead(lead);
        campaign.removeInvitedLeadSetItem(lead);
      }
    }

    Beans.get(CampaignRepository.class).save(campaign);
  }

  @Override
  @Transactional
  public void addNotParticipatingTargets(Campaign campaign, Campaign campaignContext) {

    for (Partner partner : campaignContext.getInvitedPartnerSet()) {
      if (partner.isSelected()) {
        campaign.addNotParticipatingPartnerSetItem(partner);
        campaign.removeInvitedPartnerSetItem(partner);
      }
    }

    for (Lead lead : campaignContext.getInvitedLeadSet()) {
      if (lead.isSelected()) {
        campaign.addNotParticipatingLeadSetItem(lead);
        campaign.removeInvitedLeadSetItem(lead);
      }
    }

    Beans.get(CampaignRepository.class).save(campaign);
  }

  @Override
  @Transactional
  public void markLeadPresent(Campaign campaign, Lead lead) {

    campaign.addPresentLeadSetItem(lead);
    Beans.get(CampaignRepository.class).save(campaign);
  }

  @Override
  @Transactional
  public void markPartnerPresent(Campaign campaign, Partner partner) {

    campaign.addPresentPartnerSetItem(partner);
    Beans.get(CampaignRepository.class).save(campaign);
  }
}
