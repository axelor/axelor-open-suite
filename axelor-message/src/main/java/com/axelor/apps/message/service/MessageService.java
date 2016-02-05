/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.message.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;

import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaAttachment;
import com.axelor.meta.db.MetaFile;
import com.google.inject.persist.Transactional;

public interface MessageService {
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Message createMessage(String model, int id, String subject, String content, EmailAddress fromEmailAddress, List<EmailAddress> replytoEmailAddressList, List<EmailAddress> toEmailAddressList, 
			List<EmailAddress> ccEmailAddressList, List<EmailAddress> bccEmailAddressList, Set<MetaFile> metaFiles, String addressBlock, int mediaTypeSelect);

	@Transactional(rollbackOn = Exception.class)
	public void attachMetaFiles( Message message, Set<MetaFile> metaFiles );
	
	public Set<MetaAttachment> getMetaAttachments( Message message );
	
	public Message sendMessage(Message message) throws AxelorException;
	
	@Transactional(rollbackOn = { MessagingException.class, IOException.class, Exception.class })
	public Message sendByEmail(Message message) throws MessagingException, IOException, AxelorException;
	
	@Transactional(rollbackOn = Exception.class)
	public Message sendToUser(Message message);
	
	@Transactional(rollbackOn = Exception.class)
	public Message sendByMail(Message message);
	
	public String printMessage(Message message) throws AxelorException;
	
}