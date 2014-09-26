/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.message;

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.Message;
import com.axelor.auth.db.User;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MessageServiceCrmImpl extends MessageServiceBaseImpl {

	@Inject
	public MessageServiceCrmImpl(UserService userService) {
		super(userService);
		// TODO Auto-generated constructor stub
	}


	private static final Logger LOG = LoggerFactory.getLogger(MessageServiceCrmImpl.class);
	
	private DateTime todayTime;
	
	
	@Transactional
	public Message createMessage(Event event, MailAccount mailAccount)  {
		
		User recipientUser = event.getUser();
		
		List<EmailAddress> toEmailAddressList = Lists.newArrayList();
		
		if(recipientUser != null)  {
			Partner partner = recipientUser.getPartner();
			if(partner != null)  {
				EmailAddress emailAddress = partner.getEmailAddress();
				if(emailAddress != null)  {
					toEmailAddressList.add(emailAddress);
				}
			}
		}
		
		Message message = super.createMessage(
				event.getDescription(), 
				null, 
				RELATED_TO_EVENT, 
				event.getId().intValue(), 
				event.getRelatedToSelect(), 
				event.getRelatedToSelectId(), 
				todayTime.toLocalDateTime(), 
				false, 
				STATUS_SENT, 
				"Remind : "+event.getSubject(), 
				TYPE_RECEIVED,
				toEmailAddressList,
				null,
				null,
				mailAccount,
				null, null, 0);
		
		message.setRecipientUser(event.getResponsibleUser());
		
		return save(message);
	}	
	
	
	
	
	
}