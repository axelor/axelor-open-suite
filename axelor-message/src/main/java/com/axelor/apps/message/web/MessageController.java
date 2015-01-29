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
package com.axelor.apps.message.web;

import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.apps.message.service.MessageService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MessageController {

	
	public void sendByEmail(ActionRequest request, ActionResponse response) {
		
		Message message = request.getContext().asType(Message.class);
		MessageService messageService =  Beans.get(MessageService.class);
		
		message = messageService.sendMessageByEmail(messageService.find(message.getId()));
		
		response.setReload(true);
		
		if(message.getStatusSelect() == MessageRepository.STATUS_SENT)  {
			if(message.getSentByEmail())  {
				response.setFlash(I18n.get(IExceptionMessage.MESSAGE_4));
			}
			else  {
				response.setFlash(I18n.get(IExceptionMessage.MESSAGE_5));
			}
			
		}
		else  {
			response.setFlash(I18n.get(IExceptionMessage.MESSAGE_6));
		}
	}
	
	
	
}