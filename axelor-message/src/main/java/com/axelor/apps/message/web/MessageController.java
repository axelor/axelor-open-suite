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
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;

@Singleton
public class MessageController {

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
		List<Integer> ids = (List<Integer>) request.getContext().get("_ids");

		if (ids != null && !ids.isEmpty()) {
			int countError = 0;
		    for (Integer id: ids) {
		        Message message = messageRepo.find(Long.valueOf(id));
		        try {
		        	messageService.sendMessage(message);
				} catch (AxelorException e) {
		            countError++;
				}
			}
			response.setFlash(
					String.format("%d message%s sent and %d error%s append.",
							ids.size() - countError,
							(ids.size() - countError > 1 ? "s" : ""),
							countError,
							(countError > 1 ? "s" : "")
					)
			);
			response.setReload(true);
		} else {
			response.setFlash("Please select one or more message !");
		}
	}
}
