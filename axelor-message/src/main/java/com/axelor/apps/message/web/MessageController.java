/*
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
package com.axelor.apps.message.web;

import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.MessageServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class MessageController {

	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	private MessageRepository messageRepo;
	
	@Inject
	private MessageService messageService;

	public void sendMessage(ActionRequest request, ActionResponse response) {
		Message message = request.getContext().asType(Message.class);

		try {
			message = messageService.sendMessage(messageRepo.find(message.getId()));
			response.setReload(true);

			if ( message.getStatusSelect() == MessageRepository.STATUS_SENT ) {

				if ( message.getSentByEmail() ) { response.setFlash( I18n.get( IExceptionMessage.MESSAGE_4 ) ); }
				else { response.setFlash( I18n.get( IExceptionMessage.MESSAGE_5 ) ); }

			} else  { response.setFlash( I18n.get( IExceptionMessage.MESSAGE_6 ) );	}
		} catch (AxelorException e) {
			TraceBackService.trace(response, e);
		}
	}

	public void sendMessages(ActionRequest request, ActionResponse response) {
		List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
		List<Message> messages = messageService.findMessages(idList);
		if (messages.isEmpty()) {
			response.setFlash(I18n.get(IExceptionMessage.MESSAGE_8));
		} else {
			MessageServiceImpl.apply(messages, message -> {
				try {
					messageService.sendMessage(message);
				} catch (Exception e) {
					TraceBackService.trace(e);
				}
			});
			response.setFlash(I18n.get(IExceptionMessage.MESSAGE_9));
			response.setReload(true);
		}
	}

	public void regenerateMessages(ActionRequest request, ActionResponse response) {
		List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
		List<Message> messages = messageService.findMessages(idList);
		if (messages.isEmpty()) {
			response.setFlash(I18n.get(IExceptionMessage.MESSAGE_8));
		} else {
			MessageServiceImpl.apply(messages, message -> {
				try {
					messageService.regenerateMessage(message);
				} catch (Exception e) {
					TraceBackService.trace(e);
				}
			});
			response.setFlash(I18n.get(IExceptionMessage.MESSAGE_10));
			response.setReload(true);
		}
	}
}
