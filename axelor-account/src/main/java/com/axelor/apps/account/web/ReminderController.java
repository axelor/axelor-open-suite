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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Reminder;
import com.axelor.apps.account.db.repo.ReminderRepository;
import com.axelor.apps.account.service.debtrecovery.ReminderActionService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ReminderController {


    private ReminderRepository reminderRepository;
    private ReminderActionService reminderService;

    @Inject
    public ReminderController(ReminderRepository reminderRepository, ReminderActionService reminderService) {
        this.reminderRepository = reminderRepository;
        this.reminderService = reminderService;
    }

    public void runReminder(ActionRequest request, ActionResponse response) {
        Reminder reminder = request.getContext().asType(Reminder.class);

        reminder = reminderRepository.find(reminder.getId());
        try {
            reminder.setReminderMethodLine(reminder.getWaitReminderMethodLine());
            reminderService.runManualAction(reminder);
            //find the updated reminder
            reminder = reminderRepository.find(reminder.getId());
            reminderService.runMessage(reminder);
            response.setReload(true);
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }

    }
}
