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
package com.axelor.apps.crm.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.crm.db.EventReminder;
import com.axelor.apps.crm.message.MessageServiceCrmImpl;
import com.axelor.apps.crm.service.EventReminderService;
import com.axelor.apps.crm.service.TargetService;
import com.axelor.apps.message.service.MailAccountService;

public abstract class BatchStrategy extends AbstractBatch {

	protected EventReminderService eventReminderService;
	protected MessageServiceCrmImpl messageServiceCrmImpl;
	protected MailAccountService mailAccountService;
	protected TargetService targetService;

	
	
	protected BatchStrategy(EventReminderService eventReminderService) {
		super();
		this.eventReminderService = eventReminderService;
	}
	
	protected BatchStrategy(MessageServiceCrmImpl messageServiceCrmImpl, MailAccountService mailAccountService) {
		super();
		this.messageServiceCrmImpl = messageServiceCrmImpl;
		this.mailAccountService = mailAccountService;
	}
	
	protected BatchStrategy(TargetService targetService) {
		super();
		this.targetService = targetService;
	}
	
	protected void updateEventReminder( EventReminder eventReminder ){
		
		eventReminder.addBatchSetItem( Batch.find( batch.getId() ) );
			
		incrementDone();
	}
	
//	protected void updateEvent( Event event ){
//		
//		event.addBatchSetItem( Batch.find( batch.getId() ) );
//			
//		incrementDone();
//	}
	
	
	
}
