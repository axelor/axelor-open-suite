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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.crm.message.MessageServiceCrmImpl;
import com.axelor.apps.crm.service.batch.BatchEventReminderMessage;
import com.axelor.apps.message.service.MailAccountService;
import com.google.inject.Injector;

public class EventReminderThread extends Thread {
	
	private Batch batch;
	private Injector injector;

	public EventReminderThread(Batch batch, Injector injector) {
		this.batch = batch;
		this.injector = injector;
	}

	@Override
	public void run() {
		
		new BatchEventReminderMessage(injector.getInstance(MessageServiceCrmImpl.class), injector.getInstance(MailAccountService.class)).process();
	}
	
	
	

}
