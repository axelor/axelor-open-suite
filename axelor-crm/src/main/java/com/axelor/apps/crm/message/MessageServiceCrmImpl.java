/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.message;

import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.service.config.CrmConfigService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.SendMailQueueService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.db.repo.MetaAttachmentRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;

public class MessageServiceCrmImpl extends MessageServiceBaseImpl {

  @Inject
  public MessageServiceCrmImpl(
      MetaAttachmentRepository metaAttachmentRepository,
      MessageRepository messageRepository,
      SendMailQueueService sendMailQueueService,
      UserService userService,
      AppBaseService appBaseService) {
    super(
        metaAttachmentRepository,
        messageRepository,
        sendMailQueueService,
        userService,
        appBaseService);
  }

  @Transactional(rollbackOn = {Exception.class})
  public Message createMessage(Event event)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, IOException {

    // Get template depending on event type
    Template template = null;

    switch (event.getTypeSelect()) {
      case ICalendarEventRepository.TYPE_EVENT:
        template =
            Beans.get(CrmConfigService.class)
                .getCrmConfig(event.getUser().getActiveCompany())
                .getEventTemplate();
        break;

      case ICalendarEventRepository.TYPE_IN_BOUND_CALL:
      case ICalendarEventRepository.TYPE_OUT_BOUND_CALL:
        template =
            Beans.get(CrmConfigService.class)
                .getCrmConfig(event.getUser().getActiveCompany())
                .getCallTemplate();
        break;

      case ICalendarEventRepository.TYPE_MEETING:
        template =
            Beans.get(CrmConfigService.class)
                .getCrmConfig(event.getUser().getActiveCompany())
                .getMeetingTemplate();
        break;

      case ICalendarEventRepository.TYPE_TASK:
        template =
            Beans.get(CrmConfigService.class)
                .getCrmConfig(event.getUser().getActiveCompany())
                .getTaskTemplate();
        break;

      default:
        break;
    }

    Message message = Beans.get(TemplateMessageService.class).generateMessage(event, template);

    return messageRepository.save(message);
  }
}
