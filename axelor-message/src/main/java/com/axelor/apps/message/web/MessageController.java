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
package com.axelor.apps.message.web;

import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.apps.message.service.MessageService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class MessageController {

	@Inject
	private MessageRepository messageRepo;
	
	@Inject
	private MessageService messageService;
	
	public void sendMessage(ActionRequest request, ActionResponse response) throws AxelorException {

		Message message = request.getContext().asType(Message.class);

		message = messageService.sendMessage( messageRepo.find( message.getId() ) );

		response.setReload(true);

		if ( message.getStatusSelect() == MessageRepository.STATUS_SENT ) {

			if ( message.getSentByEmail() ) { response.setFlash( I18n.get( IExceptionMessage.MESSAGE_4 ) ); }
			else { response.setFlash( I18n.get( IExceptionMessage.MESSAGE_5 ) ); }

		} else  { response.setFlash( I18n.get( IExceptionMessage.MESSAGE_6 ) );	}

	}

}