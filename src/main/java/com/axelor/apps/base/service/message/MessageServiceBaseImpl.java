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
package com.axelor.apps.base.service.message;

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.MessageServiceImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public abstract class MessageServiceBaseImpl extends MessageServiceImpl {

	private static final Logger LOG = LoggerFactory.getLogger(MessageServiceBaseImpl.class);
	
	private DateTime todayTime;
	
	@Inject
	private UserService userService;
	
	@Inject
	public MessageServiceBaseImpl(UserService userService) {

		this.todayTime = GeneralService.getTodayDateTime();
		this.userService = userService;
	}
	
	
	@Override
	@Transactional
	public Message createMessage(String model, int id, String subject, String content, List<EmailAddress> toEmailAddressList, List<EmailAddress> ccEmailAddressList, 
			List<EmailAddress> bccEmailAddressList, MailAccount mailAccount, String linkPath, String addressBlock, int mediaTypeSelect)  {
		
		Message message = super.createMessage(
				content, 
				null, 
				model, 
				id, 
				null, 
				0, 
				todayTime.toLocalDateTime(), 
				false, 
				Message.STATUS_DRAFT, 
				subject, 
				Message.TYPE_SENT,
				toEmailAddressList,
				ccEmailAddressList,
				bccEmailAddressList,
				mailAccount,
				linkPath,
				addressBlock,
				mediaTypeSelect)
				.save();
		
		message.setCompany(userService.getUserActiveCompany());
		message.setSenderUser(userService.getUser());
		return message;
		
	}	
	
	@Override
	@Transactional
	public Message sendMessageByEmail(Message message)  {
			
		super.sendMessageByEmail(message);
		this.sendToUser(message);
			
		return message;
		
	}
	
	
	private void sendToUser(Message message)  {
		
		if(!message.getSentByEmail() && message.getRecipientUser()!=null)  {
			message.setStatusSelect(Message.STATUS_SENT);
			message.setSentByEmail(false);
			message.save();
		}
	}
	
	
	public String printMessage(Message message){
//		Company company = message.getCompany();
//		if(company == null)
//			return null;
//		PrintingSettings printSettings = company.getPrintingSettings();
//		printSettings = company.getPrintingSettings();
//		if(printSettings == null || printSettings.getDefaultMailBirtTemplate() == null)
//			return null;
//		BirtTemplate birtTemplate = printSettings.getDefaultMailBirtTemplate();
//		LOG.debug("Default BirtTemplate : {}",birtTemplate);
//		TemplateMaker maker = new TemplateMaker(new Locale("fr"), '$', '$');
//		maker.setContext(JPA.find(message.getClass(), message.getId()), "Message");
//		try {
//			return templateMessageService.generatePdfFromBirtTemplate(maker, birtTemplate, "url");
//		} catch (AxelorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return null;
	}
	
	
}