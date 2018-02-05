/**
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
package com.axelor.apps.marketing.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

import javax.mail.MessagingException;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.marketing.db.Campaign;
import com.axelor.apps.marketing.db.TargetList;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;

public class CampaignServiceImpl implements CampaignService {
	
	@Inject
	private TemplateMessageService templateMessageService;
	
	@Inject
	private MetaFiles metaFiles;
	
	public MetaFile sendEmail(Campaign campaign) {
		
		String errorPartners = "";
		String errorLeads = "";
		for (TargetList target : campaign.getTargetListSet()) {
			errorPartners = sendToPartners(target.getPartnerSet(), campaign.getPartnerTemplate());
			errorLeads = sendToLeads(target.getLeadSet(), campaign.getLeadTemplate());
		}
		
		if (errorPartners.isEmpty() && errorLeads.isEmpty()) {
			return null;
		}
		
		return generateLog(errorPartners, errorLeads, campaign.getEmailLog(), campaign.getId());
	}

	private String sendToPartners(Set<Partner> partnerSet,
			Template template) {
		
		StringBuilder errors = new StringBuilder();
		
		for (Partner partner : partnerSet) {
			
			try {
				templateMessageService.generateAndSendMessage(partner, template);
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException | MessagingException | IOException
					| AxelorException e) {
				errors.append(partner.getName() + "\n");
				e.printStackTrace();
			}
			
		}
		
		return errors.toString();
	}
	
	private String sendToLeads(Set<Lead> leadSet, Template template) {
		
		StringBuilder errors = new StringBuilder();
		
		for (Lead lead : leadSet) {
			
			try {
				templateMessageService.generateAndSendMessage(lead, template);
			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException | MessagingException | IOException
					| AxelorException e) {
				errors.append(lead.getName() + "\n");
				e.printStackTrace();
			}
			
		}
		
		return errors.toString();
		
	}
	
	private MetaFile generateLog(String errorPartners, String errorLeads, MetaFile metaFile, Long campaignId) {
		
		if (metaFile == null) {
			metaFile = new MetaFile();
			metaFile.setFileName("EmailLog" + campaignId + ".text");
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append(I18n.get("Error in sending an email to the following targets"));
		builder.append("\n");
		if (!errorPartners.isEmpty()) {
			builder.append("Partners:\n");
			builder.append(errorPartners);
		}
		if (!errorLeads.isEmpty()) {
			builder.append("Leads:\n");
			builder.append(errorLeads);
		}
		
		ByteArrayInputStream stream = new ByteArrayInputStream(builder.toString().getBytes());
		
		try {
			return metaFiles.upload(stream, metaFile.getFileName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}	
