/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.crm.service.batch;

import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.crm.db.EventReminder;
import com.axelor.apps.crm.db.repo.EventReminderRepository;
import com.axelor.apps.crm.message.MessageServiceCrmImpl;
import com.axelor.message.service.MailAccountService;
import com.google.inject.Inject;

public abstract class BatchStrategy extends AbstractBatch {

  protected MessageServiceCrmImpl messageServiceCrmImpl;
  protected MailAccountService mailAccountService;

  @Inject protected EventReminderRepository eventReminderRepo;

  protected BatchStrategy(
      MessageServiceCrmImpl messageServiceCrmImpl, MailAccountService mailAccountService) {
    super();
    this.messageServiceCrmImpl = messageServiceCrmImpl;
    this.mailAccountService = mailAccountService;
  }

  protected void updateEventReminder(EventReminder eventReminder) {

    eventReminder.addBatchSetItem(batchRepo.find(batch.getId()));
    eventReminder.setIsReminded(true);
    incrementDone();
    //		eventReminderService.save(eventReminder);
  }

  //	protected void updateEvent( Event event ){
  //
  //		event.addBatchSetItem( batchRepo.find( batch.getId() ) );
  //
  //		incrementDone();
  //	}

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_CRM_BATCH);
  }
}
