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

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.MailAccountService;
import com.axelor.auth.db.User;
import com.axelor.meta.db.repo.MetaAttachmentRepository;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MessageServiceCrmImpl extends MessageServiceBaseImpl {
	
	@Inject
	public MessageServiceCrmImpl( MetaAttachmentRepository metaAttachmentRepository, MailAccountService mailAccountService, UserService userService ) { 
		super(metaAttachmentRepository, mailAccountService, userService);
	}
	
	@Transactional
	public Message createMessage( Event event )  {
		
		User recipientUser = event.getUser();
		
		List<EmailAddress> toEmailAddressList = Lists.newArrayList();
		
		if ( recipientUser != null )  {
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
				getTodayLocalTime(), 
				false, 
				STATUS_SENT, 
				"Remind : " + event.getSubject(), 
				TYPE_RECEIVED, 
				null,
				toEmailAddressList, 
				null,
				null,
				null,
				MEDIA_TYPE_EMAIL);
		
		return save(message);
	}	
	
	
	
	
	
}