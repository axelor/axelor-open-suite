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
package com.axelor.apps.hr.service;

import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;

public class MailManagementService {
	
	protected MessageService messageService;
	protected TemplateMessageService templateMessageService;
	
	@Inject
	public MailManagementService(MessageService messageService, TemplateMessageService templateMessageService){
		
		this.messageService = messageService;
		this.templateMessageService = templateMessageService;
	}
	
	public boolean sendEmail(Template template,long objectId) throws AxelorException{
		if(template!=null){
			sendEmailTemplate(template,objectId);
			return true;
		}
		return false;
	}

	public void sendEmailTemplate(Template template,long objectId){
		String model = template.getMetaModel().getFullName();
		String tag = template.getMetaModel().getName();
		Message message = new Message();
		try{
			message = templateMessageService.generateMessage(objectId, model, tag, template);
			message = messageService.sendByEmail(message);
		}
		catch(Exception e){
			TraceBackService.trace(new Exception(e));
		}
	}
}
